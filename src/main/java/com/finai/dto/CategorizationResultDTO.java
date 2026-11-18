package com.finai.dto;

import com.finai.model.TransactionCategory;
import com.finai.model.TransactionType;

/**
 * Structured result returned by the AI categorization engine.
 * Conforms to Section 6 of the project specification.
 */
public class CategorizationResultDTO {

    private String originalDescription;
    private String merchantName;
    private TransactionCategory category;
    private String categoryDisplayName;
    private String subcategory;
    private TransactionType transactionType;
    private Double confidence;
    private String explanation;
    private String provider;
    private boolean fallbackUsed;

    public CategorizationResultDTO() {
    }

    public CategorizationResultDTO(String originalDescription, String merchantName,
                                   TransactionCategory category, String subcategory,
                                   TransactionType transactionType, Double confidence,
                                   String explanation, String provider, boolean fallbackUsed) {
        this.originalDescription = originalDescription;
        this.merchantName = merchantName != null ? merchantName : "Unknown";
        this.category = category != null ? category : TransactionCategory.OTHER;
        this.categoryDisplayName = this.category.getDisplayName();
        this.subcategory = subcategory != null ? subcategory : "General";
        this.transactionType = transactionType != null ? transactionType : TransactionType.DEBIT;
        this.confidence = confidence != null ? confidence : 0.0;
        this.explanation = explanation;
        this.provider = provider;
        this.fallbackUsed = fallbackUsed;
    }

    // Getters and Setters

    public String getOriginalDescription() {
        return originalDescription;
    }

    public void setOriginalDescription(String originalDescription) {
        this.originalDescription = originalDescription;
    }

    public String getMerchantName() {
        return merchantName;
    }

    public void setMerchantName(String merchantName) {
        this.merchantName = merchantName;
    }

    public TransactionCategory getCategory() {
        return category;
    }

    public void setCategory(TransactionCategory category) {
        this.category = category;
        this.categoryDisplayName = (category != null) ? category.getDisplayName() : "Other";
    }

    public String getCategoryDisplayName() {
        return categoryDisplayName;
    }

    public void setCategoryDisplayName(String categoryDisplayName) {
        this.categoryDisplayName = categoryDisplayName;
    }

    public String getSubcategory() {
        return subcategory;
    }

    public void setSubcategory(String subcategory) {
        this.subcategory = subcategory;
    }

    public TransactionType getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(TransactionType transactionType) {
        this.transactionType = transactionType;
    }

    public Double getConfidence() {
        return confidence;
    }

    public void setConfidence(Double confidence) {
        this.confidence = confidence;
    }

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public boolean isFallbackUsed() {
        return fallbackUsed;
    }

    public void setFallbackUsed(boolean fallbackUsed) {
        this.fallbackUsed = fallbackUsed;
    }
}
