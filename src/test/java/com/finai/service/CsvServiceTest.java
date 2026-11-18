package com.finai.service;

import com.finai.ai.AiCategorizationService;
import com.finai.dto.CategorizationResultDTO;
import com.finai.model.Transaction;
import com.finai.model.TransactionCategory;
import com.finai.model.TransactionType;
import com.finai.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Lightweight unit tests for CsvService.
 * Uses lightweight Java standard dynamic proxy and subclass stubs.
 */
class CsvServiceTest {

    private CsvService csvService;
    private List<Transaction> savedTransactions;
    private Transaction sampleTx;

    @BeforeEach
    void setUp() {
        savedTransactions = new ArrayList<>();
        sampleTx = new Transaction(
                "NETFLIX SUBSCRIPTION", "Netflix", new BigDecimal("649.00"),
                LocalDate.of(2026, 11, 10), TransactionType.DEBIT,
                TransactionCategory.ENTERTAINMENT, "Streaming", 0.99, "Valid subscription"
        );
        sampleTx.setId(101L);

        TransactionRepository repoStub = (TransactionRepository) Proxy.newProxyInstance(
                TransactionRepository.class.getClassLoader(),
                new Class<?>[]{TransactionRepository.class},
                (proxy, method, args) -> {
                    if ("saveAll".equals(method.getName())) {
                        @SuppressWarnings("unchecked")
                        Iterable<Transaction> list = (Iterable<Transaction>) args[0];
                        list.forEach(savedTransactions::add);
                        return savedTransactions;
                    }
                    if ("findAll".equals(method.getName())) {
                        return List.of(sampleTx);
                    }
                    return null;
                }
        );

        AiCategorizationService aiServiceStub = new AiCategorizationService(null, null) {
            @Override
            public List<CategorizationResultDTO> categorizeBatch(List<String> descriptions) {
                return descriptions.stream().map(d -> new CategorizationResultDTO(
                        d,
                        d.contains("SWIGGY") ? "Swiggy" : "BESCOM",
                        d.contains("SWIGGY") ? TransactionCategory.FOOD : TransactionCategory.UTILITIES,
                        "Services",
                        TransactionType.DEBIT,
                        0.95,
                        "Matched",
                        "AI",
                        false
                )).toList();
            }
        };

        csvService = new CsvService(repoStub, aiServiceStub);
    }

    @Test
    @DisplayName("Should import CSV and categorize rows via AI service")
    void testImportCsv_Success() throws IOException {
        String csvContent = """
                Description,Amount,Date,Type
                UPI/409283/SWIGGY BANGALORE,450.00,2026-11-15,DEBIT
                BESCOM ELECTRICITY BILL,1200.00,2026-11-16,DEBIT
                ,50.00,2026-11-17,DEBIT
                """;

        MockMultipartFile file = new MockMultipartFile(
                "file", "statement.csv", "text/csv", csvContent.getBytes(StandardCharsets.UTF_8)
        );

        Map<String, Object> result = csvService.importCsv(file);

        assertNotNull(result);
        assertEquals(2, result.get("importedCount"));
        assertEquals(1, result.get("skippedCount"));
        assertEquals(2, savedTransactions.size());
        assertEquals("Swiggy", savedTransactions.get(0).getMerchantName());
        assertEquals(TransactionCategory.FOOD, savedTransactions.get(0).getCategory());
        assertEquals("BESCOM", savedTransactions.get(1).getMerchantName());
        assertEquals(TransactionCategory.UTILITIES, savedTransactions.get(1).getCategory());
    }

    @Test
    @DisplayName("Should export transactions to CSV with proper header and fields")
    void testExportCsv() {
        byte[] csvBytes = csvService.exportCsv();
        assertNotNull(csvBytes);
        String csvString = new String(csvBytes, StandardCharsets.UTF_8);

        assertTrue(csvString.startsWith("ID,Date,Merchant,Description"));
        assertTrue(csvString.contains("Netflix"));
        assertTrue(csvString.contains("NETFLIX SUBSCRIPTION"));
        assertTrue(csvString.contains("ENTERTAINMENT"));
        assertTrue(csvString.contains("649.00"));
    }
}
