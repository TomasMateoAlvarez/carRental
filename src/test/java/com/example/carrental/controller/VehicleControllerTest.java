package com.example.carrental.controller;

import com.example.carrental.dto.VehicleRequestDTO;
import com.example.carrental.dto.VehicleResponseDTO;
import com.example.carrental.enums.VehicleStatus;
import com.example.carrental.services.VehicleService;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class VehicleControllerTest {

    @Mock
    private VehicleService vehicleService;

    @InjectMocks
    private VehicleController vehicleController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(vehicleController).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void getAllVehicles_ShouldReturnVehiclesList() throws Exception {
        // Given
        List<VehicleResponseDTO> vehicles = Arrays.asList(
                createSampleVehicleResponse(),
                createAnotherSampleVehicleResponse()
        );

        when(vehicleService.getAllVehicles()).thenReturn(vehicles);

        // When & Then
        mockMvc.perform(get("/api/v1/vehicles"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].brand").value("Toyota"))
                .andExpect(jsonPath("$[0].model").value("Corolla"))
                .andExpect(jsonPath("$[0].licensePlate").value("ABC-123"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].brand").value("Honda"))
                .andExpect(jsonPath("$[1].model").value("Civic"));

        verify(vehicleService, times(1)).getAllVehicles();
    }

    @Test
    void getVehicleById_WithValidId_ShouldReturnVehicle() throws Exception {
        // Given
        Long vehicleId = 1L;
        VehicleResponseDTO vehicle = createSampleVehicleResponse();

        when(vehicleService.getVehicleById(vehicleId)).thenReturn(vehicle);

        // When & Then
        mockMvc.perform(get("/api/v1/vehicles/{id}", vehicleId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.brand").value("Toyota"))
                .andExpect(jsonPath("$.model").value("Corolla"))
                .andExpect(jsonPath("$.licensePlate").value("ABC-123"))
                .andExpect(jsonPath("$.status").value("AVAILABLE"))
                .andExpect(jsonPath("$.dailyRate").value(45.00));

        verify(vehicleService, times(1)).getVehicleById(vehicleId);
    }

    @Test
    void createVehicle_WithValidData_ShouldReturnCreatedVehicle() throws Exception {
        // Given
        VehicleRequestDTO request = createSampleVehicleRequest();
        VehicleResponseDTO response = createSampleVehicleResponse();

        when(vehicleService.createVehicle(any(VehicleRequestDTO.class))).thenReturn(response);

        // When & Then
        mockMvc.perform(post("/api/v1/vehicles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.brand").value("Toyota"))
                .andExpect(jsonPath("$.model").value("Corolla"))
                .andExpect(jsonPath("$.licensePlate").value("ABC-123"))
                .andExpect(jsonPath("$.status").value("AVAILABLE"));

        verify(vehicleService, times(1)).createVehicle(any(VehicleRequestDTO.class));
    }

    @Test
    void createVehicle_WithInvalidData_ShouldReturnBadRequest() throws Exception {
        // Given - Request with missing required fields
        VehicleRequestDTO request = new VehicleRequestDTO();
        request.setBrand(""); // Invalid - empty brand
        request.setModel(""); // Invalid - empty model
        // Missing other required fields

        // When & Then
        mockMvc.perform(post("/api/v1/vehicles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(vehicleService, never()).createVehicle(any());
    }

    @Test
    void updateVehicle_WithValidData_ShouldReturnUpdatedVehicle() throws Exception {
        // Given
        Long vehicleId = 1L;
        VehicleRequestDTO request = createSampleVehicleRequest();
        request.setDailyRate(BigDecimal.valueOf(50.00)); // Updated rate

        VehicleResponseDTO response = createSampleVehicleResponse();
        response.setDailyRate(BigDecimal.valueOf(50.00));

        when(vehicleService.updateVehicle(eq(vehicleId), any(VehicleRequestDTO.class))).thenReturn(response);

        // When & Then
        mockMvc.perform(put("/api/v1/vehicles/{id}", vehicleId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.dailyRate").value(50.00));

        verify(vehicleService, times(1)).updateVehicle(eq(vehicleId), any(VehicleRequestDTO.class));
    }

    @Test
    void deleteVehicle_WithValidId_ShouldReturnNoContent() throws Exception {
        // Given
        Long vehicleId = 1L;
        doNothing().when(vehicleService).deleteVehicle(vehicleId);

        // When & Then
        mockMvc.perform(delete("/api/v1/vehicles/{id}", vehicleId))
                .andExpect(status().isNoContent());

        verify(vehicleService, times(1)).deleteVehicle(vehicleId);
    }

    @Test
    void getAvailableVehicles_ShouldReturnOnlyAvailableVehicles() throws Exception {
        // Given
        List<VehicleResponseDTO> availableVehicles = Arrays.asList(
                createSampleVehicleResponse() // Status is AVAILABLE by default
        );

        when(vehicleService.getAvailableVehicles()).thenReturn(availableVehicles);

        // When & Then
        mockMvc.perform(get("/api/v1/vehicles/available"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].status").value("AVAILABLE"));

        verify(vehicleService, times(1)).getAvailableVehicles();
    }

    @Test
    void updateVehicleStatus_WithValidData_ShouldReturnUpdatedVehicle() throws Exception {
        // Given
        Long vehicleId = 1L;
        VehicleStatus newStatus = VehicleStatus.MAINTENANCE;
        VehicleResponseDTO response = createSampleVehicleResponse();
        response.setStatus(VehicleStatus.MAINTENANCE);

        when(vehicleService.changeVehicleStatus(vehicleId, newStatus)).thenReturn(response);

        // When & Then
        mockMvc.perform(patch("/api/v1/vehicles/{id}/status", vehicleId)
                        .param("status", newStatus.name()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("MAINTENANCE"));

        verify(vehicleService, times(1)).changeVehicleStatus(vehicleId, newStatus);
    }

    // Helper methods to create sample data
    private VehicleRequestDTO createSampleVehicleRequest() {
        VehicleRequestDTO request = new VehicleRequestDTO();
        request.setBrand("Toyota");
        request.setModel("Corolla");
        request.setYear(2023);
        request.setLicensePlate("ABC-123");
        request.setCategory("COMPACT");
        request.setColor("Blanco");
        request.setFuelType("GASOLINE");
        request.setTransmission("AUTOMATIC");
        request.setSeats(5);
        request.setDailyRate(BigDecimal.valueOf(45.00));
        request.setMileage(25000);
        request.setStatus(VehicleStatus.AVAILABLE);
        request.setDescription("Toyota Corolla 2023 en excelente estado");
        return request;
    }

    private VehicleResponseDTO createSampleVehicleResponse() {
        VehicleResponseDTO response = new VehicleResponseDTO();
        response.setId(1L);
        response.setBrand("Toyota");
        response.setModel("Corolla");
        response.setYear(2023);
        response.setLicensePlate("ABC-123");
        response.setCategory("COMPACT");
        response.setColor("Blanco");
        response.setFuelType("GASOLINE");
        response.setTransmission("AUTOMATIC");
        response.setSeats(5);
        response.setDailyRate(BigDecimal.valueOf(45.00));
        response.setMileage(25000);
        response.setStatus(VehicleStatus.AVAILABLE);
        response.setDescription("Toyota Corolla 2023 en excelente estado");
        response.setCreatedAt(LocalDateTime.now());
        response.setUpdatedAt(LocalDateTime.now());
        return response;
    }

    private VehicleResponseDTO createAnotherSampleVehicleResponse() {
        VehicleResponseDTO response = new VehicleResponseDTO();
        response.setId(2L);
        response.setBrand("Honda");
        response.setModel("Civic");
        response.setYear(2022);
        response.setLicensePlate("XYZ-789");
        response.setCategory("COMPACT");
        response.setColor("Negro");
        response.setFuelType("GASOLINE");
        response.setTransmission("MANUAL");
        response.setSeats(5);
        response.setDailyRate(BigDecimal.valueOf(40.00));
        response.setMileage(30000);
        response.setStatus(VehicleStatus.AVAILABLE);
        response.setDescription("Honda Civic 2022 económico");
        response.setCreatedAt(LocalDateTime.now());
        response.setUpdatedAt(LocalDateTime.now());
        return response;
    }
}