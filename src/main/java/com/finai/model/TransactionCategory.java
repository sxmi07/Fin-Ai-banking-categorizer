package com.finai.model;

/**
 * Predefined spending categories supported by FinAI.
 * As defined in Section 6 of the project specification.
 */
public enum TransactionCategory {
    FOOD("Food", "Groceries, dining, food delivery, cafes"),
    TRAVEL("Travel", "Flights, trains, cabs, rideshare, fuel, transit"),
    UTILITIES("Utilities", "Electricity, water, gas, internet, mobile recharge"),
    SHOPPING("Shopping", "Clothing, electronics, e-commerce, retail"),
    ENTERTAINMENT("Entertainment", "Streaming, movies, gaming, events"),
    HEALTHCARE("Healthcare", "Pharmacies, doctors, clinics, insurance, diagnostics"),
    EDUCATION("Education", "Tuition, courses, books, learning subscriptions"),
    RENT("Rent", "House rent, apartment lease, maintenance"),
    TRANSFERS("Transfers", "Peer-to-peer transfers, account deposits, withdrawals"),
    OTHER("Other", "Uncategorized, general, or ambiguous expenses");

    private final String displayName;
    private final String description;

    TransactionCategory(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public static TransactionCategory fromString(String text) {
        if (text == null || text.isBlank()) {
            return OTHER;
        }
        String clean = text.trim().toUpperCase();
        for (TransactionCategory cat : values()) {
            if (cat.name().equalsIgnoreCase(clean) || cat.displayName.equalsIgnoreCase(clean)) {
                return cat;
            }
        }
        return OTHER;
    }
}
