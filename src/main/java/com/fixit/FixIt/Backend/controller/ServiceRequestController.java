package com.fixit.FixIt.Backend.controller;

import com.fixit.FixIt.Backend.dto.CreateServiceRequestDto;
import com.fixit.FixIt.Backend.model.ServiceRequest;
import com.fixit.FixIt.Backend.model.ServiceStatus;
import com.fixit.FixIt.Backend.model.User;
import com.fixit.FixIt.Backend.repository.UserRepository;
import com.fixit.FixIt.Backend.service.ServiceRequestService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/service-requests")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true", maxAge = 3600)
public class ServiceRequestController {
    private static final Logger logger = LoggerFactory.getLogger(ServiceRequestController.class);

    @Autowired
    private ServiceRequestService serviceRequestService;

    @Autowired
    private UserRepository userRepository;

    @PostMapping
    public ResponseEntity<ServiceRequest> createServiceRequest(@Valid @RequestBody CreateServiceRequestDto requestDto) {
        try {
            logger.info("Received service request DTO: {}", requestDto);
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String userEmail = auth.getName();
            User currentUser = userRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new RuntimeException("Authenticated user not found"));

            logger.info("Customer for service request: {}", currentUser);
            ServiceRequest savedRequest = serviceRequestService.createServiceRequest(requestDto, currentUser);
            logger.info("Successfully saved service request: {}", savedRequest);
            return ResponseEntity.ok(savedRequest);
        } catch (Exception e) {
            logger.error("Error creating service request", e);
            throw e;
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ServiceRequest> updateServiceRequest(
            @PathVariable Long id,
            @Valid @RequestBody ServiceRequest serviceRequest) {
        return ResponseEntity.ok(serviceRequestService.updateServiceRequest(id, serviceRequest));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteServiceRequest(@PathVariable Long id) {
        serviceRequestService.deleteServiceRequest(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ServiceRequest> getServiceRequest(@PathVariable Long id) {
        return ResponseEntity.ok(serviceRequestService.getServiceRequest(id));
    }

    @GetMapping
    public ResponseEntity<List<ServiceRequest>> getAllServiceRequests() {
        return ResponseEntity.ok(serviceRequestService.getAllServiceRequests());
    }

    @GetMapping("/customer")
    public ResponseEntity<List<ServiceRequest>> getCustomerServiceRequests() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = auth.getName();
        User currentUser = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Authenticated user not found"));
        return ResponseEntity.ok(serviceRequestService.getServiceRequestsByCustomer(currentUser));
    }

    @GetMapping("/provider")
    public ResponseEntity<List<ServiceRequest>> getProviderServiceRequests() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = auth.getName();
        User currentUser = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Authenticated user not found"));
        return ResponseEntity.ok(serviceRequestService.getServiceRequestsByProvider(currentUser));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<ServiceRequest>> getServiceRequestsByStatus(@PathVariable ServiceStatus status) {
        return ResponseEntity.ok(serviceRequestService.getServiceRequestsByStatus(status));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ServiceRequest> updateServiceRequestStatus(
            @PathVariable Long id,
            @RequestParam ServiceStatus status) {
        return ResponseEntity.ok(serviceRequestService.updateServiceRequestStatus(id, status));
    }

    @GetMapping("/healthz")
    public String healthz() {
        return "OK";
    }
} 