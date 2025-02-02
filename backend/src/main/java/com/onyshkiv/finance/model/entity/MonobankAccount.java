package com.onyshkiv.finance.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "monobank_account")
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class MonobankAccount {
    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "client_id", nullable = false)
    private String clientId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "account_id", nullable = false)
    private String accountId;

    @Column(name = "send_id", nullable = false)
    private String sendId;

    @Column(name = "iban", nullable = false)
    private String iban;

    @Column(name = "currency_code", nullable = false)
    private Integer currencyCode;

    @Enumerated
    @Column(name = "type", nullable = false)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    private MonobankAccountType type;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = OffsetDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = OffsetDateTime.now();
    }
}
