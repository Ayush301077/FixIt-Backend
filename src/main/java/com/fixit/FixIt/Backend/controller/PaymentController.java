package com.fixit.FixIt.Backend.controller;

import com.fixit.FixIt.Backend.dto.PaymentRequestDto;
import com.fixit.FixIt.Backend.dto.PaymentResponseDto;
import com.fixit.FixIt.Backend.service.PaymentService;
import com.razorpay.RazorpayException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@CrossOrigin(origins = "http://localhost:5173")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/create-order")
    public ResponseEntity<PaymentResponseDto> createOrder(@RequestBody PaymentRequestDto paymentRequest) throws RazorpayException {
        PaymentResponseDto response = paymentService.createOrder(paymentRequest);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/verify")
    public ResponseEntity<String> verifyPayment(
            @RequestParam String orderId,
            @RequestParam String paymentId,
            @RequestParam String signature) throws RazorpayException {
        paymentService.verifyPayment(orderId, paymentId, signature);
        return ResponseEntity.ok("Payment verified successfully");
    }
} 