package com.darts.mis.model;

import com.darts.mis.Schedule;
import com.darts.mis.domain.Account;
import com.darts.mis.domain.Domain;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AccountItem {
    private final Account account;
    private final List<SubscriptionItem> subscriptionItems;
    private final Map<Domain, Schedule> revenues;

    public AccountItem(final Account account, List<SubscriptionItem> subscriptionItems) {
        this.account = account;
        this.subscriptionItems = subscriptionItems;
        this.revenues = new HashMap<>();
        subscriptionItems
                .stream()
                .flatMap(si -> si.getRevenues().entrySet().stream())
                .forEach(e -> revenues.computeIfAbsent(e.getKey(), d -> new Schedule()).add(e.getValue()));
    }

    public Account getAccount() {
        return account;
    }

    public List<SubscriptionItem> getSubscriptionItems() {
        return subscriptionItems;
    }

    public Map<Domain, Schedule> getRevenues() {
        return revenues;
    }
}
