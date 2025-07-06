package com.fixit.FixIt.Backend.service;

import com.fixit.FixIt.Backend.dto.PaymentRequestDto;
import com.fixit.FixIt.Backend.dto.PaymentResponseDto;
import com.razorpay.RazorpayException;

public interface PaymentService {
    PaymentResponseDto createOrder(PaymentRequestDto paymentRequest) throws RazorpayException;
    void verifyPayment(String orderId, String paymentId, String signature) throws RazorpayException;
} 