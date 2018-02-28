package com.darts.mis;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class Position extends HashMap<String, BigDecimal> {
    public static final Position ZERO = new Position();

    public Position() {
    }

    public Position(Position p){
        this.putAll(p);
    }

    public Position putAmount(String iso, BigDecimal amount){
        this.put(iso, amount);
        return this;
    }

    public Position putAmount(String iso, double amount){
        return this.putAmount(iso, new BigDecimal(amount));
    }

    public Position normalize(){
        return entrySet().stream().filter(e -> e.getValue().signum() != 0).collect(Position::new, (p, e) -> p.put(e.getKey(), e.getValue()), Position::add);
    }

    public boolean isZero(){
        return normalize().isEmpty();
    }

    public BigDecimal dot(Position that){
        BigDecimal bd = BigDecimal.ZERO;
        for (Map.Entry<String, BigDecimal> e: entrySet()){
            bd = bd.add(e.getValue().multiply(that.getOrDefault(e.getKey(), BigDecimal.ZERO)));
        }
        return bd;
    }

    public Position add(Position that){
        return entrySet().stream().collect(() -> new Position(that), (p, e) -> p.put(e.getKey(), e.getValue().add(p.getOrDefault(e.getKey(), BigDecimal.ZERO))), Position::add);
    }

    public Position subtract(Position that){
        return add(that.negate());
    }

    public Position scalar(BigDecimal bd){
        return entrySet().stream().filter(e -> e.getValue().signum() != 0).collect(Position::new, (p, e) -> p.put(e.getKey(), e.getValue().multiply(bd)), Position::add);
    }

    public Position inverseScalar(BigDecimal bd, MathContext mathContext){
        return entrySet().stream().filter(e -> e.getValue().signum() != 0).collect(Position::new, (p, e) -> p.put(e.getKey(), e.getValue().divide(bd, mathContext)), Position::add);
    }

    public Position inverseScalar(BigDecimal bd){
        return inverseScalar(bd, MathContext.DECIMAL128);
    }

    public Position negate(){
        return entrySet().stream().collect(Position::new, (p, e) -> p.put(e.getKey(), e.getValue().negate()), Position::add);
    }

    public Position inverse(MathContext mathContext){
        return entrySet().stream().filter(e -> e.getValue().signum() != 0).collect(Position::new, (p, e) -> p.put(e.getKey(), BigDecimal.ONE.divide(e.getValue(), mathContext)), Position::add);
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

    public static Position of(String iso, BigDecimal amount) {
        return new Position().putAmount(iso, amount);
    }

    public static Position of(String iso1, BigDecimal amount1, String iso2, BigDecimal amount2){
        return new Position().putAmount(iso1, amount1).putAmount(iso2, amount2);
    }

    public static Position of(String iso1, BigDecimal amount1, String iso2, BigDecimal amount2, String iso3, BigDecimal amount3){
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
        return String.format("%s", entrySet().stream().map((e) -> String.format("%s %s", e.getKey(), e.getValue())).collect(Collectors.joining(",")));
    }
}
