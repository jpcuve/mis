package com.darts.mis.domain;

import javax.persistence.*;
import java.sql.Timestamp;

@Entity
@Table( name = "history_query")
public class HistoryQuery {
    @Id
    @Column(name = "id", nullable = false)
    private Long id;
    @Enumerated(EnumType.STRING)
    @Column(name = "domain")
    private Domain domain;
    @Basic
    @Column(name = "executed", nullable = false)
    private Timestamp executed;
    @Basic
    @Column(name = "result_count", nullable = false)
    private int resultCount;
    @ManyToOne
    @JoinColumn(name = "usr_fk")
    private User user;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Domain getDomain() {
        return domain;
    }

    public void setDomain(Domain domain) {
        this.domain = domain;
    }

    public Timestamp getExecuted() {
        return executed;
    }

    public void setExecuted(Timestamp executed) {
        this.executed = executed;
    }

    public int getResultCount() {
        return resultCount;
    }

    public void setResultCount(int resultCount) {
        this.resultCount = resultCount;
    }
}
