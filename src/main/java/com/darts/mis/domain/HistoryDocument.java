package com.darts.mis.domain;

import javax.persistence.*;
import java.sql.Timestamp;

@Entity
@Table( name = "history_document")
public class HistoryDocument {
    @Id
    @Column(name = "id", nullable = false)
    private Long id;
    @Column(name = "jdocument_fk")
    private Long documentId;
    @Basic
    @Column(name = "executed", nullable = false)
    private Timestamp executed;
    @ManyToOne
    @JoinColumn(name = "usr_fk")
    private User user;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getDocumentId() {
        return documentId;
    }

    public void setDocumentId(Long documentId) {
        this.documentId = documentId;
    }

    public Timestamp getExecuted() {
        return executed;
    }

    public void setExecuted(Timestamp executed) {
        this.executed = executed;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
