package com.fixit.FixIt.Backend.repository;

import com.fixit.FixIt.Backend.model.ServiceRequest;
import com.fixit.FixIt.Backend.model.ServiceStatus;
import com.fixit.FixIt.Backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional; // IMPORTANT: Add this import

@Repository
public interface ServiceRequestRepository extends JpaRepository<ServiceRequest, Long> {
    List<ServiceRequest> findByCustomer(User customer);
    List<ServiceRequest> findByProvider(User provider);
    List<ServiceRequest> findByStatus(ServiceStatus status);
    List<ServiceRequest> findByProviderAndStatus(User provider, ServiceStatus status);
    Optional<ServiceRequest> findByIdAndCustomer(Long id, User customer); // IMPORTANT: Added this method
}