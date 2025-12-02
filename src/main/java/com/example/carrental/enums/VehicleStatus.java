package com.example.carrental.enums;

/**
 * Estados posibles de un vehículo en el sistema de car rental
 * Implementa un state machine para controlar las transiciones válidas
 */
public enum VehicleStatus {
    AVAILABLE("Disponible para alquiler"),
    RESERVED("Reservado para alquiler futuro"),
    RENTED("Actualmente alquilado"),
    RETURNED("Devuelto - pendiente de limpieza"),
    CLEANING("En proceso de limpieza"),
    OUT_OF_SERVICE("Fuera de servicio temporal"),
    MAINTENANCE("En mantenimiento programado"),
    WASHING("En proceso de limpieza"),
    IN_REPAIR("En reparación");

    private final String description;

    VehicleStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Valida si la transición de estado es válida
     * @param newStatus el nuevo estado deseado
     * @return true si la transición es válida
     */
    public boolean canTransitionTo(VehicleStatus newStatus) {
        return switch (this) {
            case AVAILABLE -> newStatus == RESERVED || newStatus == RENTED ||
                            newStatus == OUT_OF_SERVICE || newStatus == MAINTENANCE ||
                            newStatus == WASHING || newStatus == CLEANING;

            case RESERVED -> newStatus == AVAILABLE || newStatus == RENTED ||
                           newStatus == OUT_OF_SERVICE;

            case RENTED -> newStatus == RETURNED || newStatus == MAINTENANCE ||
                          newStatus == IN_REPAIR || newStatus == AVAILABLE;

            case RETURNED -> newStatus == CLEANING || newStatus == AVAILABLE ||
                           newStatus == MAINTENANCE;

            case CLEANING -> newStatus == AVAILABLE || newStatus == MAINTENANCE ||
                           newStatus == WASHING;

            case OUT_OF_SERVICE -> newStatus == AVAILABLE || newStatus == MAINTENANCE ||
                                  newStatus == IN_REPAIR;

            case MAINTENANCE -> newStatus == AVAILABLE || newStatus == IN_REPAIR ||
                               newStatus == WASHING || newStatus == CLEANING;

            case WASHING -> newStatus == AVAILABLE || newStatus == MAINTENANCE ||
                          newStatus == CLEANING;

            case IN_REPAIR -> newStatus == AVAILABLE || newStatus == MAINTENANCE ||
                             newStatus == OUT_OF_SERVICE;
        };
    }
}