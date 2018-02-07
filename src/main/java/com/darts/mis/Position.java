package com.darts.mis;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by jpc on 9/29/16.
 */
public class Position extends HashMap<String, BigDecimal> {
    public static final Position ZERO = new Position();

    public Position() {
    }

    public Position(Position p){
        this.putAll(p);
    }

    private Position putAmount(String iso, double amount){
        this.put(iso, new BigDecimal(amount));
        return this;
    }

    public Position normalize(){
        return entrySet().stream().filter(e -> e.getValue().signum() != 0).collect(Position::new, (p, e) -> p.put(e.getKey(), e.getValue()), Position::add);
    }

    public boolean isZero(){
        return normalize().isEmpty();
    }

    public Position add(Position that){
        return entrySet().stream().collect(() -> new Position(that), (p, e) -> p.put(e.getKey(), e.getValue().add(p.getOrDefault(e.getKey(),BigDecimal.ZERO))), Position::add);
    }

    public Position subtract(Position that){
        return add(that.negate());
    }

    public Position scalar(BigDecimal bd){
        return entrySet().stream().filter(e -> e.getValue().signum() != 0).collect(Position::new, (p, e) -> p.put(e.getKey(), e.getValue().multiply(bd)), Position::add);
    }

    public Position negate(){
        return entrySet().stream().collect(Position::new, (p, e) -> p.put(e.getKey(), e.getValue().negate()), Position::add);
    }

    public Position filter(String... isos){
        final List<String> ts = Arrays.asList(isos);
        return entrySet().stream().filter(e -> ts.contains(e.getKey())).collect(Position::new, (p, e) -> p.put(e.getKey(), e.getValue()), Position::add);
    }

    public static Position of(String iso, double amount) {
        return new Position().putAmount(iso, amount);
    }

    public static Position of(String iso1, double amount1, String iso2, double amount2){
        return new Position().putAmount(iso1, amount1).putAmount(iso2, amount2);
    }

    public static Position of(String iso1, double amount1, String iso2, double amount2, String iso3, double amount3){
        return new Position().putAmount(iso1, amount1).putAmount(iso2, amount2).putAmount(iso3, amount3);
    }



    @Override
    public boolean equals(Object o) {
        if (o instanceof Position){
            final Position that = (Position) o;
            return subtract(that).normalize().isZero();
        }
        return false;
    }

    @Override
    public String toString() {
        return String.format("Position{%s}", entrySet().stream().map((e) -> String.format("%s:%s", e.getKey(), e.getValue())).collect(Collectors.joining(",")));
    }
}
