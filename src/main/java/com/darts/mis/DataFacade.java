package com.darts.mis;

import com.darts.mis.domain.Account;
import com.darts.mis.domain.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

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
}
