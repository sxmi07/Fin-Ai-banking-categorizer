package com.finai.service;

import com.finai.ai.AiCategorizationService;
import com.finai.dto.*;
import com.finai.exception.ResourceNotFoundException;
import com.finai.model.Transaction;
import com.finai.model.TransactionCategory;
import com.finai.model.TransactionType;
import com.finai.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Lightweight unit tests for TransactionService.
 * Uses lightweight Java standard dynamic proxy and subclass stubs.
 */
class TransactionServiceTest {

    private TransactionService transactionService;
    private Transaction sampleTx;
    private AtomicBoolean deleteCalled;
    private Transaction savedTx;

    @BeforeEach
    void setUp() {
        deleteCalled = new AtomicBoolean(false);
        sampleTx = new Transaction(
                "UPI/409283/SWIGGY BANGALORE",
                "Swiggy",
                new BigDecimal("450.00"),
                LocalDate.of(2026, 11, 15),
                TransactionType.DEBIT,
                TransactionCategory.FOOD,
                "Food Delivery",
                0.98,
                "UPI food order"
        );
        sampleTx.setId(1L);

        TransactionRepository repoStub = (TransactionRepository) Proxy.newProxyInstance(
                TransactionRepository.class.getClassLoader(),
                new Class<?>[]{TransactionRepository.class},
                (proxy, method, args) -> {
                    String name = method.getName();
                    if ("findById".equals(name)) {
                        Long id = (Long) args[0];
                        return id.equals(1L) ? Optional.of(sampleTx) : Optional.empty();
                    }
                    if ("save".equals(name)) {
                        savedTx = (Transaction) args[0];
                        if (savedTx.getId() == null) savedTx.setId(10L);
                        return savedTx;
                    }
                    if ("existsById".equals(name)) {
                        Long id = (Long) args[0];
                        return id.equals(1L);
                    }
                    if ("deleteById".equals(name)) {
                        deleteCalled.set(true);
                        return null;
                    }
                    if ("sumAmountByType".equals(name)) {
                        TransactionType type = (TransactionType) args[0];
                        return type == TransactionType.DEBIT ? new BigDecimal("5000.00") : new BigDecimal("15000.00");
                    }
                    if ("count".equals(name)) {
                        return 10L;
                    }
                    if ("countByCategory".equals(name)) {
                        return 2L;
                    }
                    if ("countByCategoryNot".equals(name)) {
                        return 8L;
                    }
                    if ("countByManuallyEditedTrue".equals(name)) {
                        return 1L;
                    }
                    if ("findExpenseDistributionByCategory".equals(name)) {
                        List<Object[]> distribution = new ArrayList<>();
                        distribution.add(new Object[]{TransactionCategory.FOOD, new BigDecimal("3000.00"), 5L});
                        distribution.add(new Object[]{TransactionCategory.TRAVEL, new BigDecimal("2000.00"), 3L});
                        return distribution;
                    }
                    return null;
                }
        );

        AiCategorizationService aiServiceStub = new AiCategorizationService(null, null) {
            @Override
            public List<CategorizationResultDTO> categorizeBatch(List<String> descriptions) {
                return List.of(new CategorizationResultDTO(
                        "UPI/409283/SWIGGY", "Swiggy", TransactionCategory.FOOD, "Delivery",
                        TransactionType.DEBIT, 0.98, "Matched Swiggy", "Rule-Based", true
                ));
            }
        };

        transactionService = new TransactionService(repoStub, aiServiceStub);
    }

    @Test
    @DisplayName("Should retrieve transaction by valid ID")
    void testGetTransactionById_Success() {
        TransactionResponseDTO result = transactionService.getTransactionById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Swiggy", result.getMerchantName());
        assertEquals(new BigDecimal("450.00"), result.getAmount());
        assertEquals(TransactionCategory.FOOD, result.getCategory());
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException for invalid ID")
    void testGetTransactionById_NotFound() {
        assertThrows(ResourceNotFoundException.class, () -> transactionService.getTransactionById(999L));
    }

    @Test
    @DisplayName("Should create a manual transaction successfully")
    void testCreateTransaction() {
        TransactionRequestDTO req = new TransactionRequestDTO(
                "BESCOM ELECTRICITY BILL",
                new BigDecimal("1200.00"),
                LocalDate.now(),
                TransactionType.DEBIT
        );

        TransactionResponseDTO created = transactionService.createTransaction(req);

        assertNotNull(created);
        assertEquals(10L, created.getId());
        assertEquals("BESCOM ELECTRICITY BILL", created.getRawDescription());
        assertEquals(new BigDecimal("1200.00"), created.getAmount());
    }

    @Test
    @DisplayName("Should manually update transaction category and set manuallyEdited=true")
    void testUpdateCategory_Success() {
        CategoryUpdateRequestDTO updateReq = new CategoryUpdateRequestDTO("Shopping", "Online Retail");
        TransactionResponseDTO updated = transactionService.updateCategory(1L, updateReq);

        assertNotNull(updated);
        assertEquals(TransactionCategory.SHOPPING, updated.getCategory());
        assertEquals("Online Retail", updated.getSubcategory());
        assertTrue(updated.getManuallyEdited());
    }

    @Test
    @DisplayName("Should delete existing transaction successfully")
    void testDeleteTransaction_Success() {
        assertDoesNotThrow(() -> transactionService.deleteTransaction(1L));
        assertTrue(deleteCalled.get());
    }

    @Test
    @DisplayName("Should calculate financial summary correctly")
    void testGetSummary() {
        TransactionSummaryDTO summary = transactionService.getSummary(null, null);

        assertNotNull(summary);
        assertEquals(new BigDecimal("5000.00"), summary.getTotalExpenses());
        assertEquals(new BigDecimal("15000.00"), summary.getTotalIncome());
        assertEquals(new BigDecimal("10000.00"), summary.getNetSavings());
        assertEquals(10L, summary.getTotalTransactions());
        assertEquals("Food", summary.getTopSpendingCategory());
        assertEquals(new BigDecimal("3000.00"), summary.getTopCategoryAmount());
    }

    @Test
    @DisplayName("Should categorize transactions via AiCategorizationService")
    void testCategorizeTransactions() {
        CategorizeRequestDTO req = new CategorizeRequestDTO(List.of("UPI/409283/SWIGGY"));
        List<CategorizationResultDTO> results = transactionService.categorizeTransactions(req);

        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals(TransactionCategory.FOOD, results.get(0).getCategory());
        assertEquals("Swiggy", results.get(0).getMerchantName());
    }
}
