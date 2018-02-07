package com.darts.mis.domain;

import javax.persistence.*;
import java.util.Set;

@Entity
@Table( name = "account")
@NamedQueries({
        @NamedQuery(name = Account.FULL_BY_ID, query = "select a from Account a left join fetch a.users left join fetch a.subscriptions s left join fetch s.edits where a.id=:id")
})
public class Account {
    public static final String FULL_BY_ID = "account.fullById";
    @Id
    @Column(name = "id", nullable = false)
    private Long id;
    @Basic
    @Column(name = "active", nullable = false)
    private boolean active;
    @OneToMany(mappedBy = "account")
    private Set<Subscription> subscriptions;
    @OneToMany(mappedBy = "account")
    private Set<User> users;

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

    public Set<Subscription> getSubscriptions() {
        return subscriptions;
    }

    public void setSubscriptions(Set<Subscription> subscriptions) {
        this.subscriptions = subscriptions;
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
                ", active=" + active +
                '}';
    }
}