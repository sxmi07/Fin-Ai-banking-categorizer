package com.finai.dto;

import jakarta.validation.constraints.NotEmpty;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Request payload for the POST /api/transactions/categorize endpoint.
 * Accepts multiple transaction descriptions or structured items.
 */
public class CategorizeRequestDTO {

    @NotEmpty(message = "At least one transaction description must be provided")
    private List<String> descriptions = new ArrayList<>();

    // Optional metadata if single item or bulk items have known amounts
    private BigDecimal defaultAmount;
    private LocalDate defaultDate;
    private boolean saveToDatabase = false;

    public CategorizeRequestDTO() {
    }

    public CategorizeRequestDTO(List<String> descriptions) {
        this.descriptions = descriptions;
    }

    public List<String> getDescriptions() {
        return descriptions;
    }

    public void setDescriptions(List<String> descriptions) {
        this.descriptions = descriptions;
    }

    public BigDecimal getDefaultAmount() {
        return defaultAmount;
    }

    public void setDefaultAmount(BigDecimal defaultAmount) {
        this.defaultAmount = defaultAmount;
    }

    public LocalDate getDefaultDate() {
        return defaultDate;
    }

    public void setDefaultDate(LocalDate defaultDate) {
        this.defaultDate = defaultDate;
    }

    public boolean isSaveToDatabase() {
        return saveToDatabase;
    }

    public void setSaveToDatabase(boolean saveToDatabase) {
        this.saveToDatabase = saveToDatabase;
    }
}
