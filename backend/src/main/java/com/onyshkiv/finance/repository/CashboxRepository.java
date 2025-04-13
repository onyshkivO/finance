package com.onyshkiv.finance.repository;

import com.onyshkiv.finance.model.entity.Cashbox;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CashboxRepository extends JpaRepository<Cashbox, UUID> {

    @Query("FROM Cashbox c " +
            "WHERE c.userId = :userId " +
            "AND c.deletedAt is NULL")
    List<Cashbox> findAllUserCashboxes(@Param("userId") UUID userId);

    Optional<Cashbox> findByUserIdAndName(UUID userId, String name);

}
