package com.darts.mis.model;

import com.darts.mis.LocalDateRange;
import com.darts.mis.Position;
import com.darts.mis.Schedule;
import com.darts.mis.domain.*;

import java.math.BigDecimal;
import java.math.MathContext;
import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.darts.mis.domain.SubscriptionEditOperation.*;

public class SubscriptionItem implements Comparable<SubscriptionItem> {
    private final Subscription subscription;
    private final Map<Domain, Schedule> revenues;
    private final List<SubscriptionEdit> edits;
    private final Optional<SubscriptionEdit> lastRenewOrUpdate;
    private final Optional<LocalDateRange> range;

    public SubscriptionItem(Subscription subscription, Map<Domain, Long> queryCounts) {
        this.subscription = subscription;
        this.edits = subscription.getEdits().stream().sorted(Comparator.comparing(SubscriptionEdit::getId)).collect(Collectors.toList());
        final Schedule revenue = new Schedule();
        revenue.add(computeSubscriptionRevenue());
        revenue.add(computeServiceRevenue());
        revenue.normalize();
        final Map<Domain, BigDecimal> split = computeSplit(
                queryCounts.isEmpty() ?
                subscription.getDomains().stream().collect(Collectors.toMap(Function.identity(), d -> 1L)) :
                queryCounts
        );
        this.revenues = split.keySet().stream().collect(Collectors.toMap(Function.identity(), domain -> {
            final Schedule schedule = new Schedule(revenue);
            schedule.scalar(split.get(domain));
            return schedule;
        }));
        this.lastRenewOrUpdate = edits
                .stream()
                .filter(se -> se.getOperation() == SubscriptionEditOperation.REN || se.getOperation() == SubscriptionEditOperation.UPG)
                .reduce((se1, se2) -> se2);
        LocalDateRange range = null;
        for (final SubscriptionEdit edit: edits){
            range = edit.getRange().union(range);
        }
        for (final Service service: subscription.getServices()){
            range = service.getRange().union(range);
        }
        this.range = Optional.ofNullable(range);
    }

    public Optional<SubscriptionEdit> getLastRenewOrUpdate(){
        return lastRenewOrUpdate;
    }

    public Subscription getSubscription() {
        return subscription;
    }

    public Map<Domain, Schedule> getRevenues() {
        return revenues;
    }

    public boolean isCancelled(){
        return !edits.isEmpty() && edits.get(edits.size() - 1).getOperation() == SubscriptionEditOperation.REM;
    }

    public Optional<LocalDateRange> getRange() {
        return range;
    }

    private static Map<Domain, BigDecimal> computeSplit(Map<Domain, Long> queryCounts){
        // first establish a multiplication factor f for each domain. Sum(f) = 1. If no counts, split evenly across domains.
        if (queryCounts.size() == 0){
            throw new IllegalStateException("No domains for this subscription");
        }
        final List<Domain> domains = new ArrayList<>(queryCounts.keySet());
        final long total = queryCounts.values().stream().mapToLong(Long::longValue).sum();
        final Map<Domain, BigDecimal> factors = new HashMap<>();
        BigDecimal accumulator = BigDecimal.ZERO;
        for (int i = 0; i < domains.size() - 1; i++){
            final Domain domain = domains.get(i);
            final BigDecimal domainProportion = new BigDecimal((double) queryCounts.get(domain) / total, MathContext.DECIMAL64);
            factors.put(domain, domainProportion);
            accumulator = accumulator.add(domainProportion);
        }
        factors.put(domains.get(domains.size() - 1), BigDecimal.ONE.subtract(accumulator));
        return factors;
    }

    private Schedule computeSubscriptionRevenue(){
        final Schedule schedule = new Schedule();
        SubscriptionEdit last = null;
        for (final SubscriptionEdit subscriptionEdit: this.edits){
            LocalDate from = subscriptionEdit.getFrom();
            LocalDate to = subscriptionEdit.getTo();
            if (from.isAfter(to)){
                throw new IllegalStateException("Interval is <= 0, subscription: " + subscription.getId());
            }

            /*
            If UPG or REM, the amount of the previous edit is cancelled from the 'from' date.
            Cancel the amount of 'last' from the start date of the subscriptionEdit
            to the end date of 'last'.
             */
            if ((subscriptionEdit.getOperation() == REM || subscriptionEdit.getOperation() == UPG) && last != null && subscriptionEdit.getFrom().isBefore(last.getTo())){
                final LocalDateRange range = new LocalDateRange(from, last.getTo());
                final Position amount = Position.of(last.getCurrency(), last.getPrice()).negate();
                if (last.isYearlyPrice()){
                    schedule.add(Schedule.yearly(range, amount));
                } else {
                    schedule.add(Schedule.full(range, amount));
                }
            }

            /*
            Standard case
             */
            if (subscriptionEdit.getOperation() == REN || subscriptionEdit.getOperation() == UPG){
                if (subscriptionEdit.getPrice().signum() > 0){
                    if (from.equals(to)){
                        to = from.plusDays(1); // example: subscription id 19818. 5 of them in the database as of 2018/02/12
                    }
                    final Position amount = Position.of(subscriptionEdit.getCurrency(), subscriptionEdit.getPrice());
                    final LocalDateRange range = new LocalDateRange(from, to);
                    if (subscriptionEdit.isYearlyPrice()){
                        schedule.add(Schedule.yearly(range, amount));
                    } else {
                        schedule.add(Schedule.full(range, amount));
                    }
                }
            }

            /*
            For all operations, check adjustment (we have 5 adjustments for CRE cases, that have from==to)
             */
            if (subscriptionEdit.getAdjustment() != null){
                final Position amount = Position.of(subscriptionEdit.getCurrency(), subscriptionEdit.getAdjustment());
                final LocalDate inc = subscriptionEdit.getAdjustmentApplication() == 2 ? to.plusDays(-1) : from;
                final LocalDate exc = subscriptionEdit.getAdjustmentApplication() == 1 ? from.plusDays(1) : to;
                final LocalDateRange range = new LocalDateRange(inc, exc.equals(inc) ? exc.plusDays(1) : exc);
                schedule.add(Schedule.full(range, amount));
            }

            last = subscriptionEdit;
        }
        return schedule;
    }

    private Schedule computeServiceRevenue(){
        final Schedule schedule = new Schedule();
        subscription.getServices().forEach(service -> {
            BigDecimal amount = service.getPrice();
            if (service.getAdjustment() != null){
                amount = amount.add(service.getAdjustment());
            }
            schedule.add(Schedule.flat(service.getWhen(), Position.of(service.getCurrency(), amount)));
        });
        return schedule;
    }

    @Override
    public int compareTo(SubscriptionItem o) {
        return lastRenewOrUpdate.map(SubscriptionEdit::getTo).orElse(LocalDate.MIN).compareTo(o.lastRenewOrUpdate.map(SubscriptionEdit::getTo).orElse(LocalDate.MIN));
    }
}
