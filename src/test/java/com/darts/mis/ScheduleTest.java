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
        eur.addFlow(LocalDate.of(2018, 1, 1), LocalDate.of(2018, 1, 10), Position.of("EUR", 4));
        eur.addFlow(LocalDate.of(2018, 1, 3), LocalDate.of(2018, 1, 15), Position.of("EUR", 2));
        this.usd = new Schedule();
        usd.addFlow(LocalDate.of(2018,  1, 5), LocalDate.of(2018, 1, 10), Position.of("USD", 5));
    }

    @Test
    public void testAccumulationTo(){
        eur.addFlow(LocalDate.of(2018, 1, 10), LocalDate.of(2018, 1, 15), Position.of("EUR", 1));
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
}
