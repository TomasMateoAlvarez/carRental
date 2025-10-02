package com.example.carrental.enums;

public enum RentalStatus {
    ACTIVE("Activo - Vehículo en uso"),
    COMPLETED("Completado - Vehículo devuelto"),
    OVERDUE("Vencido - Retorno tardío"),
    CANCELLED("Cancelado");

    private final String description;

    RentalStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public boolean canTransitionTo(RentalStatus newStatus) {
        return switch (this) {
            case ACTIVE -> newStatus == COMPLETED || newStatus == OVERDUE || newStatus == CANCELLED;
            case OVERDUE -> newStatus == COMPLETED || newStatus == CANCELLED;
            case COMPLETED, CANCELLED -> false; // Final states
        };
    }

    public boolean isFinalState() {
        return this == COMPLETED || this == CANCELLED;
    }

    public boolean isActive() {
        return this == ACTIVE || this == OVERDUE;
    }
}