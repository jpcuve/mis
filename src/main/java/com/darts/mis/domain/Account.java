package com.darts.mis.domain;

import com.darts.mis.Schedule;

import javax.persistence.*;
import java.util.Set;

@Entity
@Table( name = "account")
@NamedQueries({
        @NamedQuery(name = Account.FULL_BY_ID, query = "select a from Account a left join fetch a.users left join fetch a.subscriptions s left join fetch s.edits left join fetch s.services where a.id=:id"),
        @NamedQuery(name = Account.ACCOUNT_ALL, query = "select distinct a from Account a left join fetch a.users left join fetch a.subscriptions s left join fetch s.edits left join fetch s.services")
})
public class Account {
    public static final String FULL_BY_ID = "account.fullById";
    public static final String ACCOUNT_ALL = "account.all";
    @Id
    @Column(name = "id", nullable = false)
    private Long id;
    @Basic
    @Column(name = "name", nullable = false)
    private String name;
    @Basic
    @Column(name = "active", nullable = false)
    private boolean active;
    @OneToMany(mappedBy = "account")
    private Set<Subscription> subscriptions;
    @OneToMany(mappedBy = "account")
    private Set<User> users;
    @Transient
    private Schedule revenue;

    private Schedule computeRevenue(){
        final Schedule schedule = new Schedule();
        for (final Subscription subscription: subscriptions){
            schedule.add(subscription.getRevenue());
        }
        return schedule;
    }

    public Schedule getRevenue(){
        if (revenue == null){
            revenue = computeRevenue();
        }
        return revenue;
    }

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
                ", name='" + name + '\'' +
                ", active=" + active +
                '}';
    }
}
