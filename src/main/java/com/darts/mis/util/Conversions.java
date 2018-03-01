package com.darts.mis.util;

import java.time.LocalDate;
import java.util.Calendar;

public class Conversions {

    public static Calendar localDateToCalendar(LocalDate localDate){
        final Calendar calendar = Calendar.getInstance();
        calendar.clear();
        calendar.set(localDate.getYear(), localDate.getMonthValue() - 1 , localDate.getDayOfMonth());
        return calendar;
    }
}
