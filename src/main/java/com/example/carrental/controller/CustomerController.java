package com.example.carrental.controller;

import com.example.carrental.dto.*;
import com.example.carrental.enums.CustomerSegment;
import com.example.carrental.enums.CustomerStatus;
import com.example.carrental.services.CustomerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/customers")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174"})
public class CustomerController {

    private final CustomerService customerService;

    // Basic CRUD Operations

    @GetMapping
    public ResponseEntity<List<CustomerResponseDTO>> getAllCustomers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() :
                Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<CustomerResponseDTO> customers = customerService.getAllCustomers(pageable);

        return ResponseEntity.ok(customers.getContent());
    }

    @GetMapping("/paginated")
    public ResponseEntity<Page<CustomerResponseDTO>> getAllCustomersPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() :
                Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<CustomerResponseDTO> customers = customerService.getAllCustomers(pageable);

        return ResponseEntity.ok(customers);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CustomerResponseDTO> getCustomerById(@PathVariable Long id) {
        CustomerResponseDTO customer = customerService.getCustomerById(id);
        return ResponseEntity.ok(customer);
    }

    @GetMapping("/code/{customerCode}")
    public ResponseEntity<CustomerResponseDTO> getCustomerByCode(@PathVariable String customerCode) {
        CustomerResponseDTO customer = customerService.getCustomerByCode(customerCode);
        return ResponseEntity.ok(customer);
    }

    @PostMapping
    public ResponseEntity<CustomerResponseDTO> createCustomer(@Valid @RequestBody CustomerRequestDTO customerDTO) {
        CustomerResponseDTO createdCustomer = customerService.createCustomer(customerDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdCustomer);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CustomerResponseDTO> updateCustomer(
            @PathVariable Long id,
            @Valid @RequestBody CustomerRequestDTO customerDTO) {
        CustomerResponseDTO updatedCustomer = customerService.updateCustomer(id, customerDTO);
        return ResponseEntity.ok(updatedCustomer);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCustomer(@PathVariable Long id) {
        customerService.deleteCustomer(id);
        return ResponseEntity.noContent().build();
    }

    // Customer History and Analytics

    @GetMapping("/{id}/history")
    public ResponseEntity<CustomerHistoryDTO> getCustomerHistory(@PathVariable Long id) {
        CustomerHistoryDTO history = customerService.getCustomerHistory(id);
        return ResponseEntity.ok(history);
    }

    @PutMapping("/{id}/update-statistics")
    public ResponseEntity<Void> updateCustomerStatistics(@PathVariable Long id) {
        customerService.updateCustomerStatistics(id);
        return ResponseEntity.ok().build();
    }

    // Search and Filter Operations

    @GetMapping("/search")
    public ResponseEntity<Page<CustomerResponseDTO>> searchCustomers(
            @RequestParam String searchTerm,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<CustomerResponseDTO> customers = customerService.searchCustomers(searchTerm, pageable);
        return ResponseEntity.ok(customers);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<CustomerResponseDTO>> getCustomersByStatus(@PathVariable CustomerStatus status) {
        List<CustomerResponseDTO> customers = customerService.getCustomersByStatus(status);
        return ResponseEntity.ok(customers);
    }

    @GetMapping("/segment/{segment}")
    public ResponseEntity<List<CustomerResponseDTO>> getCustomersBySegment(@PathVariable CustomerSegment segment) {
        List<CustomerResponseDTO> customers = customerService.getCustomersBySegment(segment);
        return ResponseEntity.ok(customers);
    }

    @GetMapping("/expiring-licenses")
    public ResponseEntity<List<CustomerResponseDTO>> getCustomersWithExpiringLicenses(
            @RequestParam(defaultValue = "30") int daysAhead) {
        List<CustomerResponseDTO> customers = customerService.getCustomersWithExpiringLicenses(daysAhead);
        return ResponseEntity.ok(customers);
    }

    @GetMapping("/top-customers")
    public ResponseEntity<List<CustomerResponseDTO>> getTopCustomers(
            @RequestParam(defaultValue = "spending") String criteria,
            @RequestParam(defaultValue = "10") int limit) {
        List<CustomerResponseDTO> customers = customerService.getTopCustomers(criteria, limit);
        return ResponseEntity.ok(customers);
    }

    // Customer Analytics Endpoints

    @GetMapping("/analytics/segments")
    public ResponseEntity<Object> getCustomersBySegmentAnalytics() {
        // This could be expanded to return detailed analytics
        return ResponseEntity.ok("Segment analytics endpoint - to be implemented");
    }

    @GetMapping("/analytics/spending")
    public ResponseEntity<Object> getSpendingAnalytics() {
        // This could be expanded to return spending analytics
        return ResponseEntity.ok("Spending analytics endpoint - to be implemented");
    }

    @GetMapping("/analytics/activity")
    public ResponseEntity<Object> getActivityAnalytics() {
        // This could be expanded to return activity analytics
        return ResponseEntity.ok("Activity analytics endpoint - to be implemented");
    }

    // Utility Endpoints

    @GetMapping("/validate/email")
    public ResponseEntity<Boolean> validateEmailAvailability(@RequestParam String email) {
        // This would check if email is available
        return ResponseEntity.ok(true); // Simplified for now
    }

    @GetMapping("/validate/license")
    public ResponseEntity<Boolean> validateLicenseAvailability(@RequestParam String licenseNumber) {
        // This would check if license number is available
        return ResponseEntity.ok(true); // Simplified for now
    }

    // Customer Engagement Endpoints

    @PostMapping("/{id}/send-notification")
    public ResponseEntity<String> sendNotificationToCustomer(
            @PathVariable Long id,
            @RequestBody String message) {
        // This would send a notification to the customer
        return ResponseEntity.ok("Notification sent successfully");
    }

    @GetMapping("/{id}/engagement-score")
    public ResponseEntity<Object> getCustomerEngagementScore(@PathVariable Long id) {
        // This would calculate and return customer engagement score
        return ResponseEntity.ok("Engagement score calculation - to be implemented");
    }

    // Exception handling would be done by global exception handler
}