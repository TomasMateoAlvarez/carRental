package com.example.carrental.controller;

import com.example.carrental.dto.CreateReservationRequestDTO;
import com.example.carrental.dto.ReservationResponseDTO;
import com.example.carrental.enums.ReservationStatus;
import com.example.carrental.services.ReservationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ReservationControllerTest {

    @Mock
    private ReservationService reservationService;

    @InjectMocks
    private ReservationController reservationController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(reservationController).build();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    void createReservation_WithValidData_ShouldReturnCreatedReservation() throws Exception {
        // Given
        CreateReservationRequestDTO request = new CreateReservationRequestDTO();
        request.setVehicleId(1L);
        request.setStartDate(LocalDate.now().plusDays(1));
        request.setEndDate(LocalDate.now().plusDays(3));
        request.setPickupLocation("Madrid Centro");
        request.setReturnLocation("Madrid Aeropuerto");
        request.setSpecialRequests("Ninguna");

        ReservationResponseDTO response = createSampleReservationResponse();

        when(reservationService.createReservation(any(CreateReservationRequestDTO.class), anyString()))
                .thenReturn(response);

        // When & Then
        mockMvc.perform(post("/api/v1/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.reservationCode").value("RES123456"))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.vehicleBrand").value("Toyota"))
                .andExpect(jsonPath("$.vehicleModel").value("Corolla"))
                .andExpect(jsonPath("$.pickupLocation").value("Madrid Centro"))
                .andExpect(jsonPath("$.returnLocation").value("Madrid Aeropuerto"));

        verify(reservationService, times(1)).createReservation(any(CreateReservationRequestDTO.class), eq("admin"));
    }

    @Test
    void createReservation_WithInvalidData_ShouldReturnBadRequest() throws Exception {
        // Given - Request with null vehicleId (invalid)
        CreateReservationRequestDTO request = new CreateReservationRequestDTO();
        request.setVehicleId(null); // Invalid
        request.setStartDate(LocalDate.now().plusDays(1));
        request.setEndDate(LocalDate.now().plusDays(3));

        // When & Then
        mockMvc.perform(post("/api/v1/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(reservationService, never()).createReservation(any(), anyString());
    }

    @Test
    void createReservation_WithPastDates_ShouldReturnBadRequest() throws Exception {
        // Given - Request with past dates
        CreateReservationRequestDTO request = new CreateReservationRequestDTO();
        request.setVehicleId(1L);
        request.setStartDate(LocalDate.now().minusDays(1)); // Past date
        request.setEndDate(LocalDate.now().plusDays(1));
        request.setPickupLocation("Madrid Centro");
        request.setReturnLocation("Madrid Aeropuerto");

        // When & Then
        mockMvc.perform(post("/api/v1/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(reservationService, never()).createReservation(any(), anyString());
    }

    @Test
    void getAllReservations_ShouldReturnReservationsList() throws Exception {
        // Given
        List<ReservationResponseDTO> reservations = Arrays.asList(
                createSampleReservationResponse(),
                createAnotherSampleReservationResponse()
        );

        when(reservationService.getAllReservations()).thenReturn(reservations);

        // When & Then
        mockMvc.perform(get("/api/v1/reservations/all"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].reservationCode").value("RES123456"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].reservationCode").value("RES789012"));

        verify(reservationService, times(1)).getAllReservations();
    }

    @Test
    void updateReservationStatus_WithValidData_ShouldReturnUpdatedReservation() throws Exception {
        // Given
        Long reservationId = 1L;
        String newStatus = "CONFIRMED";
        ReservationResponseDTO updatedReservation = createSampleReservationResponse();
        updatedReservation.setStatus(ReservationStatus.CONFIRMED);

        when(reservationService.updateReservationStatus(eq(reservationId), eq(ReservationStatus.CONFIRMED)))
                .thenReturn(updatedReservation);

        // When & Then
        mockMvc.perform(put("/api/v1/reservations/{id}/status", reservationId)
                        .param("status", newStatus))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("CONFIRMED"));

        verify(reservationService, times(1)).updateReservationStatus(reservationId, ReservationStatus.CONFIRMED);
    }

    @Test
    void updateReservationStatus_WithInvalidStatus_ShouldReturnBadRequest() throws Exception {
        // Given
        Long reservationId = 1L;
        String invalidStatus = "INVALID_STATUS";

        // When & Then
        mockMvc.perform(put("/api/v1/reservations/{id}/status", reservationId)
                        .param("status", invalidStatus))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Invalid status"));

        verify(reservationService, never()).updateReservationStatus(any(), any());
    }

    @Test
    void deleteReservation_WithValidId_ShouldReturnOk() throws Exception {
        // Given
        Long reservationId = 1L;
        doNothing().when(reservationService).deleteReservation(eq(reservationId), anyString());

        // When & Then
        mockMvc.perform(delete("/api/v1/reservations/{id}", reservationId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Reservation deleted successfully"));

        verify(reservationService, times(1)).deleteReservation(reservationId, "admin");
    }

    @Test
    void checkVehicleAvailability_WithValidData_ShouldReturnAvailability() throws Exception {
        // Given
        Long vehicleId = 1L;
        LocalDate startDate = LocalDate.now().plusDays(1);
        LocalDate endDate = LocalDate.now().plusDays(3);

        when(reservationService.isVehicleAvailable(vehicleId, startDate, endDate))
                .thenReturn(true);

        // When & Then
        mockMvc.perform(get("/api/v1/reservations/check-availability")
                        .param("vehicleId", vehicleId.toString())
                        .param("startDate", startDate.toString())
                        .param("endDate", endDate.toString()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.available").value(true));

        verify(reservationService, times(1)).isVehicleAvailable(vehicleId, startDate, endDate);
    }

    // Helper methods to create sample data
    private ReservationResponseDTO createSampleReservationResponse() {
        ReservationResponseDTO reservation = new ReservationResponseDTO();
        reservation.setId(1L);
        reservation.setReservationCode("RES123456");
        reservation.setStartDate(LocalDate.now().plusDays(1));
        reservation.setEndDate(LocalDate.now().plusDays(3));
        reservation.setPickupLocation("Madrid Centro");
        reservation.setReturnLocation("Madrid Aeropuerto");
        reservation.setStatus(ReservationStatus.PENDING);
        reservation.setDailyRate(BigDecimal.valueOf(45.00));
        reservation.setTotalDays(3);
        reservation.setTotalAmount(BigDecimal.valueOf(135.00));
        reservation.setSpecialRequests("Ninguna");
        reservation.setCreatedAt(LocalDateTime.now());
        reservation.setVehicleId(1L);
        reservation.setVehicleBrand("Toyota");
        reservation.setVehicleModel("Corolla");
        reservation.setVehicleLicensePlate("ABC-123");
        reservation.setVehicleCategory("COMPACT");
        reservation.setUserId(1L);
        reservation.setUserFullName("Admin User");
        reservation.setUserEmail("admin@carrental.com");
        return reservation;
    }

    private ReservationResponseDTO createAnotherSampleReservationResponse() {
        ReservationResponseDTO reservation = new ReservationResponseDTO();
        reservation.setId(2L);
        reservation.setReservationCode("RES789012");
        reservation.setStartDate(LocalDate.now().plusDays(5));
        reservation.setEndDate(LocalDate.now().plusDays(7));
        reservation.setPickupLocation("Barcelona Centro");
        reservation.setReturnLocation("Barcelona Aeropuerto");
        reservation.setStatus(ReservationStatus.CONFIRMED);
        reservation.setDailyRate(BigDecimal.valueOf(50.00));
        reservation.setTotalDays(3);
        reservation.setTotalAmount(BigDecimal.valueOf(150.00));
        reservation.setSpecialRequests("GPS incluido");
        reservation.setCreatedAt(LocalDateTime.now());
        reservation.setVehicleId(2L);
        reservation.setVehicleBrand("Honda");
        reservation.setVehicleModel("Civic");
        reservation.setVehicleLicensePlate("XYZ-789");
        reservation.setVehicleCategory("COMPACT");
        reservation.setUserId(2L);
        reservation.setUserFullName("Demo User");
        reservation.setUserEmail("demo@carrental.com");
        return reservation;
    }
}