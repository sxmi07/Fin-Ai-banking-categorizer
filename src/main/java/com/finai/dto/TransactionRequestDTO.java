package com.finai.dto;

import com.finai.model.TransactionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Request payload for manually entering or batch submitting a transaction.
 */
public class TransactionRequestDTO {

    @NotBlank(message = "Transaction description cannot be blank")
    private String description;

    @NotNull(message = "Transaction amount is required")
    @Positive(message = "Amount must be greater than zero")
    private BigDecimal amount;

    private LocalDate date;

    private TransactionType type;

    public TransactionRequestDTO() {
    }

    public TransactionRequestDTO(String description, BigDecimal amount, LocalDate date, TransactionType type) {
        this.description = description;
        this.amount = amount;
        this.date = date;
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public TransactionType getType() {
        return type;
    }

    public void setType(TransactionType type) {
        this.type = type;
    }
}
