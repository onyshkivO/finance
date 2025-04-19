package com.onyshkiv.finance.repository;

import com.onyshkiv.finance.model.entity.Transaction;
import com.onyshkiv.finance.model.entity.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface TransactionRepository extends JpaRepository<Transaction, UUID> {
    List<Transaction> findAllByUserIdAndType(UUID userId, TransactionType transactionType);

    List<Transaction> findAllByUserId(UUID userId);

    @Query("SELECT t.type, COALESCE(SUM(t.amount),0) FROM Transaction t " +
            "WHERE t.userId = :userId " +
            "AND t.transactionDate BETWEEN :from AND :to " +
            "GROUP BY t.type")
    List<Object[]> getBalanceStats(@Param("userId") UUID userId,
                                   @Param("from") LocalDate from,
                                   @Param("to") LocalDate to);


    @Query("SELECT t.type, COALESCE(c.name, 'Other') as category_name, c.icon as category_icon, COALESCE(SUM(t.amount), 0) as totalAmount " +
            "FROM Transaction t " +
            "LEFT JOIN Category c ON t.category.id = c.id " +
            "WHERE t.userId = :userId " +
            "AND t.transactionDate BETWEEN :from AND :to " +
            "GROUP BY t.type, category_name, category_icon " +
            "ORDER BY totalAmount DESC")
    List<Object[]> getCategoriesStats(@Param("userId") UUID userId,
                                      @Param("from") LocalDate from,
                                      @Param("to") LocalDate to);

    @Query("SELECT t.type, COALESCE(c.name, 'Other') as cashbox_name, COALESCE(SUM(t.amount), 0) as totalAmount " +
            "FROM Transaction t " +
            "LEFT JOIN Cashbox c ON t.cashbox.id = c.id " +
            "WHERE t.userId = :userId " +
            "AND t.transactionDate BETWEEN :from AND :to " +
            "GROUP BY t.type, cashbox_name " +
            "ORDER BY totalAmount DESC")
    List<Object[]> getCashboxStats(UUID userId, LocalDate from, LocalDate to);


    @Query(value = "SELECT DISTINCT DATE_PART('year', t.transaction_date) AS year " +
            "FROM Transaction t " +
            "WHERE t.user_id = :userId " +
            "ORDER BY year ASC",
            nativeQuery = true)
    List<Integer> getTransactionHistoryPeriods(@Param("userId") UUID userId);

    @Query("SELECT EXTRACT(MONTH FROM t.transactionDate) AS month, t.type, SUM(t.amount) " +
            "FROM Transaction t WHERE EXTRACT(YEAR FROM t.transactionDate) = :year " +
            "AND t.userId = :userId " +
            "GROUP BY month, t.type")
    List<Object[]> sumAmountByYearGroupedByMonthAndType(@Param("year") int year, @Param("userId") UUID userId);

    @Query("SELECT EXTRACT(DAY FROM t.transactionDate) AS day, t.type, SUM(t.amount) " +
            "FROM Transaction t WHERE EXTRACT(YEAR FROM t.transactionDate) = :year " +
            "AND EXTRACT(MONTH FROM t.transactionDate) = :month " +
            "AND t.userId = :userId " +
            "GROUP BY day, t.type")
    List<Object[]> sumAmountByMonthGroupedByDayAndType(@Param("year") int year, @Param("month") int month, @Param("userId") UUID userId);

    @Query("SELECT t FROM Transaction t " +
            "WHERE t.userId = :userId " +
            "AND t.transactionDate BETWEEN :from AND :to " +
            "ORDER BY t.transactionDate DESC")
    List<Transaction> findUserTransactionByDateRange(@Param("userId") UUID userId,
                                                     @Param("from") LocalDate from,
                                                     @Param("to") LocalDate to);

}
