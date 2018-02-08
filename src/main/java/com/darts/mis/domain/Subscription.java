package com.darts.mis.domain;

import com.darts.mis.Position;
import com.darts.mis.Schedule;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.darts.mis.domain.SubscriptionEditOperation.REM;

@Entity
@Table( name = "subscription")
@NamedQueries({
        @NamedQuery(name = Subscription.FULL_BY_ID, query = "select s from Subscription s left join fetch s.edits left join fetch s.services where s.id=:id")
})
public class Subscription {
    public static final String FULL_BY_ID = "subscription.fullById";
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
    @OneToMany(mappedBy = "subscription")
    private Set<Service> services;
    @Transient
    private Optional<SubscriptionEdit> current;
    @Transient
    private Schedule revenue;

    public Optional<SubscriptionEdit> findOnDay(LocalDate day, boolean extend){
        final List<SubscriptionEdit> list = this.edits.stream()
                .sorted(Comparator.comparing(SubscriptionEdit::getFrom))
                .filter(e -> !day.isBefore(e.getFrom()) && e.getOperation() != REM && (day.isBefore(e.getTo()) || extend))
                .collect(Collectors.toList());
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(list.size() - 1));
    }

    public Optional<SubscriptionEdit> getCurrent(){
        if (current == null){
            current = findOnDay(LocalDate.now(), true);
        }
        return current;
    }

    private Schedule computeRevenue(){
        final Schedule schedule = new Schedule();
        final List<SubscriptionEdit> list = this.edits.stream().sorted(Comparator.comparing(SubscriptionEdit::getFrom)).collect(Collectors.toList());
        SubscriptionEdit last = null;
        for (final SubscriptionEdit subscriptionEdit: list){
            if (subscriptionEdit.getOperation() != REM){
                if (subscriptionEdit.getPrice().signum() > 0 && subscriptionEdit.getTo().isAfter(subscriptionEdit.getFrom())){
                    final Position amount = Position.of(subscriptionEdit.getCurrency(), subscriptionEdit.getPrice());
                    schedule.add(new Schedule(subscriptionEdit.getFrom(), subscriptionEdit.getTo(), amount));
                }
            }
            last = subscriptionEdit;
        }
        this.services.forEach(service -> {
            BigDecimal amount = service.getPrice();
            if (service.getAdjustment() != null){
                amount = amount.add(service.getAdjustment());
            }
            schedule.add(new Schedule(service.getWhen(), Position.of(service.getCurrency(), amount)));
        });
        schedule.normalize();
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
