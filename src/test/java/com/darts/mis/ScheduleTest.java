package com.darts.mis;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.time.LocalDate;

public class ScheduleTest {
    private Schedule schedule;

    @Before
    public void init(){
        this.schedule = new Schedule();
        schedule.add(LocalDate.of(2018, 1, 1), LocalDate.of(2018, 1, 10), Position.of("EUR", 4));
        schedule.add(LocalDate.of(2018, 1, 3), LocalDate.of(2018, 1, 15), Position.of("EUR", 2));
    }

    @Test
    public void testAccumulationTo(){
        schedule.add(LocalDate.of(2018, 1, 10), LocalDate.of(2018, 1, 15), Position.of("EUR", 1));
        System.out.println(2 + ": " + schedule.accumulatedTo(LocalDate.of(2018, 1, 2)));
        for (int i = 1; i < 32; i++){
            LocalDate of = LocalDate.of(2018, 1, i);
            System.out.println(of + ": " + schedule.accumulatedTo(of));
        }
        Assert.assertEquals(Position.of("USD", 65), schedule.accumulatedTo(LocalDate.of(2018, 2, 1)));
    }

    @Test
    public void testAccumulation(){
        System.out.println(schedule.accumulated(LocalDate.of(2018, 1, 7), LocalDate.of(2018, 1, 9)));
    }
}
