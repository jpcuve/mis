package com.darts.mis;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.time.LocalDate;

public class ScheduleTest {
    private Schedule eur;
    private Schedule usd;

    @Before
    public void init(){
        this.eur = new Schedule();
        eur.add(new Schedule(LocalDate.of(2018, 1, 1), LocalDate.of(2018, 1, 10), Position.of("EUR", 36)));
        eur.add(new Schedule(LocalDate.of(2018, 1, 3), LocalDate.of(2018, 1, 15), Position.of("EUR", 24)));
        this.usd = new Schedule();
        usd.add(new Schedule(LocalDate.of(2018,  1, 5), LocalDate.of(2018, 1, 10), Position.of("USD", 25)));
    }

    @Test
    public void testAccumulationTo(){
        eur.add(new Schedule(LocalDate.of(2018, 1, 10), LocalDate.of(2018, 1, 15), Position.of("EUR", 5)));
        System.out.println(2 + ": " + eur.accumulatedTo(LocalDate.of(2018, 1, 2)));
        for (int i = 1; i < 32; i++){
            LocalDate of = LocalDate.of(2018, 1, i);
            System.out.println(of + ": " + eur.accumulatedTo(of));
        }
        Assert.assertEquals(Position.of("EUR", 65), eur.accumulatedTo(LocalDate.of(2018, 2, 1)));
    }

    @Test
    public void testAccumulation(){
        System.out.println(eur.accumulated(LocalDate.of(2018, 1, 7), LocalDate.of(2018, 1, 9)));
    }

    @Test
    public void testAdd(){
        final Schedule total = new Schedule();
        total.add(eur);
        total.add(usd);
        Position accumulated = total.accumulated(LocalDate.of(2018, 1, 4), LocalDate.of(2018, 1, 9));
        System.out.println(accumulated);
        Assert.assertEquals(Position.of("USD", 20, "EUR", 30), accumulated);
    }

    @Test
    public void split(){
        final Schedule schedule = new Schedule(LocalDate.of(2018, 1, 1), LocalDate.of(2018, 1,4), Position.of("EUR", 10), 2);
        for (int i = 1; i < 32; i++) {
            LocalDate of = LocalDate.of(2018, 1, i);
            System.out.println(of + ": " + schedule.accumulatedTo(of));
        }
        Assert.assertEquals(Position.of("EUR", 10), schedule.accumulatedTo(LocalDate.of(2018, 1, 31)));
    }

    @Test
    public void testNormalize(){
        final Schedule schedule = new Schedule();
        final LocalDate localDate = LocalDate.of(2018, 1, 1);
        schedule.mergeFlow(localDate, Position.of("USD", 10, "EUR", 5));
        schedule.mergeFlow(localDate, Position.of("USD", 15, "EUR", -7));
        schedule.mergeFlow(localDate, Position.of("USD", -25, "EUR", 2));
        schedule.normalize();
        Assert.assertEquals(true, schedule.isEmpty());
    }
}
