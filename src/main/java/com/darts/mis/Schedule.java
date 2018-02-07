package com.darts.mis;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.TreeMap;

public class Schedule extends TreeMap<LocalDate, Position> {

    public void addFlow(LocalDate on, Position p){
        addFlow(on, on, p);
    }

    public void addFlow(LocalDate inc, LocalDate exc, Position p){
        merge(inc, p, Position::add);
        merge(exc.plusDays(1), p.negate(), Position::add);
    }

    public Position accumulatedTo(LocalDate exc){
        Position ret = Position.ZERO;
        Position current = Position.ZERO;
        LocalDate last = null;
        for (Map.Entry<LocalDate, Position> e: entrySet()){
            if (last == null){
                last = e.getKey();
                current = e.getValue();
            } else if (e.getKey().compareTo(exc) <= 0){
                long days = ChronoUnit.DAYS.between(last, e.getKey());
                ret = ret.add(current.scalar(days));
                current = current.add(e.getValue());
            }
        }
        return ret;
    }
}
