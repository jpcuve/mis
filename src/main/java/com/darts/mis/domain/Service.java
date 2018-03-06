package com.darts.mis.domain;

import com.darts.mis.LocalDateRange;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table( name = "service")
public class Service {
    @Id
    @Column(name = "id", nullable = false)
    private Long id;
    @Basic
    @Column(name = "price")
    private BigDecimal price;
    @Basic
    @Column(name = "currency_fk")
    private String currency;
    @Basic
    @Column(name = "adjustment")
    private BigDecimal adjustment;
    @Basic
    @Column(name = "day_when")
    private LocalDate when;
    @ManyToOne
    @JoinColumn(name = "subscription_fk")
    private Subscription subscription;

    @Transient
    private LocalDateRange range;

    public LocalDateRange getRange() {
        if (range == null){
            range = new LocalDateRange(when, when);
        }
        return range;
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public BigDecimal getAdjustment() {
        return adjustment;
    }

    public void setAdjustment(BigDecimal adjustment) {
        this.adjustment = adjustment;
    }

    public LocalDate getWhen() {
        return when;
    }

    public void setWhen(LocalDate when) {
        this.when = when;
    }

    public Subscription getSubscription() {
        return subscription;
    }

    public void setSubscription(Subscription subscription) {
        this.subscription = subscription;
    }
}
