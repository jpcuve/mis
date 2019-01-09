package com.darts.mis.domain;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.Set;

@Entity
@Table( name = "invoice")
public class Invoice {
    @Id
    @Column(name = "id", nullable = false)
    private Long id;
    @Basic
    @Column(name = "credit_note")
    private boolean creditNote;
    @Basic
    @Column(name = "day_invoice")
    private LocalDate when;
    @ManyToOne
    @JoinColumn(name = "account_fk")
    private Account account;
    @Basic
    @Column(name = "issuer_fk")
    private Long issuer;
    @OneToMany(mappedBy = "invoice")
    private Set<InvoiceLine> lines;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public boolean isCreditNote() {
        return creditNote;
    }

    public void setCreditNote(boolean creditNote) {
        this.creditNote = creditNote;
    }

    public LocalDate getWhen() {
        return when;
    }

    public void setWhen(LocalDate when) {
        this.when = when;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public Long getIssuer() {
        return issuer;
    }

    public void setIssuer(Long issuer) {
        this.issuer = issuer;
    }

    public Set<InvoiceLine> getLines() {
        return lines;
    }

    public void setLines(Set<InvoiceLine> lines) {
        this.lines = lines;
    }
}
