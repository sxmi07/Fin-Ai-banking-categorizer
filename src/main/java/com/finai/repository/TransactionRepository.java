package com.finai.repository;

import com.finai.model.Transaction;
import com.finai.model.TransactionCategory;
import com.finai.model.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Spring Data JPA Repository for Transaction persistence and query operations.
 * Conforms to Section 9 of the project specification.
 */
@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    /**
     * Search transactions across description and merchant name, with optional category, type, and date filters.
     */
    @Query("SELECT t FROM Transaction t WHERE " +
           "(:search IS NULL OR LOWER(t.rawDescription) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "                  OR LOWER(t.merchantName) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
           "(:category IS NULL OR t.category = :category) AND " +
           "(:type IS NULL OR t.transactionType = :type) AND " +
           "(:startDate IS NULL OR t.transactionDate >= :startDate) AND " +
           "(:endDate IS NULL OR t.transactionDate <= :endDate)")
    Page<Transaction> findWithFilters(
            @Param("search") String search,
            @Param("category") TransactionCategory category,
            @Param("type") TransactionType type,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            Pageable pageable);

    List<Transaction> findByCategory(TransactionCategory category);

    List<Transaction> findByTransactionType(TransactionType transactionType);

    List<Transaction> findByTransactionDateBetween(LocalDate startDate, LocalDate endDate);

    long countByCategory(TransactionCategory category);

    long countByManuallyEditedTrue();

    long countByCategoryNot(TransactionCategory category);

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.transactionType = :type")
    BigDecimal sumAmountByType(@Param("type") TransactionType type);

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.transactionType = :type " +
           "AND t.transactionDate BETWEEN :startDate AND :endDate")
    BigDecimal sumAmountByTypeAndDateBetween(
            @Param("type") TransactionType type,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    @Query("SELECT t.category, SUM(t.amount), COUNT(t) FROM Transaction t " +
           "WHERE t.transactionType = 'DEBIT' GROUP BY t.category")
    List<Object[]> findExpenseDistributionByCategory();

    List<Transaction> findTop10ByOrderByTransactionDateDescCreatedAtDesc();
}
