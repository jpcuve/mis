package com.darts.mis;

import java.math.BigDecimal;
import java.math.RoundingMode;
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

    public Schedule(LocalDate inc, LocalDate exc, boolean yearly, Position p){
        this(inc, exc, yearly, p, 2);
    }

    public Schedule(LocalDate inc, LocalDate exc, boolean yearly, Position p, int scale){
        if (exc.compareTo(inc) < 0) throw new IllegalArgumentException();
        long days = ChronoUnit.DAYS.between(inc, yearly ? inc.plusYears(1) : exc);
        final BigDecimal divisor = new BigDecimal(days);
        final Position dividend = p.inverseScalar(divisor, scale, RoundingMode.FLOOR);
        final Position remainder = p.subtract(dividend.scalar(divisor));
        if (remainder.isZero()){
            mergeFlow(inc, dividend);
        } else {
            mergeFlow(inc, dividend.add(remainder));
            mergeFlow(inc.plusDays(1), remainder.negate());
        }
        mergeFlow(exc, dividend.negate());
    }

    public Schedule(LocalDate on, Position p){
        this(on, on.plusDays(1), false, p, 0);
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

}
