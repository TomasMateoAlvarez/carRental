package com.example.carrental.enums;

public enum ReservationStatus {
    PENDING("Pendiente de confirmación"),
    CONFIRMED("Confirmada"),
    IN_PROGRESS("En curso"),
    COMPLETED("Completada"),
    CANCELLED("Cancelada"),
    NO_SHOW("Cliente no se presentó");

    private final String description;

    ReservationStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public boolean canTransitionTo(ReservationStatus newStatus) {
        return switch (this) {
            case PENDING -> newStatus == CONFIRMED || newStatus == CANCELLED;

            case CONFIRMED -> newStatus == IN_PROGRESS || newStatus == CANCELLED ||
                             newStatus == NO_SHOW;

            case IN_PROGRESS -> newStatus == COMPLETED || newStatus == CANCELLED;

            case COMPLETED, CANCELLED, NO_SHOW -> false; // Final states
        };
    }

    public boolean isFinalState() {
        return this == COMPLETED || this == CANCELLED || this == NO_SHOW;
    }

    public boolean isActive() {
        return this == CONFIRMED || this == IN_PROGRESS;
    }
}