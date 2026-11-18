package com.finai.service;

import com.finai.ai.AiCategorizationService;
import com.finai.dto.CategorizationResultDTO;
import com.finai.dto.CategorizeRequestDTO;
import com.finai.dto.CategoryUpdateRequestDTO;
import com.finai.dto.TransactionRequestDTO;
import com.finai.dto.TransactionResponseDTO;
import com.finai.dto.TransactionSummaryDTO;
import com.finai.exception.ResourceNotFoundException;
import com.finai.model.Transaction;
import com.finai.model.TransactionCategory;
import com.finai.model.TransactionType;
import com.finai.repository.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Service managing transaction CRUD, queries, and summary aggregations.
 * Conforms to Section 8, Section 9, and Section 11 of the project specification.
 */
@Service
@Transactional
public class TransactionService {

    private static final Logger log = LoggerFactory.getLogger(TransactionService.class);

    private final TransactionRepository transactionRepository;
    private final AiCategorizationService aiCategorizationService;

    public TransactionService(TransactionRepository transactionRepository,
                              AiCategorizationService aiCategorizationService) {
        this.transactionRepository = transactionRepository;
        this.aiCategorizationService = aiCategorizationService;
    }

    /**
     * Categorizes transaction descriptions using AI (with heuristic fallback).
     * Optionally persists categorized transactions into H2 database.
     */
    public List<CategorizationResultDTO> categorizeTransactions(CategorizeRequestDTO request) {
        List<CategorizationResultDTO> results = aiCategorizationService.categorizeBatch(request.getDescriptions());

        if (request.isSaveToDatabase() && !results.isEmpty()) {
            List<Transaction> toSave = new ArrayList<>();
            LocalDate txDate = request.getDefaultDate() != null ? request.getDefaultDate() : LocalDate.now();
            BigDecimal amount = request.getDefaultAmount() != null ? request.getDefaultAmount() : BigDecimal.ZERO;

            for (CategorizationResultDTO res : results) {
                Transaction tx = new Transaction();
                tx.setRawDescription(res.getOriginalDescription());
                tx.setMerchantName(res.getMerchantName());
                tx.setAmount(amount);
                tx.setTransactionDate(txDate);
                tx.setTransactionType(res.getTransactionType());
                tx.setCategory(res.getCategory());
                tx.setSubcategory(res.getSubcategory());
                tx.setAiConfidence(res.getConfidence());
                tx.setAiExplanation(res.getExplanation());
                tx.setManuallyEdited(false);
                toSave.add(tx);
            }
            transactionRepository.saveAll(toSave);
            log.info("Persisted {} AI-categorized transactions to database", toSave.size());
        }

        return results;
    }

    /**
     * Retrieve paginated and filtered transactions.
     */
    @Transactional(readOnly = true)
    public Page<TransactionResponseDTO> getTransactions(
            String search,
            TransactionCategory category,
            TransactionType type,
            LocalDate startDate,
            LocalDate endDate,
            Pageable pageable) {

        String sanitizedSearch = (search != null && !search.trim().isEmpty()) ? search.trim() : null;
        Page<Transaction> page = transactionRepository.findWithFilters(sanitizedSearch, category, type, startDate, endDate, pageable);
        return page.map(TransactionResponseDTO::fromEntity);
    }

    /**
     * Retrieve a single transaction by ID.
     */
    @Transactional(readOnly = true)
    public TransactionResponseDTO getTransactionById(Long id) {
        Transaction tx = transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction", "id", id));
        return TransactionResponseDTO.fromEntity(tx);
    }

    /**
     * Create a new transaction manually.
     */
    public TransactionResponseDTO createTransaction(TransactionRequestDTO request) {
        Transaction tx = new Transaction();
        tx.setRawDescription(request.getDescription());
        tx.setMerchantName(extractSimpleMerchant(request.getDescription()));
        tx.setAmount(request.getAmount());
        tx.setTransactionDate(request.getDate() != null ? request.getDate() : LocalDate.now());
        tx.setTransactionType(request.getType() != null ? request.getType() : TransactionType.DEBIT);
        tx.setCategory(TransactionCategory.OTHER);
        tx.setSubcategory("Pending Categorization");
        tx.setAiConfidence(0.0);
        tx.setAiExplanation("Manual entry, awaiting categorization");
        tx.setManuallyEdited(false);

        Transaction saved = transactionRepository.save(tx);
        log.info("Created transaction id: {}", saved.getId());
        return TransactionResponseDTO.fromEntity(saved);
    }

    /**
     * Update transaction category manually (Section 11).
     */
    public TransactionResponseDTO updateCategory(Long id, CategoryUpdateRequestDTO request) {
        Transaction tx = transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction", "id", id));

        TransactionCategory newCategory = TransactionCategory.fromString(request.getCategory());
        tx.setCategory(newCategory);
        if (request.getSubcategory() != null && !request.getSubcategory().isBlank()) {
            tx.setSubcategory(request.getSubcategory().trim());
        }
        tx.setManuallyEdited(true);
        tx.setAiExplanation("Category manually adjusted by user to: " + newCategory.getDisplayName());

        Transaction updated = transactionRepository.save(tx);
        log.info("Manually updated transaction id: {} to category: {}", id, newCategory);
        return TransactionResponseDTO.fromEntity(updated);
    }

    /**
     * Delete a transaction by ID.
     */
    public void deleteTransaction(Long id) {
        if (!transactionRepository.existsById(id)) {
            throw new ResourceNotFoundException("Transaction", "id", id);
        }
        transactionRepository.deleteById(id);
        log.info("Deleted transaction id: {}", id);
    }

    /**
     * Generate financial summary and spending statistics (Section 5A & Section 8).
     */
    @Transactional(readOnly = true)
    public TransactionSummaryDTO getSummary(Integer year, Integer month) {
        TransactionSummaryDTO summary = new TransactionSummaryDTO();

        LocalDate startDate = null;
        LocalDate endDate = null;
        if (year != null && month != null) {
            YearMonth ym = YearMonth.of(year, month);
            startDate = ym.atDay(1);
            endDate = ym.atEndOfMonth();
        }

        BigDecimal totalExpenses;
        BigDecimal totalIncome;

        if (startDate != null) {
            totalExpenses = transactionRepository.sumAmountByTypeAndDateBetween(TransactionType.DEBIT, startDate, endDate);
            totalIncome = transactionRepository.sumAmountByTypeAndDateBetween(TransactionType.CREDIT, startDate, endDate);
        } else {
            totalExpenses = transactionRepository.sumAmountByType(TransactionType.DEBIT);
            totalIncome = transactionRepository.sumAmountByType(TransactionType.CREDIT);
        }

        summary.setTotalExpenses(totalExpenses != null ? totalExpenses : BigDecimal.ZERO);
        summary.setTotalIncome(totalIncome != null ? totalIncome : BigDecimal.ZERO);
        summary.setNetSavings(summary.getTotalIncome().subtract(summary.getTotalExpenses()));

        long totalCount = transactionRepository.count();
        long uncategorized = transactionRepository.countByCategory(TransactionCategory.OTHER);
        long categorized = transactionRepository.countByCategoryNot(TransactionCategory.OTHER);
        long manuallyEdited = transactionRepository.countByManuallyEditedTrue();

        summary.setTotalTransactions(totalCount);
        summary.setUncategorizedCount(uncategorized);
        summary.setAiCategorizedCount(categorized);
        summary.setManuallyEditedCount(manuallyEdited);

        // Category breakdown
        List<Object[]> distribution = transactionRepository.findExpenseDistributionByCategory();
        Map<String, BigDecimal> categoryExpenses = new LinkedHashMap<>();
        Map<String, Long> categoryCounts = new LinkedHashMap<>();
        String topCategory = "N/A";
        BigDecimal topAmount = BigDecimal.ZERO;
        long totalDebitCount = 0;

        for (Object[] row : distribution) {
            TransactionCategory cat = (TransactionCategory) row[0];
            BigDecimal sum = (BigDecimal) row[1];
            Long count = (Long) row[2];

            categoryExpenses.put(cat.getDisplayName(), sum);
            categoryCounts.put(cat.getDisplayName(), count);
            totalDebitCount += count;

            if (sum.compareTo(topAmount) > 0) {
                topAmount = sum;
                topCategory = cat.getDisplayName();
            }
        }

        summary.setCategoryExpenses(categoryExpenses);
        summary.setCategoryCounts(categoryCounts);
        summary.setTopSpendingCategory(topCategory);
        summary.setTopCategoryAmount(topAmount);

        if (totalDebitCount > 0 && summary.getTotalExpenses().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal avg = summary.getTotalExpenses().divide(BigDecimal.valueOf(totalDebitCount), 2, RoundingMode.HALF_UP);
            summary.setAverageExpenseAmount(avg);
        }

        return summary;
    }

    /**
     * Batch save transactions (used for demo data loading and CSV import).
     */
    public List<Transaction> saveAll(List<Transaction> transactions) {
        return transactionRepository.saveAll(transactions);
    }

    @Transactional(readOnly = true)
    public long count() {
        return transactionRepository.count();
    }

    private String extractSimpleMerchant(String rawDescription) {
        if (rawDescription == null || rawDescription.isBlank()) {
            return "Unknown";
        }
        String clean = rawDescription.trim();
        if (clean.contains("/")) {
            String[] parts = clean.split("/");
            return parts[parts.length - 1].trim();
        }
        return clean;
    }
}
