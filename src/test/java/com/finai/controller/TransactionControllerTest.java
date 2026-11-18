package com.finai.controller;

import com.finai.dto.*;
import com.finai.exception.ResourceNotFoundException;
import com.finai.model.Transaction;
import com.finai.model.TransactionCategory;
import com.finai.model.TransactionType;
import com.finai.service.CsvService;
import com.finai.service.TransactionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Lightweight unit tests for TransactionController.
 * Uses lightweight stub services to avoid Mockito JVM agent attachment overhead on low-memory environments.
 */
class TransactionControllerTest {

    private TransactionController controller;
    private AtomicBoolean deleteCalled;
    private Transaction sampleTx;

    @BeforeEach
    void setUp() {
        deleteCalled = new AtomicBoolean(false);
        sampleTx = new Transaction("UPI/409283/SWIGGY", "Swiggy", new BigDecimal("450.00"),
                LocalDate.now(), TransactionType.DEBIT, TransactionCategory.FOOD, "Delivery", 0.98, "OK");
        sampleTx.setId(1L);

        TransactionService stubTransactionService = new TransactionService(null, null) {
            @Override
            public Page<TransactionResponseDTO> getTransactions(String search, TransactionCategory category,
                                                                 TransactionType type, LocalDate startDate,
                                                                 LocalDate endDate, Pageable pageable) {
                return new PageImpl<>(Collections.singletonList(TransactionResponseDTO.fromEntity(sampleTx)));
            }

            @Override
            public TransactionResponseDTO getTransactionById(Long id) {
                if (id == 999L) {
                    throw new ResourceNotFoundException("Transaction", "id", id);
                }
                return TransactionResponseDTO.fromEntity(sampleTx);
            }

            @Override
            public TransactionResponseDTO createTransaction(TransactionRequestDTO request) {
                Transaction created = new Transaction(request.getDescription(), "Amazon", request.getAmount(),
                        request.getDate(), request.getType(), TransactionCategory.OTHER, "Pending", 0.0, "Manual");
                created.setId(10L);
                return TransactionResponseDTO.fromEntity(created);
            }

            @Override
            public TransactionResponseDTO updateCategory(Long id, CategoryUpdateRequestDTO request) {
                sampleTx.setCategory(TransactionCategory.fromString(request.getCategory()));
                sampleTx.setSubcategory(request.getSubcategory());
                sampleTx.setManuallyEdited(true);
                return TransactionResponseDTO.fromEntity(sampleTx);
            }

            @Override
            public void deleteTransaction(Long id) {
                deleteCalled.set(true);
            }

            @Override
            public TransactionSummaryDTO getSummary(Integer year, Integer month) {
                TransactionSummaryDTO summary = new TransactionSummaryDTO();
                summary.setTotalExpenses(new BigDecimal("5000.00"));
                summary.setTotalIncome(new BigDecimal("12000.00"));
                summary.setTotalTransactions(8);
                return summary;
            }

            @Override
            public List<CategorizationResultDTO> categorizeTransactions(CategorizeRequestDTO request) {
                CategorizationResultDTO result = new CategorizationResultDTO(
                        "UPI/409283/SWIGGY", "Swiggy", TransactionCategory.FOOD, "Food Delivery",
                        TransactionType.DEBIT, 0.98, "Matched Swiggy", "Rule-Based", true
                );
                return List.of(result);
            }
        };

        CsvService stubCsvService = new CsvService(null, null) {
            @Override
            public byte[] exportCsv() {
                return "ID,Date,Merchant\n1,2026-11-15,Swiggy".getBytes();
            }
        };

        controller = new TransactionController(stubTransactionService, stubCsvService);
    }

    @Test
    @DisplayName("GET /api/transactions should return paginated list")
    void testGetTransactions() {
        ResponseEntity<ApiResponse<Page<TransactionResponseDTO>>> response = controller.getTransactions(
                null, null, null, null, null, 0, 15, "transactionDate", "DESC");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals(1, response.getBody().getData().getContent().size());
        assertEquals("Swiggy", response.getBody().getData().getContent().get(0).getMerchantName());
    }

    @Test
    @DisplayName("GET /api/transactions/{id} should return transaction when found")
    void testGetTransactionById_Success() {
        ResponseEntity<ApiResponse<TransactionResponseDTO>> response = controller.getTransactionById(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals(1L, response.getBody().getData().getId());
        assertEquals(TransactionCategory.FOOD, response.getBody().getData().getCategory());
    }

    @Test
    @DisplayName("GET /api/transactions/{id} should throw ResourceNotFoundException when missing")
    void testGetTransactionById_NotFound() {
        assertThrows(ResourceNotFoundException.class, () -> controller.getTransactionById(999L));
    }

    @Test
    @DisplayName("POST /api/transactions should create transaction and return 201 CREATED")
    void testCreateTransaction_Success() {
        TransactionRequestDTO req = new TransactionRequestDTO("AMAZON PAY", new BigDecimal("1299.00"), LocalDate.now(), TransactionType.DEBIT);
        ResponseEntity<ApiResponse<TransactionResponseDTO>> response = controller.createTransaction(req);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals(10L, response.getBody().getData().getId());
    }

    @Test
    @DisplayName("PUT /api/transactions/{id}/category should update category and return 200 OK")
    void testUpdateCategory_Success() {
        CategoryUpdateRequestDTO req = new CategoryUpdateRequestDTO("Shopping", "Online Retail");
        ResponseEntity<ApiResponse<TransactionResponseDTO>> response = controller.updateCategory(1L, req);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals(TransactionCategory.SHOPPING, response.getBody().getData().getCategory());
        assertTrue(response.getBody().getData().getManuallyEdited());
    }

    @Test
    @DisplayName("DELETE /api/transactions/{id} should delete transaction and return 200 OK")
    void testDeleteTransaction_Success() {
        ResponseEntity<ApiResponse<Void>> response = controller.deleteTransaction(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertTrue(deleteCalled.get());
    }

    @Test
    @DisplayName("GET /api/transactions/summary should return financial summary")
    void testGetSummary() {
        ResponseEntity<ApiResponse<TransactionSummaryDTO>> response = controller.getSummary(null, null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals(new BigDecimal("5000.00"), response.getBody().getData().getTotalExpenses());
        assertEquals(new BigDecimal("12000.00"), response.getBody().getData().getTotalIncome());
    }

    @Test
    @DisplayName("POST /api/transactions/categorize should return categorized results")
    void testCategorizeEndpoint() {
        CategorizeRequestDTO req = new CategorizeRequestDTO(List.of("UPI/409283/SWIGGY"));
        ResponseEntity<ApiResponse<List<CategorizationResultDTO>>> response = controller.categorize(req);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals(1, response.getBody().getData().size());
        assertEquals("Swiggy", response.getBody().getData().get(0).getMerchantName());
    }

    @Test
    @DisplayName("GET /api/transactions/export should return CSV stream")
    void testExportCsv() {
        ResponseEntity<byte[]> response = controller.exportCsv();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(new String(response.getBody()).contains("Swiggy"));
        assertTrue(response.getHeaders().getFirst("Content-Disposition").contains("finai_transactions.csv"));
    }
}
