package com.onyshkiv.finance.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "category_mcc")
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class CategoryMcc {

    @Id
    private UUID id;

    @Column(name = "category_id")
    private UUID categoryId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Enumerated
    @Column(name = "type", nullable = false)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    private TransactionType type;

    @Column(name = "mcc_code", nullable = false)
    private Integer mccCode;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CategoryMcc that = (CategoryMcc) o;
        return Objects.equals(categoryId, that.categoryId) && Objects.equals(userId, that.userId) && type == that.type && Objects.equals(mccCode, that.mccCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(categoryId, userId, type, mccCode);
    }
}