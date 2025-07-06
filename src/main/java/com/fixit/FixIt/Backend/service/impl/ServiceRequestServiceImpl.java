package com.fixit.FixIt.Backend.service.impl;

import com.fixit.FixIt.Backend.model.Earning;
import com.fixit.FixIt.Backend.model.User;
import com.fixit.FixIt.Backend.repository.ServiceRepository;
import com.fixit.FixIt.Backend.repository.ServiceRequestRepository;
import com.fixit.FixIt.Backend.repository.UserRepository;
import com.fixit.FixIt.Backend.service.EarningService;
import com.fixit.FixIt.Backend.service.ServiceRequestService;
import com.fixit.FixIt.Backend.service.EmailService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.fixit.FixIt.Backend.model.ServiceRequest;
import com.fixit.FixIt.Backend.model.ServiceStatus;
import com.fixit.FixIt.Backend.dto.CreateServiceRequestDto;

import java.time.LocalDate;
import java.util.List;

@Service
public class ServiceRequestServiceImpl implements ServiceRequestService {
    private static final Logger logger = LoggerFactory.getLogger(ServiceRequestServiceImpl.class);

    @Autowired
    private ServiceRequestRepository serviceRequestRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ServiceRepository serviceRepository;

    @Autowired
    private EarningService earningService;

    @Autowired
    private EmailService emailService;

    @Override
    public ServiceRequest createServiceRequest(CreateServiceRequestDto requestDto, User customer) {
        try {
            logger.info("Creating service request from DTO: {} for customer: {}", requestDto, customer.getEmail());

            // Fetch Provider User object
            User provider = userRepository.findById(requestDto.getProviderId())
                    .orElseThrow(() -> new EntityNotFoundException("Provider not found with ID: " + requestDto.getProviderId()));

            // Fetch Service object using fully qualified name to avoid ambiguity
            com.fixit.FixIt.Backend.model.Service service = serviceRepository.findById(requestDto.getServiceId())
                    .orElseThrow(() -> new EntityNotFoundException("Service not found with ID: " + requestDto.getServiceId()));

            ServiceRequest serviceRequest = new ServiceRequest();
            serviceRequest.setCustomer(customer);
            serviceRequest.setProvider(provider);
            serviceRequest.setDescription(requestDto.getDescription());
            serviceRequest.setDate(requestDto.getDate());
            serviceRequest.setTime(requestDto.getTime());
            serviceRequest.setStatus(ServiceStatus.PENDING); // Set initial status
            serviceRequest.setService(service);
            serviceRequest.setServiceName(requestDto.getServiceName());
            serviceRequest.setServiceCharge(requestDto.getServiceCharge());
            serviceRequest.setContact(requestDto.getContact());
            serviceRequest.setAddress(requestDto.getAddress());

            ServiceRequest savedRequest = serviceRequestRepository.save(serviceRequest);

            // Send email notification to the provider
            String providerEmail = provider.getEmail();
            String subject = "New Service Request Available!";
            String text = String.format("Dear %s,\\n\\nYou have a new service request available!\\n\\nService: %s\\nDescription: %s\\nCustomer: %s\\nContact: %s\\nDate: %s\\nTime: %s\\n\\nPlease review the request in your dashboard.\\n\\nThank you,\\nFixIt Team",
                    provider.getName(),
                    serviceRequest.getServiceName(),
                    serviceRequest.getDescription(),
                    customer.getName(),
                    serviceRequest.getContact(),
                    serviceRequest.getDate().toString(),
                    serviceRequest.getTime().toString()
            );
            emailService.sendSimpleEmail(providerEmail, subject, text);
            logger.info("Email notification sent to provider {} for new service request {}.", providerEmail, savedRequest.getId());

            return savedRequest;
        } catch (Exception e) {
            logger.error("Error creating service request: ", e);
            throw e;
        }
    }

    @Override
    public ServiceRequest getServiceRequest(Long id) {
        return serviceRequestRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Service Request not found with ID: " + id));
    }

    @Override
    public List<ServiceRequest> getAllServiceRequests() {
        return serviceRequestRepository.findAll();
    }

    @Override
    public List<ServiceRequest> getServiceRequestsByStatus(ServiceStatus status) {
        return serviceRequestRepository.findByStatus(status);
    }

    @Override
    @Transactional
    public ServiceRequest updateServiceRequestStatus(Long id, ServiceStatus newStatus) {
        ServiceRequest serviceRequest = getServiceRequest(id);

        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Authenticated user not found"));

        boolean isCustomer = serviceRequest.getCustomer().getId().equals(currentUser.getId());
        boolean isProvider = serviceRequest.getProvider().getId().equals(currentUser.getId());

        if (newStatus == ServiceStatus.COMPLETED) {
            if (isCustomer && serviceRequest.getStatus() == ServiceStatus.ACCEPTED) {
                // Customer completing online payment
                Earning earning = new Earning();
                earning.setProvider(serviceRequest.getProvider());
                earning.setAmount(serviceRequest.getServiceCharge().doubleValue());
                earning.setDate(LocalDate.now());
                earningService.createEarning(earning);
                logger.info("Earning record created for provider {} for amount {} via online payment", serviceRequest.getProvider().getEmail(), serviceRequest.getServiceCharge());
            } else if (isProvider && serviceRequest.getStatus() == ServiceStatus.CASH_PAID_PENDING_CONFIRMATION) {
                // Provider confirming cash payment
                Earning earning = new Earning();
                earning.setProvider(serviceRequest.getProvider());
                earning.setAmount(serviceRequest.getServiceCharge().doubleValue());
                earning.setDate(LocalDate.now());
                earningService.createEarning(earning);
                logger.info("Earning record created for provider {} for amount {} (cash confirmed)", serviceRequest.getProvider().getEmail(), serviceRequest.getServiceCharge());
            } else {
                throw new IllegalArgumentException("Invalid status transition to COMPLETED for current user or request status.");
            }
        } else if (newStatus == ServiceStatus.CASH_PAID_PENDING_CONFIRMATION) {
            if (!isCustomer || serviceRequest.getStatus() != ServiceStatus.ACCEPTED) {
                throw new AccessDeniedException("Only the customer can mark this request as cash paid from ACCEPTED status.");
            }
        } else if (newStatus == ServiceStatus.CASH_PAYMENT_REJECTED) {
            if (!isProvider || serviceRequest.getStatus() != ServiceStatus.CASH_PAID_PENDING_CONFIRMATION) {
                throw new AccessDeniedException("Only the provider can reject cash payment from CASH_PAID_PENDING_CONFIRMATION status.");
            }
            serviceRequest.setStatus(ServiceStatus.ACCEPTED);
            // Send email notification to customer
            String customerEmail = serviceRequest.getCustomer().getEmail();
            String subject = "Cash Payment Rejected";
            String text = String.format("Dear %s,\\n\\nYour cash payment for the service '%s' was rejected by the provider, %s. Please try to pay again from your dashboard..\\n\\nThank you,\\nFixIt Team",
                    serviceRequest.getCustomer().getName(),
                    serviceRequest.getServiceName(),
                    serviceRequest.getProvider().getName());
            emailService.sendSimpleEmail(customerEmail, subject, text);
            logger.info("Cash payment rejected by provider for request {}. Status set to ACCEPTED and notification sent to customer {}.", id, customerEmail);
            return serviceRequestRepository.save(serviceRequest);
        } else if (newStatus == ServiceStatus.ACCEPTED || newStatus == ServiceStatus.CANCELLED) {
            // Existing logic for ACCEPTED and CANCELLED (from PENDING, by provider)
            if (!isProvider || serviceRequest.getStatus() != ServiceStatus.PENDING) {
                throw new AccessDeniedException("Only the provider can change status to ACCEPTED or CANCELLED from PENDING status.");
            }

            // Send email notification to customer for ACCEPTED status
            if (newStatus == ServiceStatus.ACCEPTED) {
                String customerEmail = serviceRequest.getCustomer().getEmail();
                String subject = "Your Service Request Has Been Accepted!";
                String text = String.format("Dear %s,\\n\\nGood news! Your service request for \'%s\' on %s at %s has been accepted by %s.\\n\\nService: %s\\nProvider: %s\\nContact Provider: %s\\nCharge: %s\\n\\nThank you,\\nFixIt Team",
                        serviceRequest.getCustomer().getName(),
                        serviceRequest.getServiceName(),
                        serviceRequest.getDate().toString(),
                        serviceRequest.getTime().toString(),
                        serviceRequest.getProvider().getName(),
                        serviceRequest.getServiceName(),
                        serviceRequest.getProvider().getName(),
                        serviceRequest.getProvider().getContact(),
                        serviceRequest.getServiceCharge().toString()
                );
                emailService.sendSimpleEmail(customerEmail, subject, text);
                logger.info("Email notification sent to customer {} for accepted service request {}.", customerEmail, id);
            }

            // Send email notification to customer for CANCELLED status
            if (newStatus == ServiceStatus.CANCELLED) {
                String customerEmail = serviceRequest.getCustomer().getEmail();
                String subject = "Your Service Request Has Been Cancelled";
                String text = String.format("Dear %s,\\n\\nYour service request for \'%s\' on %s at %s has been cancelled by %s.\\n\\nService: %s\\nProvider: %s\\n\\nWe apologize for any inconvenience. You may consider booking another provider.\\n\\nThank you,\\nFixIt Team",
                        serviceRequest.getCustomer().getName(),
                        serviceRequest.getServiceName(),
                        serviceRequest.getDate().toString(),
                        serviceRequest.getTime().toString(),
                        serviceRequest.getProvider().getName(),
                        serviceRequest.getServiceName(),
                        serviceRequest.getProvider().getName()
                );
                emailService.sendSimpleEmail(customerEmail, subject, text);
                logger.info("Email notification sent to customer {} for cancelled service request {}.", customerEmail, id);
            }

        } else if (newStatus == ServiceStatus.PENDING) {
            throw new IllegalArgumentException("Cannot set status back to PENDING.");
        } else {
            throw new IllegalArgumentException("Invalid new status provided.");
        }

        serviceRequest.setStatus(newStatus);
        return serviceRequestRepository.save(serviceRequest);
    }

    @Override
    public ServiceRequest getServiceRequestByIdAndCustomer(Long requestId, User customer) {
        return serviceRequestRepository.findByIdAndCustomer(requestId, customer)
                .orElseThrow(() -> new EntityNotFoundException("Service Request not found with ID: " + requestId + " for this customer."));
    }

    @Override
    public List<ServiceRequest> getServiceRequestsByCustomer(User customer) {
        return serviceRequestRepository.findByCustomer(customer);
    }

    @Override
    public List<ServiceRequest> getServiceRequestsByProvider(User provider) {
        return serviceRequestRepository.findByProvider(provider);
    }

    @Override
    public ServiceRequest cancelServiceRequest(Long id, User currentUser) {
        ServiceRequest serviceRequest = getServiceRequest(id);

        // Check if the current user is either the customer or the provider of the request
        boolean isCustomer = serviceRequest.getCustomer().getId().equals(currentUser.getId());
        boolean isProvider = serviceRequest.getProvider() != null && serviceRequest.getProvider().getId().equals(currentUser.getId());

        if (!isCustomer && !isProvider) {
            throw new AccessDeniedException("You are not authorized to cancel this service request.");
        }

        // Define which statuses can be cancelled by whom
        if (isCustomer && serviceRequest.getStatus() != ServiceStatus.PENDING) {
            throw new IllegalArgumentException("Customers can only cancel pending requests.");
        } else if (isProvider && (serviceRequest.getStatus() != ServiceStatus.PENDING && serviceRequest.getStatus() != ServiceStatus.ACCEPTED)) {
            throw new IllegalArgumentException("Providers can only cancel pending or accepted requests.");
        }

        serviceRequest.setStatus(ServiceStatus.CANCELLED);
        return serviceRequestRepository.save(serviceRequest);
    }

    @Override
    public ServiceRequest updateServiceRequest(Long id, ServiceRequest serviceRequest) {
        ServiceRequest existingRequest = getServiceRequest(id);
        // Implement logic to update fields based on serviceRequest
        existingRequest.setDescription(serviceRequest.getDescription());
        existingRequest.setDate(serviceRequest.getDate());
        existingRequest.setTime(serviceRequest.getTime());
        existingRequest.setContact(serviceRequest.getContact());
        existingRequest.setAddress(serviceRequest.getAddress());
        existingRequest.setServiceCharge(serviceRequest.getServiceCharge());
        existingRequest.setServiceName(serviceRequest.getServiceName());
        // Do not allow status changes directly through this method, use updateServiceRequestStatus
        return serviceRequestRepository.save(existingRequest);
    }

    @Override
    public void deleteServiceRequest(Long id) {
        if (!serviceRequestRepository.existsById(id)) {
            throw new EntityNotFoundException("Service Request not found with ID: " + id);
        }
        serviceRequestRepository.deleteById(id);
    }
}