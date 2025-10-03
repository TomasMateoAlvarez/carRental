package com.example.carrental.controller;

import com.example.carrental.dto.PaymentRequestDTO;
import com.example.carrental.dto.RefundRequestDTO;
import com.example.carrental.model.Payment;
import com.example.carrental.services.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174"})
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/create-payment-intent")
    public ResponseEntity<com.example.carrental.dto.PaymentResponseDTO> createPaymentIntent(@Valid @RequestBody PaymentRequestDTO request) {
        com.example.carrental.dto.PaymentResponseDTO paymentIntent = paymentService.createPaymentIntent(request);
        return ResponseEntity.ok(paymentIntent);
    }

    @PostMapping("/confirm")
    public ResponseEntity<com.example.carrental.dto.PaymentResponseDTO> confirmPayment(@RequestParam String paymentIntentId) {
        com.example.carrental.dto.PaymentResponseDTO payment = paymentService.confirmPayment(paymentIntentId);
        return ResponseEntity.ok(payment);
    }

    @PostMapping("/refund")
    public ResponseEntity<com.example.carrental.dto.PaymentResponseDTO> refundPayment(@Valid @RequestBody RefundRequestDTO request) {
        com.example.carrental.dto.PaymentResponseDTO refund = paymentService.refundPayment(request);
        return ResponseEntity.ok(refund);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Payment>> getUserPayments(@PathVariable Long userId) {
        List<Payment> payments = paymentService.getUserPayments(userId);
        return ResponseEntity.ok(payments);
    }

    @GetMapping("/{paymentId}")
    public ResponseEntity<Payment> getPayment(@PathVariable Long paymentId) {
        Payment payment = paymentService.getPaymentById(paymentId);
        return ResponseEntity.ok(payment);
    }

    @GetMapping("/code/{paymentCode}")
    public ResponseEntity<Payment> getPaymentByCode(@PathVariable String paymentCode) {
        Payment payment = paymentService.getPaymentByCode(paymentCode);
        return ResponseEntity.ok(payment);
    }

    @PostMapping("/webhook")
    public ResponseEntity<String> handleStripeWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader) {
        try {
            paymentService.handleStripeWebhook(payload, sigHeader);
            return ResponseEntity.ok("Success");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Webhook error: " + e.getMessage());
        }
    }

    @GetMapping("/revenue/daily")
    public ResponseEntity<Map<String, Object>> getDailyRevenue() {
        Map<String, Object> revenue = paymentService.getDailyRevenue();
        return ResponseEntity.ok(revenue);
    }

    @GetMapping("/revenue/monthly")
    public ResponseEntity<Map<String, Object>> getMonthlyRevenue() {
        Map<String, Object> revenue = paymentService.getMonthlyRevenue();
        return ResponseEntity.ok(revenue);
    }
}