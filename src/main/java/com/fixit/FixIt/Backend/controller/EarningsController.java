package com.fixit.FixIt.Backend.controller;

import com.fixit.FixIt.Backend.dto.CompletedServiceDetailDto;
import com.fixit.FixIt.Backend.dto.EarningsResponseDto;
import com.fixit.FixIt.Backend.model.ServiceRequest;
import com.fixit.FixIt.Backend.model.ServiceStatus;
import com.fixit.FixIt.Backend.model.User;
import com.fixit.FixIt.Backend.repository.ServiceRequestRepository;
import com.fixit.FixIt.Backend.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/earnings")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true", maxAge = 3600)
public class EarningsController {

    private static final Logger logger = LoggerFactory.getLogger(EarningsController.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ServiceRequestRepository serviceRequestRepository;

    @GetMapping
    public ResponseEntity<EarningsResponseDto> getEarnings(@RequestParam(required = false) String period) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = auth.getName();
        User currentUser = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Authenticated user not found"));

        logger.info("Authenticated user role: {}", currentUser.getRole());

        if (!currentUser.getRole().name().equalsIgnoreCase("PROVIDER")) {
            throw new RuntimeException("Only service providers can view earnings.");
        }

        // For simplicity, we'll calculate total earnings regardless of the 'period' for now.
        // You can extend this logic to filter by period (e.g., weekly, monthly) later.
        List<ServiceRequest> completedRequests = serviceRequestRepository.findByProviderAndStatus(
                currentUser,
                ServiceStatus.COMPLETED
        );

        BigDecimal totalEarnings = completedRequests.stream()
                .map(ServiceRequest::getServiceCharge)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Integer completedServicesCount = completedRequests.size();

        List<CompletedServiceDetailDto> completedServiceDetails = completedRequests.stream()
                .map(request -> new CompletedServiceDetailDto(
                        request.getDate(),
                        request.getCustomer().getName(),
                        request.getContact(), // Assuming contact is customer's contact for this DTO
                        request.getServiceName(),
                        request.getServiceCharge()
                ))
                .collect(Collectors.toList());

        logger.info("Completed Requests Size: {}", completedRequests.size());
        logger.info("Calculated Total Earnings: {}", totalEarnings);
        logger.info("Calculated Completed Services Count: {}", completedServicesCount);
        logger.info("Detailed Completed Services List Size: {}", completedServiceDetails.size());
        logger.info("Detailed Completed Services List: {}", completedServiceDetails); // Log the list content

        EarningsResponseDto responseDto = new EarningsResponseDto(
                totalEarnings,
                completedServicesCount,
                completedServiceDetails
        );

        return ResponseEntity.ok(responseDto);
    }
}