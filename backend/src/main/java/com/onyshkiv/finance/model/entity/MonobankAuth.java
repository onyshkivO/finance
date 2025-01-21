package com.onyshkiv.finance.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "monobank_auth")
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class MonobankAuth {

    @Id
    private UUID id;

    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "token_request_id", nullable = false, length = 255)
    private String tokenRequestId;

    @Column(name = "accept_url", nullable = false)
    private String acceptUrl;

    @Column(name = "is_activated", nullable = false)
    private boolean isActivated = false;

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
