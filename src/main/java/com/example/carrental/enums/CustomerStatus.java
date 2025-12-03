package com.example.carrental.enums;

public enum CustomerStatus {
    ACTIVE("Active - Can make reservations"),
    INACTIVE("Inactive - Temporarily disabled"),
    SUSPENDED("Suspended - Account restricted"),
    BLOCKED("Blocked - Permanently banned"),
    PENDING_VERIFICATION("Pending verification - New customer");

    private final String description;

    CustomerStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}