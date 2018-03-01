package com.darts.mis;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class LocalDateRange {
    private final LocalDate incLo;
    private final LocalDate excHi;

    public LocalDateRange(LocalDate incLo, LocalDate excHi) {
        if (incLo == null || excHi == null ||incLo.isAfter(excHi)) throw new IllegalArgumentException();
        this.incLo = incLo;
        this.excHi = excHi;
    }

    public LocalDateRange(LocalDate on){
        this(on, on.plusDays(1L));
    }

    public LocalDate getFrom() {
        return incLo;
    }

    public LocalDate getTo() {
        return excHi;
    }

    public long getDayCount(){
        return ChronoUnit.DAYS.between(incLo, excHi);
    }

    public long getYearCount(){
        return ChronoUnit.YEARS.between(incLo, excHi);
    }

    public boolean isIncluding(LocalDate ld){
        return !ld.isBefore(incLo) && ld.isBefore(excHi);
    }

    public boolean isOverlapping(final LocalDateRange range){
        return range.excHi.isAfter(incLo) && range.incLo.isBefore(excHi);
    }

    public boolean isEmpty(){
        return incLo.equals(excHi);
    }

    @Override
    public String toString() {
        return String.format("[%s..%s)", incLo, excHi);
    }

    public static LocalDateRange of(int loYear, int loMonth, int loDay, int hiYear, int hiMonth, int hiDay){
        return new LocalDateRange(LocalDate.of(loYear, loMonth, loDay), LocalDate.of(hiYear, hiMonth, hiDay));
    }

    public static LocalDateRange on(int year, int month, int day){
        return new LocalDateRange(LocalDate.of(year, month, day));
    }
}
