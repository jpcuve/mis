package com.darts.mis.domain;

import com.darts.mis.Position;
import com.darts.mis.Schedule;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.darts.mis.domain.SubscriptionEditOperation.*;

@Entity
@Table( name = "subscription")
@NamedQueries({
        @NamedQuery(name = Subscription.FULL_BY_ID, query = "select s from Subscription s left join fetch s.edits left join fetch s.services where s.id=:id"),
        @NamedQuery(name = Subscription.SUBSCRIPTION_ALL_IDS, query = "select s.id from Subscription s order by s.id"),
        @NamedQuery(name = Subscription.SUBSCRIPTION_ALL, query = "select distinct s from Subscription s left join fetch s.edits left join fetch s.services"),
        @NamedQuery(name = Subscription.SUBSCRIPTION_COUNT_QUERIES_BY_DOMAIN, query = "select s.id, hq.domain, count(hq.id) from Subscription s join s.edits se join se.historyQueries hq group by s.id, hq.domain")
})
public class Subscription {
    public static final String FULL_BY_ID = "subscription.fullById";
    public static final String SUBSCRIPTION_ALL_IDS = "subscription.allIds";
    public static final String SUBSCRIPTION_ALL = "subscription.all";
    public static final String SUBSCRIPTION_COUNT_QUERIES_BY_DOMAIN = "subscription.countQueriesByDomain";
    @Id
    @Column(name = "id", nullable = false)
    private Long id;
    @Basic
    @Column(name = "active")
    private boolean active;
    @ElementCollection
    @CollectionTable(name = "subscription_domain", joinColumns = { @JoinColumn(name = "subscription_fk")})
    @Enumerated(EnumType.STRING)
    @Column(name = "domain")
    private Set<Domain> domains;
    @ElementCollection
    @CollectionTable(name = "subscription_area", joinColumns = { @JoinColumn(name = "subscription_fk")})
    @Enumerated(EnumType.STRING)
    @Column(name = "area")
    private Set<Area> areas;
    @ManyToOne
    @JoinColumn(name = "account_fk")
    private Account account;
    @OneToMany(mappedBy = "subscription")
    private Set<SubscriptionEdit> edits;
    @OneToMany(mappedBy = "subscription")
    private Set<Service> services;
    @Transient
    private Schedule revenue;

    private Schedule computeRevenue(){
        final Schedule schedule = new Schedule();
        final List<SubscriptionEdit> list = this.edits.stream().sorted(Comparator.comparing(SubscriptionEdit::getId)).collect(Collectors.toList());
        SubscriptionEdit last = null;
        for (final SubscriptionEdit subscriptionEdit: list){
            LocalDate from = subscriptionEdit.getFrom();
            LocalDate to = subscriptionEdit.getTo();
            if (from.isAfter(to)){
                throw new IllegalStateException("Interval is <= 0, subscription: " + id);
            }

            /*
            If UPG or REM, the amount of the previous edit is cancelled from the 'from' date.
            Cancel the amount of 'last' from the start date of the subscriptionEdit
            to the end date of 'last'.
             */
            if ((subscriptionEdit.getOperation() == REM || subscriptionEdit.getOperation() == UPG) && last != null && subscriptionEdit.getFrom().isBefore(last.getTo())){
                final Position amount = Position.of(last.getCurrency(), last.getPrice()).negate();
                if (last.isYearlyPrice()){
                    schedule.add(Schedule.yearly(from, last.getTo(), amount));
                } else {
                    schedule.add(Schedule.full(from, last.getTo(), amount));
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
                    if (subscriptionEdit.isYearlyPrice()){
                        schedule.add(Schedule.yearly(from, to, amount));
                    } else {
                        schedule.add(Schedule.full(from, to, amount));
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
                schedule.add(Schedule.full(
                        inc,
                        exc.equals(inc) ? exc.plusDays(1) : exc,
                        amount));
            }

            last = subscriptionEdit;
        }
        this.services.forEach(service -> {
            BigDecimal amount = service.getPrice();
            if (service.getAdjustment() != null){
                amount = amount.add(service.getAdjustment());
            }
            schedule.add(Schedule.flat(service.getWhen(), Position.of(service.getCurrency(), amount)));
        });
        schedule.normalize();
        return schedule;
    }

    public Schedule getRevenue(){
        if (revenue == null){
            revenue = computeRevenue();
        }
        return revenue;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Set<Domain> getDomains() {
        return domains;
    }

    public void setDomains(Set<Domain> domains) {
        this.domains = domains;
    }

    public Set<Area> getAreas() {
        return areas;
    }

    public void setAreas(Set<Area> areas) {
        this.areas = areas;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public Set<SubscriptionEdit> getEdits() {
        return edits;
    }

    public void setEdits(Set<SubscriptionEdit> edits) {
        this.edits = edits;
    }

    public Set<Service> getServices() {
        return services;
    }

    public void setServices(Set<Service> services) {
        this.services = services;
    }

    @Override
    public String toString() {
        return "Subscription{" +
                "id=" + id +
                ", active=" + active +
                ", domains=" + domains +
                ", areas=" + areas +
                '}';
    }
}
