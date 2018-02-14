package com.darts.mis.model;

import com.darts.mis.domain.Account;

import java.util.List;

public class AccountItem {
    private final Account account;
    private final List<SubscriptionItem> subscriptionItems;

    public AccountItem(final Account account, List<SubscriptionItem> subscriptionItems) {
        this.account = account;
        this.subscriptionItems = subscriptionItems;
    }

    public Account getAccount() {
        return account;
    }

    public List<SubscriptionItem> getSubscriptionItems() {
        return subscriptionItems;
    }
}
