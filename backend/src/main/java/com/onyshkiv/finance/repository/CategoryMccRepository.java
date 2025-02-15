package com.onyshkiv.finance.repository;

import com.onyshkiv.finance.model.entity.CategoryMcc;
import com.onyshkiv.finance.model.entity.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CategoryMccRepository extends JpaRepository<CategoryMcc, UUID> {

    @Query("SELECT cm.categoryId from CategoryMcc cm WHERE cm.mccCode = :mcc AND  cm.userId = :userId AND cm.type = :type")
    Optional<UUID> getCategoryIdByMccAndUserIdAndType(Integer mcc, UUID userId, TransactionType type);
}
