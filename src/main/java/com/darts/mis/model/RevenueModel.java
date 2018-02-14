package com.darts.mis.model;

import com.darts.mis.DataFacade;
import com.darts.mis.domain.Account;
import com.darts.mis.domain.Domain;

import java.math.MathContext;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

public class RevenueModel {
    public static final MathContext MATH_CONTEXT = new MathContext(2, RoundingMode.FLOOR);
    private final Map<Long, Map<Domain, Long>> queryCounts;
    private final Set<String> currencies = new HashSet<>();
    private final Set<Integer> years = new HashSet<>();
    private final List<AccountItem> accountItems;

    public RevenueModel(DataFacade facade) {
//        final List<Account> accounts = facade.findAccountByIds(Arrays.asList(1L, 4L));
        final List<Account> accounts = facade.findAllAccounts();
        accounts
                .stream()
                .flatMap(a -> a.getSubscriptions().stream())
                .flatMap(s -> s.getEdits().stream())
                .forEach(subscriptionEdit -> {
                    currencies.add(subscriptionEdit.getCurrency());
                    years.add(subscriptionEdit.getFrom().getYear());
                    years.add(subscriptionEdit.getTo().getYear());
                });
        this.queryCounts = facade.countSubscriptionQueriesByDomain();
        this.accountItems = accounts
                .stream()
                .map(a -> {
                    final List<SubscriptionItem> subscriptionItems = a.getSubscriptions()
                            .stream()
                            .map(s -> new SubscriptionItem(s, queryCounts.getOrDefault(s.getId(), Collections.emptyMap())))
                            .collect(Collectors.toList());
                    return new AccountItem(a, subscriptionItems);
                })
                .collect(Collectors.toList());
        // compute stuff

    }

    public Map<Long, Map<Domain, Long>> getQueryCounts() {
        return queryCounts;
    }

    public List<AccountItem> getAccountItems() {
        return accountItems;
    }

    public Optional<AccountItem> findAccount(long accountId){
        for (final AccountItem accountItem: accountItems){
            if (accountItem.getAccount().getId().equals(accountId)){
                return Optional.of(accountItem);
            }
        }
        return Optional.empty();
    }

    public Optional<SubscriptionItem> findSubscription(long subscriptionId){
        for (final AccountItem accountItem: accountItems){
            for (final SubscriptionItem subscriptionItem: accountItem.getSubscriptionItems()){
                if (subscriptionItem.getSubscription().getId().equals(subscriptionId)){
                    return Optional.of(subscriptionItem);
                }
            }
        }
        return Optional.empty();
    }
}
