package com.darts.mis;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.TreeMap;

public class Schedule extends TreeMap<LocalDate, Position> {

    public Schedule() {
    }

    public Schedule(Schedule schedule){
        super(schedule);
    }

    public void addFlow(LocalDate on, Position p){
        addFlow(on, on.plusDays(1), p);
    }

    public void addFlow(LocalDate inc, LocalDate exc, Position p){
        if (exc.compareTo(inc) <= 0) throw new IllegalArgumentException();
        merge(inc, p, Position::add);
        merge(exc, p.negate(), Position::add);
    }

    public void add(Schedule schedule){
        schedule.forEach((ld, p) -> merge(ld, p, Position::add));
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

    public static Schedule split(Position p, int scale, LocalDate inc, LocalDate exc){
        if (exc.compareTo(inc) <= 0) throw new IllegalArgumentException();
        final BigDecimal divisor = new BigDecimal(ChronoUnit.DAYS.between(inc, exc));
        final Position dividend = p.scalar(BigDecimal.ONE.divide(divisor, scale, RoundingMode.FLOOR));
        final Position remainder = p.subtract(dividend.scalar(divisor));
        final Schedule schedule = new Schedule();
        schedule.addFlow(inc, inc.plusDays(1), dividend.add(remainder));
        schedule.addFlow(inc.plusDays(1), exc, dividend);
        return schedule;
    }
}
