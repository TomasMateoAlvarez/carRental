package com.example.carrental.enums;

public enum CustomerSegment {
    NEW("New customer - 0-2 reservations"),
    REGULAR("Regular customer - 3-10 reservations"),
    PREMIUM("Premium customer - 11-25 reservations"),
    VIP("VIP customer - 25+ reservations or high value"),
    CORPORATE("Corporate customer - Business account");

    private final String description;

    CustomerSegment(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}