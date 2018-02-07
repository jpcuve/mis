package com.darts.mis.domain;

import javax.persistence.*;
import java.sql.Timestamp;

@Entity
@Table( name = "history_session")
public class HistorySession {
    @Id
    @Column(name = "id", nullable = false)
    private Long id;
    @Basic
    @Column(name = "created", nullable = false)
    private Timestamp created;
    @ManyToOne
    @JoinColumn(name = "usr_fk")
    private User user;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Timestamp getCreated() {
        return created;
    }

    public void setCreated(Timestamp created) {
        this.created = created;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
