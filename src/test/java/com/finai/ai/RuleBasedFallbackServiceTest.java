package com.finai.ai;

import com.finai.dto.CategorizationResultDTO;
import com.finai.model.TransactionCategory;
import com.finai.model.TransactionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RuleBasedFallbackServiceTest {

    private RuleBasedFallbackService fallbackService;

    @BeforeEach
    void setUp() {
        fallbackService = new RuleBasedFallbackService();
    }

    @Test
    @DisplayName("Should categorize food delivery transactions correctly")
    void testFoodCategory() {
        CategorizationResultDTO result = fallbackService.categorize("UPI/409283/SWIGGY BANGALORE");
        assertEquals(TransactionCategory.FOOD, result.getCategory());
        assertTrue(result.getConfidence() >= 0.90);
        assertEquals(TransactionType.DEBIT, result.getTransactionType());
        assertTrue(result.isFallbackUsed());
    }

    @Test
    @DisplayName("Should categorize rideshare transit transactions correctly")
    void testTravelCategory() {
        CategorizationResultDTO result = fallbackService.categorize("UPI/829182/UBER INDIA MUMBAI");
        assertEquals(TransactionCategory.TRAVEL, result.getCategory());
        assertTrue(result.getConfidence() >= 0.90);
    }

    @Test
    @DisplayName("Should categorize electricity utilities correctly")
    void testUtilitiesCategory() {
        CategorizationResultDTO result = fallbackService.categorize("BESCOM ELECTRICITY BILL BANGALORE");
        assertEquals(TransactionCategory.UTILITIES, result.getCategory());
        assertTrue(result.getConfidence() >= 0.90);
    }

    @Test
    @DisplayName("Should categorize e-commerce shopping correctly")
    void testShoppingCategory() {
        CategorizationResultDTO result = fallbackService.categorize("AMAZON PAY/ORDER/82918");
        assertEquals(TransactionCategory.SHOPPING, result.getCategory());
        assertTrue(result.getConfidence() >= 0.90);
    }

    @Test
    @DisplayName("Should categorize entertainment streaming subscriptions correctly")
    void testEntertainmentCategory() {
        CategorizationResultDTO result = fallbackService.categorize("NETFLIX SUBSCRIPTION PREMIUM");
        assertEquals(TransactionCategory.ENTERTAINMENT, result.getCategory());
        assertTrue(result.getConfidence() >= 0.90);
    }

    @Test
    @DisplayName("Should categorize medical pharmacy transactions correctly")
    void testHealthcareCategory() {
        CategorizationResultDTO result = fallbackService.categorize("APOLLO PHARMACY MEDICALS");
        assertEquals(TransactionCategory.HEALTHCARE, result.getCategory());
    }

    @Test
    @DisplayName("Should categorize online education correctly")
    void testEducationCategory() {
        CategorizationResultDTO result = fallbackService.categorize("UDEMY ONLINE COURSE JAVA 17");
        assertEquals(TransactionCategory.EDUCATION, result.getCategory());
    }

    @Test
    @DisplayName("Should categorize apartment rent correctly")
    void testRentCategory() {
        CategorizationResultDTO result = fallbackService.categorize("NEFT/RENT/OCTOBER APARTMENT");
        assertEquals(TransactionCategory.RENT, result.getCategory());
    }

    @Test
    @DisplayName("Should categorize salary income as credit transfer")
    void testSalaryCredit() {
        CategorizationResultDTO result = fallbackService.categorize("NEFT/SALARY/TECHCORP SERVICES");
        assertEquals(TransactionCategory.TRANSFERS, result.getCategory());
        assertEquals(TransactionType.CREDIT, result.getTransactionType());
    }

    @Test
    @DisplayName("Should categorize ambiguous transactions as Other with lower confidence")
    void testAmbiguousTransaction() {
        CategorizationResultDTO result = fallbackService.categorize("POS-TXN-90281 SHOP NO 42");
        assertEquals(TransactionCategory.OTHER, result.getCategory());
        assertTrue(result.getConfidence() <= 0.60);
    }

    @Test
    @DisplayName("Should handle empty and blank strings gracefully")
    void testBlankDescription() {
        CategorizationResultDTO result = fallbackService.categorize("   ");
        assertEquals(TransactionCategory.OTHER, result.getCategory());
        assertEquals(0.0, result.getConfidence());
    }
}
