package com.darts.mis.domain;

import com.darts.mis.Schedule;

import javax.persistence.*;
import java.util.Set;

@Entity
@Table( name = "account")
@NamedQueries({
        @NamedQuery(name = Account.FULL_BY_IDS, query = "select distinct a from Account a left join fetch a.users left join fetch a.subscriptions s left join fetch s.edits left join fetch s.services left join fetch a.invoices i left join fetch i.lines il where a.id in (:ids)"),
        @NamedQuery(name = Account.ACCOUNT_ALL, query = "select distinct a from Account a left join fetch a.users left join fetch a.subscriptions s left join fetch s.edits left join fetch s.services left join fetch a.invoices i left join fetch i.lines il"),
})
public class Account {
    public static final String FULL_BY_IDS = "account.fullByIds";
    public static final String ACCOUNT_ALL = "account.all";
    @Id
    @Column(name = "id", nullable = false)
    private Long id;
    @Basic
    @Column(name = "name", nullable = false)
    private String name;
    @Basic
    @Column(name = "invoicing_country")
    private String country;
    @Enumerated(EnumType.STRING)
    @Column(name = "nature")
    private AccountNature nature;
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private AccountStatus status;
    @Basic
    @Column(name = "active", nullable = false)
    private boolean active;
    @OneToMany(mappedBy = "account")
    private Set<Subscription> subscriptions;
    @OneToMany(mappedBy = "account")
    private Set<Invoice> invoices;
    @OneToMany(mappedBy = "account")
    private Set<User> users;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public AccountNature getNature() {
        return nature;
    }

    public void setNature(AccountNature nature) {
        this.nature = nature;
    }

    public AccountStatus getStatus() {
        return status;
    }

    public void setStatus(AccountStatus status) {
        this.status = status;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Set<Subscription> getSubscriptions() {
        return subscriptions;
    }

    public void setSubscriptions(Set<Subscription> subscriptions) {
        this.subscriptions = subscriptions;
    }

    public Set<Invoice> getInvoices() {
        return invoices;
    }

    public void setInvoices(Set<Invoice> invoices) {
        this.invoices = invoices;
    }

    public Set<User> getUsers() {
        return users;
    }

    public void setUsers(Set<User> users) {
        this.users = users;
    }

    @Override
    public String toString() {
        return "Account{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", active=" + active +
                '}';
    }
}
