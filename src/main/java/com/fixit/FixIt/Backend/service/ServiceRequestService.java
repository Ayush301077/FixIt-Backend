package com.fixit.FixIt.Backend.service;

import com.fixit.FixIt.Backend.dto.CreateServiceRequestDto;
import com.fixit.FixIt.Backend.model.ServiceRequest;
import com.fixit.FixIt.Backend.model.ServiceStatus;
import com.fixit.FixIt.Backend.model.User;

import java.util.List;

public interface ServiceRequestService {
    ServiceRequest createServiceRequest(CreateServiceRequestDto requestDto, User customer);
    ServiceRequest updateServiceRequest(Long id, ServiceRequest serviceRequest);
    void deleteServiceRequest(Long id);
    ServiceRequest getServiceRequest(Long id);
    List<ServiceRequest> getAllServiceRequests();
    List<ServiceRequest> getServiceRequestsByCustomer(User customer);
    List<ServiceRequest> getServiceRequestsByProvider(User provider);
    List<ServiceRequest> getServiceRequestsByStatus(ServiceStatus status);
    ServiceRequest updateServiceRequestStatus(Long id, ServiceStatus status);
    ServiceRequest getServiceRequestByIdAndCustomer(Long requestId, User customer);
    ServiceRequest cancelServiceRequest(Long id, User currentUser);
} 