package com.onyshkiv.finance.repository;

import com.onyshkiv.finance.model.entity.MonobankAuth;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface MonobankAuthRepository extends JpaRepository<MonobankAuth, UUID> {
    Optional<MonobankAuth> findByRequestId(String requestId);
}
