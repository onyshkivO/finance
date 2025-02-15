package com.onyshkiv.finance.repository;

import com.onyshkiv.finance.model.entity.Category;
import com.onyshkiv.finance.model.entity.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CategoryRepository extends JpaRepository<Category, UUID> {
    List<Category> findAllByUserIdAndType(UUID userId, TransactionType transactionType);

    @Modifying
    @Query(value = "UPDATE transaction " +
            "SET category_id = CASE WHEN :categoryIdTo IS NOT NULL THEN :categoryIdTo ELSE NULL END " +
            "WHERE (category_id = :categoryIdFrom OR (:categoryIdFrom IS NULL AND category_id IS NULL))" +
            "AND type = CAST(:type AS type_enum)",
            nativeQuery = true)
    void transferCategoryTransactions(@Param("categoryIdFrom") UUID categoryIdFrom,
                                      @Param("categoryIdTo") UUID categoryIdTo,
                                      @Param("type") String type);

    Optional<Category> getByUserIdAndNameAndType(UUID userId, String name, TransactionType transactionType);

}
