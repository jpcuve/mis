package com.darts.mis.domain;

import com.darts.mis.LocalDateRange;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

import static java.time.temporal.ChronoUnit.DAYS;

@Entity
@NamedQueries(
        @NamedQuery(name = SubscriptionEdit.SUBSCRIPTION_EDIT_ALL_CURRENCIES, query = "select distinct se.currency from SubscriptionEdit se")
)
@Table( name = "subscription_edit")
public class SubscriptionEdit {
    public static final String SUBSCRIPTION_EDIT_ALL_CURRENCIES = "subscriptionEdit.allCurrencies";
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
    @Column(name = "yearly")
    private boolean yearlyPrice;
    @Basic
    @Column(name = "adjustment")
    private BigDecimal adjustment;
    @Basic
    @Column(name = "adjustment_application")
    private int adjustmentApplication;
    @Basic
    @Column(name = "currency_fk")
    private String currency;
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
    @ManyToMany
    @JoinTable(
            name = "history_query_subscription",
            joinColumns = @JoinColumn(name = "subscription_edit_fk", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "history_query_fk", referencedColumnName = "id")
    )
    private Set<HistoryQuery> historyQueries;
    @Transient
    private LocalDateRange range;

    public LocalDateRange getRange() {
        if (range == null){
            range = new LocalDateRange(from, to);
        }
        return range;
    }

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

    public boolean isYearlyPrice() {
        return yearlyPrice;
    }

    public void setYearlyPrice(boolean yearlyPrice) {
        this.yearlyPrice = yearlyPrice;
    }

    public BigDecimal getAdjustment() {
        return adjustment;
    }

    public void setAdjustment(BigDecimal adjustment) {
        this.adjustment = adjustment;
    }

    public int getAdjustmentApplication() {
        return adjustmentApplication;
    }

    public void setAdjustmentApplication(int adjustmentApplication) {
        this.adjustmentApplication = adjustmentApplication;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
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

    public Set<HistoryQuery> getHistoryQueries() {
        return historyQueries;
    }

    public void setHistoryQueries(Set<HistoryQuery> historyQueries) {
        this.historyQueries = historyQueries;
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
