package com.darts.mis.domain;

import javax.persistence.*;
import java.math.BigDecimal;

@Entity
@Table( name = "invoice_line")
public class InvoiceLine {
    @Id
    @Column(name = "id", nullable = false)
    private Long id;
    @Basic
    @Column(name = "amount_tax_excluded")
    private BigDecimal amount;
    @Basic
    @Column(name = "additional_amount_tax_excluded")
    private BigDecimal additonalAmount;
    @ManyToOne
    @JoinColumn(name = "subscription_edit_fk")
    private SubscriptionEdit subscriptionEdit;
    @ManyToOne
    @JoinColumn(name = "service_fk")
    private Service service;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public BigDecimal getAdditonalAmount() {
        return additonalAmount;
    }

    public void setAdditonalAmount(BigDecimal additonalAmount) {
        this.additonalAmount = additonalAmount;
    }

    public SubscriptionEdit getSubscriptionEdit() {
        return subscriptionEdit;
    }

    public void setSubscriptionEdit(SubscriptionEdit subscriptionEdit) {
        this.subscriptionEdit = subscriptionEdit;
    }

    public Service getService() {
        return service;
    }

    public void setService(Service service) {
        this.service = service;
    }
}
