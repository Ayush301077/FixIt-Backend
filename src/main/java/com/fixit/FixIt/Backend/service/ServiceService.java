package com.fixit.FixIt.Backend.service;

import com.fixit.FixIt.Backend.model.User;

import java.util.List;

public interface ServiceService {
    com.fixit.FixIt.Backend.model.Service addService(Long providerId, com.fixit.FixIt.Backend.model.Service service);
    com.fixit.FixIt.Backend.model.Service updateService(Long serviceId, com.fixit.FixIt.Backend.model.Service serviceDetails);
    void deleteService(Long serviceId);
    List<com.fixit.FixIt.Backend.model.Service> getServicesByProvider(Long providerId);
} 