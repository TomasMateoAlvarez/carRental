package com.example.carrental.scheduler;

import com.example.carrental.services.ReservationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduler for automatic vehicle status updates based on reservation dates
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(value = "app.scheduling.enabled", havingValue = "true", matchIfMissing = true)
public class VehicleStatusScheduler {

    private final ReservationService reservationService;

    /**
     * Update vehicle statuses based on current date
     * Runs every day at 6:00 AM
     *
     * This task will:
     * - Set vehicles to RESERVED if their reservation is active today
     * - Set vehicles to AVAILABLE if their reservation has ended
     */
    @Scheduled(cron = "0 0 6 * * ?") // Every day at 6:00 AM
    public void updateVehicleStatuses() {
        try {
            log.info("=== Starting scheduled vehicle status update ===");
            reservationService.updateAllVehicleStatusesBasedOnDates();
            log.info("=== Scheduled vehicle status update completed successfully ===");
        } catch (Exception e) {
            log.error("Error during scheduled vehicle status update", e);
        }
    }

    /**
     * Additional task that runs every hour during business hours
     * to ensure real-time updates during the day
     */
    @Scheduled(cron = "0 0 8-20 * * ?") // Every hour from 8 AM to 8 PM
    public void updateVehicleStatusesHourly() {
        try {
            log.debug("Running hourly vehicle status update");
            reservationService.updateAllVehicleStatusesBasedOnDates();
            log.debug("Hourly vehicle status update completed");
        } catch (Exception e) {
            log.error("Error during hourly vehicle status update", e);
        }
    }
}