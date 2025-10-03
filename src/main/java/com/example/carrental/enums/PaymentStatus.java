package com.example.carrental.enums;

public enum PaymentStatus {
    PENDING("Pendiente"),
    PROCESSING("Procesando"),
    COMPLETED("Completado"),
    FAILED("Fallido"),
    CANCELLED("Cancelado"),
    REFUNDED("Reembolsado"),
    PARTIALLY_REFUNDED("Parcialmente Reembolsado"),
    DISPUTED("En Disputa"),
    CHARGEBACK("Contracargo");

    private final String description;

    PaymentStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public boolean isTerminal() {
        return this == COMPLETED || this == FAILED || this == CANCELLED || this == REFUNDED;
    }

    public boolean isSuccessful() {
        return this == COMPLETED || this == PARTIALLY_REFUNDED;
    }

    public boolean canBeRefunded() {
        return this == COMPLETED || this == PARTIALLY_REFUNDED;
    }
}