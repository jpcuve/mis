package com.darts.mis.domain;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table( name = "usr")
@NamedQueries({
        @NamedQuery(name = User.USER_ALL_IDS, query = "select u.id from User u"),
        @NamedQuery(name = User.USER_IDS_BY_ACCOUNT, query = "select u.id from User u where u.account.id = :id")
})
public class User {
    public static final String USER_ALL_IDS = "user.allIds";
    public static final String USER_IDS_BY_ACCOUNT = "user.idsByAccount";
    @Id
    @Column(name = "id", nullable = false)
    private Long id;
    @Basic
    @Column(name = "deleted")
    private LocalDateTime deleted;
    @Basic
    @Column(name = "first_login")
    private LocalDateTime firstLogin;
    @Basic
    @Column(name = "last_login")
    private LocalDateTime lastLogin;
    @ManyToOne
    @JoinColumn(name = "account_fk")
    private Account account;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getDeleted() {
        return deleted;
    }

    public void setDeleted(LocalDateTime deleted) {
        this.deleted = deleted;
    }

    public LocalDateTime getFirstLogin() {
        return firstLogin;
    }

    public void setFirstLogin(LocalDateTime firstLogin) {
        this.firstLogin = firstLogin;
    }

    public LocalDateTime getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(LocalDateTime lastLogin) {
        this.lastLogin = lastLogin;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }
}
