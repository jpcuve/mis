package com.darts.mis;

import java.time.LocalDate;

public class LocalDateRange {
    private final LocalDate incLo;
    private final LocalDate excHi;

    public LocalDateRange(LocalDate incLo, LocalDate excHi) {
        if (incLo == null || excHi == null ||incLo.isAfter(excHi)) throw new IllegalArgumentException();
        this.incLo = incLo;
        this.excHi = excHi;
    }

    public LocalDate getFrom() {
        return incLo;
    }

    public LocalDate getTo() {
        return excHi;
    }

    public boolean isIncluding(LocalDate ld){
        return !ld.isBefore(incLo) && ld.isBefore(excHi);
    }

    public boolean isOverlapping(final LocalDateRange range){
        return range.excHi.isAfter(incLo) && range.incLo.isBefore(excHi);
    }

    @Override
    public String toString() {
        return String.format("[%s-%s)", incLo, excHi);
    }

    public static LocalDateRange of(int loYear, int loMonth, int loDay, int hiYear, int hiMonth, int hiDay){
        return new LocalDateRange(LocalDate.of(loYear, loMonth, loDay), LocalDate.of(hiYear, hiMonth, hiDay));
    }

}
