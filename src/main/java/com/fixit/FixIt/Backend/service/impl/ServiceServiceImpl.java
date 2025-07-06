package com.fixit.FixIt.Backend.service.impl;

import com.fixit.FixIt.Backend.model.User;
import com.fixit.FixIt.Backend.repository.ServiceRepository;
import com.fixit.FixIt.Backend.repository.UserRepository;
import com.fixit.FixIt.Backend.service.ServiceService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ServiceServiceImpl implements ServiceService {

    @Autowired
    private ServiceRepository serviceRepository;

    @Autowired
    private UserRepository userRepository;

    @Override
    public com.fixit.FixIt.Backend.model.Service addService(Long providerId, com.fixit.FixIt.Backend.model.Service service) {
        User provider = userRepository.findById(providerId)
                .orElseThrow(() -> new RuntimeException("Provider not found with id: " + providerId));
        service.setProvider(provider);
        return serviceRepository.save(service);
    }

    @Override
    public com.fixit.FixIt.Backend.model.Service updateService(Long serviceId, com.fixit.FixIt.Backend.model.Service serviceDetails) {
        com.fixit.FixIt.Backend.model.Service existingService = serviceRepository.findById(serviceId)
                .orElseThrow(() -> new RuntimeException("Service not found with id: " + serviceId));
        existingService.setName(serviceDetails.getName());
        existingService.setCharge(serviceDetails.getCharge());
        return serviceRepository.save(existingService);
    }

    @Override
    public void deleteService(Long serviceId) {
        if (!serviceRepository.existsById(serviceId)) {
            throw new EntityNotFoundException("Service not found with id: " + serviceId);
        }
        serviceRepository.deleteById(serviceId);
    }

    @Override
    public List<com.fixit.FixIt.Backend.model.Service> getServicesByProvider(Long providerId) {
        return serviceRepository.findByProviderId(providerId);
    }
} 