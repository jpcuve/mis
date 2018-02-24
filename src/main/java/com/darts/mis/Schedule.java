package com.darts.mis;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class Schedule extends TreeMap<LocalDate, Position> {
    public static final Schedule EMPTY = new Schedule();

    public Schedule() {
    }

    public Schedule(Schedule schedule){
        super(schedule);
    }

    public Schedule(LocalDateRange range, Position p){
        mergePosition(range, p);
    }

    public void scalar(BigDecimal bd){
        forEach((ld, p) -> put(ld, p.scalar(bd)));
    }

    private void mergePosition(LocalDateRange range, Position p){
        final BigDecimal divisor = new BigDecimal(range.getDayCount());
        final Position daily = p.inverseScalar(divisor);
        final Position remainder = p.subtract(daily.scalar(divisor));
        if (remainder.isZero()){
            mergeFlow(range.getFrom(), daily);
        } else {
            mergeFlow(range.getFrom(), daily.add(remainder));
            mergeFlow(range.getFrom().plusDays(1), remainder.negate());
        }
        mergeFlow(range.getTo(), daily.negate());
    }

    public void add(Schedule schedule){
        schedule.forEach((ld, p) -> merge(ld, p, Position::add));
    }

    public void normalize(){
        final Set<LocalDate> removes = new HashSet<>();
        for (final LocalDate localDate: keySet()){
            final Position position = get(localDate).normalize();
            if (position.isEmpty()){
                removes.add(localDate);
            }
            put(localDate, position);
        }
        removes.forEach(this::remove);
    }

    public void mergeFlow(LocalDate when, Position p){
        if (!p.isZero()){
            merge(when, p, Position::add);
        }
    }

    public Position accumulatedTo(LocalDate exc){
        Position ret = Position.ZERO;
        Position current = Position.ZERO;
        LocalDate last = null;
        for (Map.Entry<LocalDate, Position> e: entrySet()){
            if (last != null){
                LocalDate ld = exc.compareTo(e.getKey()) < 0 ? exc : e.getKey();
                if (last.compareTo(ld) < 0){
                    long days = ChronoUnit.DAYS.between(last, ld);
                    ret = ret.add(current.scalar(new BigDecimal(days)));
                }
            }
            current = current.add(e.getValue());
            last = e.getKey();
        }
        return ret;
    }

    public Position accumulated(LocalDateRange range){
        return accumulatedTo(range.getTo()).subtract(accumulatedTo(range.getFrom()));
    }

    public static Schedule full(LocalDateRange range, Position p){
        return new Schedule(range, p);
    }

    public static Schedule yearly(LocalDateRange range, Position p){
        final Schedule schedule = new Schedule();
        LocalDateRange remaining = range;
        long years = range.getYearCount();
        if (years > 0){
            final LocalDate intermediate = range.getFrom().plusYears(years);
            schedule.mergePosition(new LocalDateRange(range.getFrom(), intermediate), p.scalar(new BigDecimal(years)));
            remaining = new LocalDateRange(intermediate, range.getTo());
        }
        if (!remaining.isEmpty()){
            final long yearDayCount = new LocalDateRange(remaining.getFrom(), remaining.getFrom().plusYears(1)).getDayCount();
            final BigDecimal divisor = new BigDecimal(yearDayCount);
            final Position daily = p.inverseScalar(divisor);
            schedule.mergeFlow(range.getFrom(), daily);
            schedule.mergeFlow(range.getTo(), daily.negate());
        }
        return schedule;
    }

    public static Schedule flat(LocalDate on, Position p){
        return new Schedule(new LocalDateRange(on), p);
    }

}
