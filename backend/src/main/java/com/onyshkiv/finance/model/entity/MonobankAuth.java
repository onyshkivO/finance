package com.onyshkiv.finance.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "monobank_auth")
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class MonobankAuth {

    @Id
    @Column(name = "request_id", nullable = false)
    private String requestId;

    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "accept_url", nullable = false)
    private String acceptUrl;

    @Column(name = "activated", nullable = false)
    private boolean activated = false;

    @OneToMany(mappedBy = "requestId", cascade = CascadeType.ALL)
    private List<MonobankAccount> accounts;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
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
