package com.darts.mis.domain;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table( name = "subscription_edit")
public class SubscriptionEdit {
    @Id
    @Column(name = "id", nullable = false)
    private Long id;
    @Enumerated(EnumType.STRING)
    @Column(name = "level")
    private SubscriptionLevel level;
    @Basic
    @Column(name = "price")
    private BigDecimal price;
    @Basic
    @Column(name = "day_from")
    private LocalDate from;
    @Basic
    @Column(name = "day_to")
    private LocalDate to;
    @Enumerated(EnumType.STRING)
    @Column(name = "operation")
    private SubscriptionEditOperation operation;
    @ManyToOne
    @JoinColumn(name = "subscription_fk")
    private Subscription subscription;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public SubscriptionLevel getLevel() {
        return level;
    }

    public void setLevel(SubscriptionLevel level) {
        this.level = level;
    }

    public LocalDate getFrom() {
        return from;
    }

    public void setFrom(LocalDate from) {
        this.from = from;
    }

    public LocalDate getTo() {
        return to;
    }

    public void setTo(LocalDate to) {
        this.to = to;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public SubscriptionEditOperation getOperation() {
        return operation;
    }

    public void setOperation(SubscriptionEditOperation operation) {
        this.operation = operation;
    }

    public Subscription getSubscription() {
        return subscription;
    }

    public void setSubscription(Subscription subscription) {
        this.subscription = subscription;
    }

    @Override
    public String toString() {
        return "SubscriptionEdit{" +
                "id=" + id +
                ", level=" + level +
                ", price=" + price +
                ", from=" + from +
                ", to=" + to +
                ", operation=" + operation +
                '}';
    }
}
