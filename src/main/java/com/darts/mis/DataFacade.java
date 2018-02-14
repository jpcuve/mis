package com.darts.mis;

import com.darts.mis.domain.Account;
import com.darts.mis.domain.Domain;
import com.darts.mis.domain.Subscription;
import com.darts.mis.domain.SubscriptionEdit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.io.Serializable;
import java.util.*;

/**
 * Created by jpc on 31-05-17.
 */
@Repository
@Transactional
public class DataFacade {
    private static final Logger LOGGER = LoggerFactory.getLogger(DataFacade.class);
    @PersistenceContext
    private EntityManager em;

    public <T, ID extends Serializable> Optional<T> findOne(Class<T> clazz, ID pk){
        Objects.requireNonNull(pk);
        return Optional.of(em.find(clazz, pk));
    }

    public Optional<Account> findAccountById(long id){
        return em.createNamedQuery(Account.FULL_BY_ID, Account.class)
                .setParameter("id", id)
                .getResultList()
                .stream()
                .findFirst();
    }

    public Optional<Subscription> findSubscriptionById(long id){
        return em.createNamedQuery(Subscription.FULL_BY_ID, Subscription.class)
                .setParameter("id", id)
                .getResultList()
                .stream()
                .findFirst();
    }

    public List<Long> findAllSubscriptionIds(){
        return em.createNamedQuery(Subscription.SUBSCRIPTION_ALL_IDS, Long.class).getResultList();
    }

    public List<Subscription> findAllSubscriptions(){
        return em.createNamedQuery(Subscription.SUBSCRIPTION_ALL, Subscription.class).getResultList();
    }

    public List<Account> findAllAccounts(){
        return em.createNamedQuery(Account.ACCOUNT_ALL, Account.class).getResultList();
    }

    public List<String> findAllCurrencies(){
        return em.createNamedQuery(SubscriptionEdit.SUBSCRIPTION_EDIT_ALL_CURRENCIES, String.class).getResultList();
    }

    public Map<Long, Map<Domain, Long>> countSubscriptionQueriesByDomain(){
        final Map<Long, Map<Domain, Long>> ret = new HashMap<>();
        em.createNamedQuery(Subscription.SUBSCRIPTION_COUNT_QUERIES_BY_DOMAIN, Object[].class)
                .getResultList()
                .forEach(os -> {
                    final Long subscriptionId = (Long) os[0];
                    final Domain domain = (Domain) os[1];
                    final Long queryCount = (Long) os[2];
                    if (domain != null){
                        ret.computeIfAbsent(subscriptionId, id -> new EnumMap<>(Domain.class)).put(domain, queryCount);
                    }
                });
        return ret;
    }
}
