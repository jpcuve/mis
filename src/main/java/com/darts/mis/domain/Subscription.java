package com.darts.mis.domain;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Table( name = "subscription")
public class Subscription {
    @Id
    @Column(name = "id", nullable = false)
    private Long id;
    @Basic
    @Column(name = "active")
    private boolean active;
    @ElementCollection
    @CollectionTable(name = "subscription_domain", joinColumns = { @JoinColumn(name = "subscription_fk")})
    @Enumerated(EnumType.STRING)
    @Column(name = "domain")
    private Set<Domain> domains;
    @ElementCollection
    @CollectionTable(name = "subscription_area", joinColumns = { @JoinColumn(name = "subscription_fk")})
    @Enumerated(EnumType.STRING)
    @Column(name = "area")
    private Set<Area> areas;
    @ManyToOne
    @JoinColumn(name = "account_fk")
    private Account account;
    @OneToMany(mappedBy = "subscription")
    private Set<SubscriptionEdit> edits;
    @Transient
    private Optional<SubscriptionEdit> current;

    public Optional<SubscriptionEdit> findOnDay(LocalDate day, boolean extend){
        final List<SubscriptionEdit> list = this.edits.stream()
                .sorted(Comparator.comparing(SubscriptionEdit::getFrom))
                .filter(e -> !day.isBefore(e.getFrom()) && e.getOperation() != SubscriptionEditOperation.REM && (day.isBefore(e.getTo()) || extend))
                .collect(Collectors.toList());
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(list.size() - 1));
    }

    public Optional<SubscriptionEdit> getCurrent(){
        if (current == null){
            current = findOnDay(LocalDate.now(), true);
        }
        return current;
    }

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
