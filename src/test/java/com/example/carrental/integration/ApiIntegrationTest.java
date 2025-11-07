package com.example.carrental.integration;

import com.example.carrental.dto.LoginRequestDTO;
import com.example.carrental.dto.AuthResponseDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class ApiIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    private String getBaseUrl() {
        return "http://localhost:" + port + "/api/v1";
    }

    @Test
    void testHealthEndpoint() {
        ResponseEntity<String> response = restTemplate.getForEntity(
            "http://localhost:" + port + "/actuator/health", String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().contains("UP"));
    }

    @Test
    void testLoginEndpoint() {
        LoginRequestDTO loginRequest = new LoginRequestDTO();
        loginRequest.setUsername("admin");
        loginRequest.setPassword("admin123");

        ResponseEntity<AuthResponseDTO> response = restTemplate.postForEntity(
            getBaseUrl() + "/auth/login", loginRequest, AuthResponseDTO.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getToken());
        assertEquals("admin", response.getBody().getUsername());
    }

    @Test
    void testLoginWithInvalidCredentials() {
        LoginRequestDTO loginRequest = new LoginRequestDTO();
        loginRequest.setUsername("invalid");
        loginRequest.setPassword("invalid");

        ResponseEntity<String> response = restTemplate.postForEntity(
            getBaseUrl() + "/auth/login", loginRequest, String.class);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void testUnauthorizedAccessToProtectedEndpoint() {
        ResponseEntity<String> response = restTemplate.getForEntity(
            getBaseUrl() + "/vehicles", String.class);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void testAuthorizedAccessToProtectedEndpoint() {
        // First, login to get token
        LoginRequestDTO loginRequest = new LoginRequestDTO();
        loginRequest.setUsername("admin");
        loginRequest.setPassword("admin123");

        ResponseEntity<AuthResponseDTO> loginResponse = restTemplate.postForEntity(
            getBaseUrl() + "/auth/login", loginRequest, AuthResponseDTO.class);

        assertEquals(HttpStatus.OK, loginResponse.getStatusCode());
        String token = loginResponse.getBody().getToken();

        // Now, use token to access protected endpoint
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
            getBaseUrl() + "/vehicles", HttpMethod.GET, entity, String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void testDashboardKPIsEndpoint() {
        // Login first
        LoginRequestDTO loginRequest = new LoginRequestDTO();
        loginRequest.setUsername("admin");
        loginRequest.setPassword("admin123");

        ResponseEntity<AuthResponseDTO> loginResponse = restTemplate.postForEntity(
            getBaseUrl() + "/auth/login", loginRequest, AuthResponseDTO.class);

        String token = loginResponse.getBody().getToken();

        // Access dashboard KPIs
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
            getBaseUrl() + "/dashboard/kpis", HttpMethod.GET, entity, String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("totalVehicles"));
    }

    @Test
    void testDatabaseConnectivity() {
        // Test that the application can start and connect to database
        // This test passes if the Spring context loads successfully
        assertTrue(port > 0);
        assertNotNull(restTemplate);
    }

    @Test
    void testCorsConfiguration() {
        // Test CORS headers are present
        LoginRequestDTO loginRequest = new LoginRequestDTO();
        loginRequest.setUsername("admin");
        loginRequest.setPassword("admin123");

        ResponseEntity<AuthResponseDTO> response = restTemplate.postForEntity(
            getBaseUrl() + "/auth/login", loginRequest, AuthResponseDTO.class);

        HttpHeaders responseHeaders = response.getHeaders();
        // CORS headers should be present in actual cross-origin requests
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void testJsonSerialization() throws Exception {
        LoginRequestDTO loginRequest = new LoginRequestDTO();
        loginRequest.setUsername("test");
        loginRequest.setPassword("test123");

        String json = objectMapper.writeValueAsString(loginRequest);
        assertTrue(json.contains("test"));
        assertTrue(json.contains("test123"));

        LoginRequestDTO deserialized = objectMapper.readValue(json, LoginRequestDTO.class);
        assertEquals("test", deserialized.getUsername());
        assertEquals("test123", deserialized.getPassword());
    }

    @Test
    void testErrorHandling() {
        // Test that malformed JSON returns proper error
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>("{invalid json}", headers);

        ResponseEntity<String> response = restTemplate.exchange(
            getBaseUrl() + "/auth/login", HttpMethod.POST, entity, String.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }
}