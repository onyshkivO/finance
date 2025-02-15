package com.onyshkiv.finance.repository;

import com.onyshkiv.finance.model.entity.CategoryMcc;
import com.onyshkiv.finance.model.entity.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Repository
public interface CategoryMccRepository extends JpaRepository<CategoryMcc, UUID> {

    @Query("SELECT cm.categoryId from CategoryMcc cm WHERE cm.mccCode = :mcc AND  cm.userId = :userId AND cm.type = :type")
    Optional<UUID> getCategoryIdByMccAndUserIdAndType(Integer mcc, UUID userId, TransactionType type);

    @Query("SELECT cm.mccCode FROM CategoryMcc cm WHERE cm.userId = :userId AND cm.type = :type AND cm.mccCode IN :mccCodes")
    List<Integer> findDuplicateMccCodes(@Param("userId") UUID userId, @Param("type") TransactionType type, @Param("mccCodes") Set<Integer> mccCodes);

    @Query("SELECT cm.mccCode FROM CategoryMcc cm WHERE cm.userId = :userId AND cm.type = :type AND cm.mccCode IN :mccCodes AND cm.categoryId <> :categoryId")
    List<Integer> findDuplicateMccCodes(
            @Param("userId") UUID userId,
            @Param("type") TransactionType type,
            @Param("mccCodes") Set<Integer> mccCodes,
            @Param("categoryId") UUID categoryId
    );

}
