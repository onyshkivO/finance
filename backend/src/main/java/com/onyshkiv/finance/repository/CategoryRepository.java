package com.onyshkiv.finance.repository;

import com.onyshkiv.finance.model.entity.Category;
import com.onyshkiv.finance.model.entity.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CategoryRepository extends JpaRepository<Category, UUID> {
    List<Category> findAllByUserIdAndType(UUID userId, TransactionType transactionType);

    int deleteByIdAndUserId(UUID id, UUID userId);
}
