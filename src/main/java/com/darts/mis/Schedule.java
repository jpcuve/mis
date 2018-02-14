package com.darts.mis;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class Schedule extends TreeMap<LocalDate, Position> {

    public Schedule() {
    }

    public Schedule(Schedule schedule){
        super(schedule);
    }

    public Schedule(LocalDate inc, LocalDate exc, Position p){
        mergePosition(inc, exc, p);
    }

    public void scalar(BigDecimal bd){
        forEach((ld, p) -> put(ld, p.scalar(bd)));
    }

    private void mergePosition(LocalDate inc, LocalDate exc, Position p){
        long days = ChronoUnit.DAYS.between(inc, exc);
        final BigDecimal divisor = new BigDecimal(days);
        final Position daily = p.inverseScalar(divisor);
        final Position remainder = p.subtract(daily.scalar(divisor));
        if (remainder.isZero()){
            mergeFlow(inc, daily);
        } else {
            mergeFlow(inc, daily.add(remainder));
            mergeFlow(inc.plusDays(1), remainder.negate());
        }
        mergeFlow(exc, daily.negate());
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

    public Position accumulated(LocalDate inc, LocalDate exc){
        return accumulatedTo(exc).subtract(accumulatedTo(inc));
    }


    public static Schedule full(LocalDate inc, LocalDate exc, Position p){
        return new Schedule(inc, exc, p);
    }

    public static Schedule yearly(LocalDate inc, LocalDate exc, Position p){
        final Schedule schedule = new Schedule();
        LocalDate start = inc;
        long years = ChronoUnit.YEARS.between(inc, exc);
        if (years > 0){
            start = inc.plusYears(years);
            schedule.mergePosition(inc, start, p.scalar(new BigDecimal(years)));
        }
        long remainingDays = ChronoUnit.DAYS.between(start, start.plusYears(1));
        if (remainingDays > 0){
            final BigDecimal divisor = new BigDecimal(remainingDays);
            final Position daily = p.inverseScalar(divisor);
            schedule.mergeFlow(start, daily);
            schedule.mergeFlow(exc, daily.negate());
        }
        return schedule;
    }

    public static Schedule flat(LocalDate on, Position p){
        return new Schedule(on, on.plusDays(1), p);
    }

}
