package com.darts.mis.domain;

import javax.persistence.*;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Set;

@Entity
@Table( name = "subscription")
@NamedQueries({
        @NamedQuery(name = Subscription.FULL_BY_IDS, query = "select s from Subscription s left join fetch s.edits left join fetch s.services where s.id in (:ids)"),
        @NamedQuery(name = Subscription.SUBSCRIPTION_ALL_IDS, query = "select s.id from Subscription s order by s.id"),
        @NamedQuery(name = Subscription.SUBSCRIPTION_ALL, query = "select distinct s from Subscription s left join fetch s.edits left join fetch s.services"),
        @NamedQuery(name = Subscription.SUBSCRIPTION_COUNT_QUERIES_BY_DOMAIN, query = "select s.id, hq.domain, count(hq.id) from Subscription s join s.edits se join se.historyQueries hq group by s.id, hq.domain")
})
public class Subscription {
    public static final String FULL_BY_IDS = "subscription.fullByIds";
    public static final String SUBSCRIPTION_ALL_IDS = "subscription.allIds";
    public static final String SUBSCRIPTION_ALL = "subscription.all";
    public static final String SUBSCRIPTION_COUNT_QUERIES_BY_DOMAIN = "subscription.countQueriesByDomain";
    @Id
    @Column(name = "id", nullable = false)
    private Long id;
    @Basic
    @Column(name = "active")
    private boolean active;
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "subscription_domain", joinColumns = { @JoinColumn(name = "subscription_fk")})
    @Enumerated(EnumType.STRING)
    @Column(name = "domain")
    private Set<Domain> domains;
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "subscription_area", joinColumns = { @JoinColumn(name = "subscription_fk")})
    @Enumerated(EnumType.STRING)
    @Column(name = "area")
    private Set<Area> areas;
    @ManyToOne
    @JoinColumn(name = "account_fk")
    private Account account;
    @OneToMany(mappedBy = "subscription")
    private Set<SubscriptionEdit> edits;
    @OneToMany(mappedBy = "subscription")
    private Set<Service> services;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Set<Domain> getDomains() {
        return domains;
    }

    public void setDomains(Set<Domain> domains) {
        this.domains = domains;
    }

    public Set<Area> getAreas() {
        return areas;
    }

    public void setAreas(Set<Area> areas) {
        this.areas = areas;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public Set<SubscriptionEdit> getEdits() {
        return edits;
    }

    public void setEdits(Set<SubscriptionEdit> edits) {
        this.edits = edits;
    }

    public Set<Service> getServices() {
        return services;
    }

    public void setServices(Set<Service> services) {
        this.services = services;
    }

    @Override
    public String toString() {
        return "Subscription{" +
                "id=" + id +
                ", active=" + active +
                ", domains=" + domains +
                ", areas=" + areas +
                '}';
    }
}
