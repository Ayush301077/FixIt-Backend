package com.fixit.FixIt.Backend.service.impl;

import com.fixit.FixIt.Backend.dto.PaymentRequestDto;
import com.fixit.FixIt.Backend.dto.PaymentResponseDto;
import com.fixit.FixIt.Backend.service.PaymentService;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@Service
public class PaymentServiceImpl implements PaymentService {

    private final RazorpayClient razorpayClient;

    @Value("${razorpay.key.id}")
    private String keyId;

    @Value("${razorpay.key.secret}")
    private String keySecret;

    public PaymentServiceImpl(RazorpayClient razorpayClient) {
        this.razorpayClient = razorpayClient;
    }

    @Override
    public PaymentResponseDto createOrder(PaymentRequestDto paymentRequest) throws RazorpayException {
        try {
            JSONObject orderRequest = new JSONObject();
            orderRequest.put("amount", paymentRequest.getAmount() * 100); // amount in smallest currency unit
            orderRequest.put("currency", paymentRequest.getCurrency());
            orderRequest.put("receipt", paymentRequest.getReceipt());

            Order order = razorpayClient.orders.create(orderRequest);

            PaymentResponseDto response = new PaymentResponseDto();
            response.setOrderId(order.get("id").toString());
            response.setAmount(paymentRequest.getAmount());
            response.setCurrency(paymentRequest.getCurrency());
            response.setKeyId(keyId);
            response.setServiceRequestId(paymentRequest.getServiceRequestId().toString());

            return response;
        } catch (RazorpayException e) {
            throw new RazorpayException("Error creating order: " + e.getMessage());
        }
    }

    @Override
    public void verifyPayment(String orderId, String paymentId, String signature) throws RazorpayException {
        try {
            String data = orderId + "|" + paymentId;
            String generatedSignature = calculateHmacSha256(data, keySecret);
            
            if (!generatedSignature.equals(signature)) {
                throw new RazorpayException("Invalid payment signature");
            }
        } catch (Exception e) {
            throw new RazorpayException("Error verifying payment: " + e.getMessage());
        }
    }

    private String calculateHmacSha256(String data, String secret) throws NoSuchAlgorithmException, InvalidKeyException {
        Mac sha256Hmac = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        sha256Hmac.init(secretKey);
        byte[] signedBytes = sha256Hmac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        return bytesToHex(signedBytes);
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }
} 