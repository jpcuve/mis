package com.darts.mis.model;

import com.darts.mis.DataFacade;
import com.darts.mis.domain.Account;
import com.darts.mis.domain.Domain;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Component
@Scope("singleton")
public class RevenueModel {
    private final DataFacade facade;
    private List<String> currencies;
    private List<Integer> years;
    private List<AccountItem> accountItems;
    @Value("${app.account-ids}")
    private String accountIds;

    @Autowired
    public RevenueModel(DataFacade facade) {
        this.facade = facade;
    }

    @PostConstruct
    public void init(){
        final List<Account> accounts = accountIds == null || accountIds.length() == 0 ?
                facade.findAllAccounts() :
                facade.findAccountByIds(Arrays.stream(accountIds.split(",")).map(Long::parseLong).collect(Collectors.toList()));
        final Set<String> cs = new TreeSet<>();
        final Set<Integer> ys = new TreeSet<>();
        accounts
                .stream()
                .flatMap(a -> a.getSubscriptions().stream())
                .flatMap(s -> s.getEdits().stream())
                .forEach(subscriptionEdit -> {
                    cs.add(subscriptionEdit.getCurrency());
                    ys.add(subscriptionEdit.getFrom().getYear());
                    ys.add(subscriptionEdit.getTo().getYear());
                });
        this.currencies = new ArrayList<>(cs);
        this.years = new ArrayList<>(ys);
        years.sort(Comparator.reverseOrder());
        final Map<Long, Map<Domain, Long>> queryCounts = facade.countSubscriptionQueriesByDomain();
        this.accountItems = accounts
                .stream()
                .sorted(Comparator.comparing(Account::getName))
                .map(a -> {
                    final List<SubscriptionItem> subscriptionItems = a.getSubscriptions()
                            .stream()
                            .map(s -> new SubscriptionItem(s, queryCounts.getOrDefault(s.getId(), Collections.emptyMap())))
                            .sorted()
                            .collect(Collectors.toList());
                    return new AccountItem(a, subscriptionItems);
                })
                .collect(Collectors.toList());
    }

    public List<String> getCurrencies() {
        return currencies;
    }

    public List<Integer> getYears() {
        return years;
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
