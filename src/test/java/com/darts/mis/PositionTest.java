package com.darts.mis;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class PositionTest {
    @Test
    public void testAdditionAndNegate(){
        final Position p1 = Position.of("EUR", 10, "USD", 5);
        final Position p2 = Position.of("USD", 15, "JPY", 100);
        assertEquals(Position.of("EUR", 10, "USD", 20, "JPY", 100), p1.add(p2));
        assertEquals(Position.of("EUR", 10, "USD", 20, "JPY", 100), p2.add(p1));
        assertEquals(Position.of("USD", -15, "JPY", -100), p2.negate());
    }
}
