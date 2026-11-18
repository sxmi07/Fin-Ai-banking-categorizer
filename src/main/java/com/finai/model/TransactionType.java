package com.finai.model;

/**
 * Indicates whether the transaction is an expense (DEBIT) or incoming funds (CREDIT).
 */
public enum TransactionType {
    DEBIT("Debit", "Money going out / Expense"),
    CREDIT("Credit", "Money coming in / Income");

    private final String displayName;
    private final String description;

    TransactionType(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public static TransactionType fromString(String text) {
        if (text == null || text.isBlank()) {
            return DEBIT;
        }
        String clean = text.trim().toUpperCase();
        for (TransactionType type : values()) {
            if (type.name().equalsIgnoreCase(clean) || type.displayName.equalsIgnoreCase(clean)) {
                return type;
            }
        }
        return DEBIT;
    }
}
