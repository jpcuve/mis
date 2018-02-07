package com.darts.mis;

import org.junit.Test;

import java.time.LocalDate;

public class ScheduleTest {

    @Test
    public void testAccumulation(){
        final Schedule schedule = new Schedule();
        schedule.addFlow(LocalDate.of(2018, 1, 1), LocalDate.of(2018, 1, 10), new Position("EUR", 4));
        schedule.addFlow(LocalDate.of(2018, 1, 3), LocalDate.of(2018, 1, 15), new Position("EUR", 2));
        schedule.addFlow(LocalDate.of(2018, 1, 10), LocalDate.of(2018, 1, 15), new Position("EUR", 1));
        for (int i = 1; i < 32; i++){
            System.out.println(i + ": " + schedule.accumulatedTo(LocalDate.of(2018, 1, i)));
        }

    }
}
