package com.darts.mis.domain;

public enum AccountStatus {
    P("Prospect"),
    C("Client"),
    T("Tester"),
    D("Dead"),
    O("Other");

    private final String name;

    AccountStatus(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }

}
