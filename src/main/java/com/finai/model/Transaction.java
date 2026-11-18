package com.finai.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Core JPA Entity representing a categorized banking transaction.
 * Conforms to Section 9 of the project specification.
 */
@Entity
@Table(name = "transactions", indexes = {
    @Index(name = "idx_tx_date", columnList = "transactionDate"),
    @Index(name = "idx_tx_category", columnList = "category"),
    @Index(name = "idx_tx_type", columnList = "transactionType")
})
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 500)
    private String rawDescription;

    @Column(length = 150)
    private String merchantName;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false)
    private LocalDate transactionDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TransactionType transactionType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private TransactionCategory category;

    @Column(length = 100)
    private String subcategory;

    @Column
    private Double aiConfidence;

    @Column(length = 1000)
    private String aiExplanation;

    @Column(nullable = false)
    private Boolean manuallyEdited = false;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public Transaction() {
    }

    public Transaction(String rawDescription, String merchantName, BigDecimal amount,
                       LocalDate transactionDate, TransactionType transactionType,
                       TransactionCategory category, String subcategory,
                       Double aiConfidence, String aiExplanation) {
        this.rawDescription = rawDescription;
        this.merchantName = merchantName != null ? merchantName : "Unknown";
        this.amount = amount;
        this.transactionDate = transactionDate != null ? transactionDate : LocalDate.now();
        this.transactionType = transactionType != null ? transactionType : TransactionType.DEBIT;
        this.category = category != null ? category : TransactionCategory.OTHER;
        this.subcategory = subcategory;
        this.aiConfidence = aiConfidence != null ? aiConfidence : 0.0;
        this.aiExplanation = aiExplanation;
        this.manuallyEdited = false;
    }

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        if (this.transactionDate == null) {
            this.transactionDate = LocalDate.now();
        }
        if (this.transactionType == null) {
            this.transactionType = TransactionType.DEBIT;
        }
        if (this.category == null) {
            this.category = TransactionCategory.OTHER;
        }
        if (this.manuallyEdited == null) {
            this.manuallyEdited = false;
        }
        if (this.merchantName == null || this.merchantName.isBlank()) {
            this.merchantName = "Unknown";
        }
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
