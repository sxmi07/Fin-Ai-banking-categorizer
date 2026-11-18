package com.finai.dto;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Summary DTO delivering financial totals, category breakdown, and metrics.
 * Conforms to Section 5A (Dashboard) and Section 8 (REST API Development).
 */
public class TransactionSummaryDTO {

    private BigDecimal totalExpenses = BigDecimal.ZERO;
    private BigDecimal totalIncome = BigDecimal.ZERO;
    private BigDecimal netSavings = BigDecimal.ZERO;

    private long totalTransactions = 0;
    private long aiCategorizedCount = 0;
    private long uncategorizedCount = 0;
    private long manuallyEditedCount = 0;

    private BigDecimal averageExpenseAmount = BigDecimal.ZERO;
    private String topSpendingCategory = "N/A";
    private BigDecimal topCategoryAmount = BigDecimal.ZERO;

    private Map<String, BigDecimal> categoryExpenses = new LinkedHashMap<>();
    private Map<String, Long> categoryCounts = new LinkedHashMap<>();

    public TransactionSummaryDTO() {
    }

    // Getters and Setters

    public BigDecimal getTotalExpenses() {
        return totalExpenses;
    }

    public void setTotalExpenses(BigDecimal totalExpenses) {
        this.totalExpenses = totalExpenses;
    }

    public BigDecimal getTotalIncome() {
        return totalIncome;
    }

    public void setTotalIncome(BigDecimal totalIncome) {
        this.totalIncome = totalIncome;
    }

    public BigDecimal getNetSavings() {
        return netSavings;
    }

    public void setNetSavings(BigDecimal netSavings) {
        this.netSavings = netSavings;
    }

    public long getTotalTransactions() {
        return totalTransactions;
    }

    public void setTotalTransactions(long totalTransactions) {
        this.totalTransactions = totalTransactions;
    }

    public long getAiCategorizedCount() {
        return aiCategorizedCount;
    }

    public void setAiCategorizedCount(long aiCategorizedCount) {
        this.aiCategorizedCount = aiCategorizedCount;
    }

    public long getUncategorizedCount() {
        return uncategorizedCount;
    }

    public void setUncategorizedCount(long uncategorizedCount) {
        this.uncategorizedCount = uncategorizedCount;
    }

    public long getManuallyEditedCount() {
        return manuallyEditedCount;
    }

    public void setManuallyEditedCount(long manuallyEditedCount) {
        this.manuallyEditedCount = manuallyEditedCount;
    }

    public BigDecimal getAverageExpenseAmount() {
        return averageExpenseAmount;
    }

    public void setAverageExpenseAmount(BigDecimal averageExpenseAmount) {
        this.averageExpenseAmount = averageExpenseAmount;
    }

    public String getTopSpendingCategory() {
        return topSpendingCategory;
    }

    public void setTopSpendingCategory(String topSpendingCategory) {
        this.topSpendingCategory = topSpendingCategory;
    }

    public BigDecimal getTopCategoryAmount() {
        return topCategoryAmount;
    }

    public void setTopCategoryAmount(BigDecimal topCategoryAmount) {
        this.topCategoryAmount = topCategoryAmount;
    }

    public Map<String, BigDecimal> getCategoryExpenses() {
        return categoryExpenses;
    }

    public void setCategoryExpenses(Map<String, BigDecimal> categoryExpenses) {
        this.categoryExpenses = categoryExpenses;
    }

    public Map<String, Long> getCategoryCounts() {
        return categoryCounts;
    }

    public void setCategoryCounts(Map<String, Long> categoryCounts) {
        this.categoryCounts = categoryCounts;
    }
}
