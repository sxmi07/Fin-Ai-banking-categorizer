package com.finai.dto;

import com.finai.model.Transaction;
import com.finai.model.TransactionCategory;
import com.finai.model.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Standard response payload representing a transaction.
 */
public class TransactionResponseDTO {

    private Long id;
    private String rawDescription;
    private String merchantName;
    private BigDecimal amount;
    private LocalDate transactionDate;
    private TransactionType transactionType;
    private TransactionCategory category;
    private String categoryDisplayName;
    private String subcategory;
    private Double aiConfidence;
    private String aiExplanation;
    private Boolean manuallyEdited;
    private LocalDateTime createdAt;

    public TransactionResponseDTO() {
    }

    public static TransactionResponseDTO fromEntity(Transaction entity) {
        if (entity == null) {
            return null;
        }
        TransactionResponseDTO dto = new TransactionResponseDTO();
        dto.setId(entity.getId());
        dto.setRawDescription(entity.getRawDescription());
        dto.setMerchantName(entity.getMerchantName());
        dto.setAmount(entity.getAmount());
        dto.setTransactionDate(entity.getTransactionDate());
        dto.setTransactionType(entity.getTransactionType());
        dto.setCategory(entity.getCategory());
        dto.setCategoryDisplayName(entity.getCategory() != null ? entity.getCategory().getDisplayName() : "Other");
        dto.setSubcategory(entity.getSubcategory());
        dto.setAiConfidence(entity.getAiConfidence());
        dto.setAiExplanation(entity.getAiExplanation());
        dto.setManuallyEdited(entity.getManuallyEdited());
        dto.setCreatedAt(entity.getCreatedAt());
        return dto;
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRawDescription() {
        return rawDescription;
    }

    public void setRawDescription(String rawDescription) {
        this.rawDescription = rawDescription;
    }

    public String getMerchantName() {
        return merchantName;
    }

    public void setMerchantName(String merchantName) {
        this.merchantName = merchantName;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public LocalDate getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(LocalDate transactionDate) {
        this.transactionDate = transactionDate;
    }

    public TransactionType getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(TransactionType transactionType) {
        this.transactionType = transactionType;
    }

    public TransactionCategory getCategory() {
        return category;
    }

    public void setCategory(TransactionCategory category) {
        this.category = category;
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

    public Double getAiConfidence() {
        return aiConfidence;
    }

    public void setAiConfidence(Double aiConfidence) {
        this.aiConfidence = aiConfidence;
    }

    public String getAiExplanation() {
        return aiExplanation;
    }

    public void setAiExplanation(String aiExplanation) {
        this.aiExplanation = aiExplanation;
    }

    public Boolean getManuallyEdited() {
        return manuallyEdited;
    }

    public void setManuallyEdited(Boolean manuallyEdited) {
        this.manuallyEdited = manuallyEdited;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
