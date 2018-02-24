package com.darts.mis;

import org.junit.Assert;
import org.junit.Test;

import java.time.LocalDate;

public class LocalDateRangeTest {
    @Test
    public void testRangeInclude(){
        final LocalDateRange range = LocalDateRange.of(2018, 2, 15, 2018, 2, 18);
        Assert.assertFalse(range.isIncluding(LocalDate.of(2018, 2, 14)));
        Assert.assertTrue(range.isIncluding(LocalDate.of(2018, 2, 15)));
        Assert.assertTrue(range.isIncluding(LocalDate.of(2018, 2, 16)));
        Assert.assertFalse(range.isIncluding(LocalDate.of(2018, 2, 18)));
        Assert.assertFalse(range.isIncluding(LocalDate.of(2018, 2, 19)));
    }

    @Test
    public void testRangeOverlap(){
        final LocalDateRange range = LocalDateRange.of(2018, 2, 15, 2018, 2, 18);
        Assert.assertFalse(range.isOverlapping(LocalDateRange.of(2018, 2, 1, 2018, 2, 4)));
        Assert.assertFalse(range.isOverlapping(LocalDateRange.of(2018, 2, 1, 2018, 2, 15)));
        Assert.assertTrue(range.isOverlapping(LocalDateRange.of(2018, 2, 1, 2018, 2, 16)));
        Assert.assertTrue(range.isOverlapping(LocalDateRange.of(2018, 2, 1, 2018, 2, 20)));
        Assert.assertTrue(range.isOverlapping(LocalDateRange.of(2018, 2, 15, 2018, 2, 20)));
        Assert.assertFalse(range.isOverlapping(LocalDateRange.of(2018, 2, 18, 2018, 2, 20)));
        Assert.assertFalse(range.isOverlapping(LocalDateRange.of(2018, 2, 19, 2018, 2, 20)));
    }

    @Test
    public void testDayCount(){
        final LocalDateRange range = LocalDateRange.of(2018, 2, 15, 2018, 2, 18);
        Assert.assertEquals(3L, range.getDayCount());

    }
}
