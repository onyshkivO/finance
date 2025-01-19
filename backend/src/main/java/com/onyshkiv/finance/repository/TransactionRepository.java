package com.onyshkiv.finance.repository;

import com.onyshkiv.finance.model.entity.Category;
import com.onyshkiv.finance.model.entity.Transaction;
import com.onyshkiv.finance.model.entity.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TransactionRepository extends JpaRepository<Transaction, UUID> {
    List<Transaction> findAllByUserIdAndType(UUID userId, TransactionType transactionType);

    int deleteByIdAndUserId(UUID id, UUID userId);

    @Modifying
    @Query("UPDATE Transaction t SET t.category.id = :targetCategoryId WHERE t.category.id = :sourceCategoryId")
    void moveTransactionsToAnotherCategory(@Param("sourceCategoryId") UUID sourceCategoryId, @Param("targetCategoryId") UUID targetCategoryId);

}
