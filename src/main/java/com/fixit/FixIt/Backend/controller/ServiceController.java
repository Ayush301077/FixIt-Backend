package com.fixit.FixIt.Backend.controller;

import com.fixit.FixIt.Backend.repository.UserRepository;
import com.fixit.FixIt.Backend.service.ServiceService;
import com.fixit.FixIt.Backend.model.User;
import com.fixit.FixIt.Backend.service.ServiceService;
import com.fixit.FixIt.Backend.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/services")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true", maxAge = 3600)
public class ServiceController {

    @Autowired
    private ServiceService serviceService;

    @Autowired
    private UserRepository userRepository; // To get the full User object

    // Add a new service for the authenticated provider
    @PostMapping
    public ResponseEntity<?> addService(@Valid @RequestBody com.fixit.FixIt.Backend.model.Service service, Authentication authentication) {
        try {
            String email = authentication.getName();
            User currentUser = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            com.fixit.FixIt.Backend.model.Service newService = serviceService.addService(currentUser.getId(), service);
            return ResponseEntity.status(HttpStatus.CREATED).body(newService);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to add service: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    // Get all services for the authenticated provider
    @GetMapping
    public ResponseEntity<?> getProviderServices(Authentication authentication) {
        try {
            String email = authentication.getName();
            User currentUser = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            List<com.fixit.FixIt.Backend.model.Service> services = serviceService.getServicesByProvider(currentUser.getId());
            return ResponseEntity.ok(services);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to fetch services: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    // Update an existing service for the authenticated provider
    @PutMapping("/{id}")
    public ResponseEntity<?> updateService(@PathVariable Long id, @Valid @RequestBody com.fixit.FixIt.Backend.model.Service serviceDetails, Authentication authentication) {
        try {
            String email = authentication.getName();
            User currentUser = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Ensure the service belongs to the current user
            com.fixit.FixIt.Backend.model.Service existingService = serviceService.getServicesByProvider(currentUser.getId())
                                          .stream()
                                          .filter(s -> s.getId().equals(id))
                                          .findFirst()
                                          .orElseThrow(() -> new RuntimeException("Service not found or not owned by user"));

            com.fixit.FixIt.Backend.model.Service updatedService = serviceService.updateService(id, serviceDetails);
            return ResponseEntity.ok(updatedService);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to update service: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    // Delete a service for the authenticated provider
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteService(@PathVariable Long id, Authentication authentication) {
        try {
            String email = authentication.getName();
            User currentUser = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Ensure the service belongs to the current user before deleting
            serviceService.getServicesByProvider(currentUser.getId())
                          .stream()
                          .filter(s -> s.getId().equals(id))
                          .findFirst()
                          .orElseThrow(() -> new RuntimeException("Service not found or not owned by user"));

            serviceService.deleteService(id);
            return ResponseEntity.ok().body(Map.of("message", "Service deleted successfully"));
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to delete service: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }
} 