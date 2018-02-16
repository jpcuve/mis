package com.darts.mis.domain;

public enum AccountNature {
    P("Prospect"),
    C("Corporate"),
    S("Service Provider"),
    A("Academics"),
    I("Internal"),
    O("Other");

    private final String name;

    AccountNature(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
