package com.darts.mis.model;

import com.darts.mis.Schedule;
import com.darts.mis.domain.Account;
import com.darts.mis.domain.Domain;
import com.darts.mis.domain.SubscriptionEdit;
import com.darts.mis.domain.SubscriptionEditOperation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

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

    public Optional<SubscriptionEdit> getLastRenewOrUpdate(){
        final List<SubscriptionEdit> edits = subscriptionItems
                .stream()
                .flatMap(si -> si.getEdits().stream())
                .filter(se -> se.getOperation() == SubscriptionEditOperation.REN || se.getOperation() == SubscriptionEditOperation.UPG)
                .collect(Collectors.toList());
        return edits.isEmpty() ? Optional.empty() : Optional.of(edits.get(edits.size() - 1));
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
