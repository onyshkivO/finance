package com.onyshkiv.finance.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "transfer")
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class Transfer {

    @Id
    private UUID id;

    @Column(name="from_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal fromAmount;

    @Column(name="to_amount",nullable = false, precision = 19, scale = 2)
    private BigDecimal toAmount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_cashbox_id", nullable = false, foreignKey = @ForeignKey(name = "fk_transfer_from_cashbox"))
    private Cashbox fromCashbox;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_cashbox_id", nullable = false, foreignKey = @ForeignKey(name = "fk_transfer_to_cashbox"))
    private Cashbox toCashbox;

    @Column(nullable = false)
    private LocalDate date;

    private String description;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    @Column(name = "deleted_at")
    private OffsetDateTime deletedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = OffsetDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = OffsetDateTime.now();
    }
}
