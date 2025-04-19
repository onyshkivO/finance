package com.onyshkiv.finance.model.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "transaction")
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@NamedEntityGraph(
        name = "graph.Transaction.category",
        attributeNodes = @NamedAttributeNode(value = "category")
)
public class Transaction {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

    @Enumerated
    @Column(name = "type", nullable = false)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    private TransactionType type;

    @Column(name = "amount", nullable = false)
    private BigDecimal amount;

    @Column(name = "description")
    private String description;

    @Column(name = "transaction_date", nullable = false, columnDefinition = "date")
    private LocalDate transactionDate;

    @Column(name = "base_amount", nullable = false)
    private BigDecimal baseAmount;

    @Enumerated
    @Column(name = "base_currency", nullable = false)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    private Currency baseCurrency;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cashbox_id", foreignKey = @ForeignKey(name = "fk_transaction_cashbox"))
    private Cashbox cashbox;

    @PrePersist
    protected void onCreate() {
        this.createdAt = OffsetDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = OffsetDateTime.now();
    }
}