package com.example.carrental.enums;

public enum PaymentMethod {
    CREDIT_CARD("Tarjeta de Crédito"),
    DEBIT_CARD("Tarjeta de Débito"),
    PAYPAL("PayPal"),
    BANK_TRANSFER("Transferencia Bancaria"),
    APPLE_PAY("Apple Pay"),
    GOOGLE_PAY("Google Pay"),
    KLARNA("Klarna"),
    CASH("Efectivo"),
    CHECK("Cheque"),
    CRYPTO("Criptomoneda");

    private final String description;

    PaymentMethod(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public boolean isOnline() {
        return this != CASH && this != CHECK;
    }

    public boolean requiresStripe() {
        return this == CREDIT_CARD || this == DEBIT_CARD || this == APPLE_PAY || this == GOOGLE_PAY;
    }

    public boolean isInstant() {
        return this == CREDIT_CARD || this == DEBIT_CARD || this == APPLE_PAY ||
               this == GOOGLE_PAY || this == PAYPAL;
    }
}