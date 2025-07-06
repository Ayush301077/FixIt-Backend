package com.fixit.FixIt.Backend.controller;

import com.fixit.FixIt.Backend.model.Role;
import com.fixit.FixIt.Backend.model.Service;
import com.fixit.FixIt.Backend.model.User;
import com.fixit.FixIt.Backend.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(Authentication authentication) {
        String email = authentication.getName();
        User currentUser = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found"));
        return ResponseEntity.ok(currentUser);
    }

    @GetMapping("/providers")
    public ResponseEntity<List<User>> getAllProviders() {
        List<User> providers = userRepository.findAll()
            .stream()
            .filter(user -> user.getRole() == Role.PROVIDER)
            .toList();
        return ResponseEntity.ok(providers);
    }

    @GetMapping("/providers/{id}")
    public ResponseEntity<?> getProviderById(@PathVariable Long id) {
        User provider = userRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Provider not found"));
        
        if (provider.getRole() != Role.PROVIDER) {
            return ResponseEntity.badRequest().body("User is not a service provider");
        }

        // Explicitly initialize the lazy-loaded services collection
        // This will fetch the services from the database while the session is still open
        if (provider.getServices() != null) {
            provider.getServices().size(); // Force initialization
        }
        
        return ResponseEntity.ok(provider);
    }

    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(@RequestBody User updatedUser, Authentication authentication) {
        String email = authentication.getName();
        User currentUser = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found"));

        // Update allowed fields
        currentUser.setName(updatedUser.getName());
        currentUser.setContact(updatedUser.getContact());
        currentUser.setArea(updatedUser.getArea());
        currentUser.setCity(updatedUser.getCity());

        // Handle services for providers
        if (currentUser.getRole() == com.fixit.FixIt.Backend.model.Role.PROVIDER) {
            // Map existing services by ID for quick lookup
            java.util.Map<Long, com.fixit.FixIt.Backend.model.Service> existingServicesMap = new java.util.HashMap<>();
            for (com.fixit.FixIt.Backend.model.Service service : currentUser.getServices()) {
                if (service.getId() != null) {
                    existingServicesMap.put(service.getId(), service);
                }
            }

            // Create a new list for updated services to replace the existing one
            java.util.List<com.fixit.FixIt.Backend.model.Service> updatedServicesList = new java.util.ArrayList<>();

            if (updatedUser.getServices() != null) {
                for (com.fixit.FixIt.Backend.model.Service incomingService : updatedUser.getServices()) {
                    if (incomingService.getId() != null && existingServicesMap.containsKey(incomingService.getId())) {
                        // This is an existing service, update its details
                        com.fixit.FixIt.Backend.model.Service existingService = existingServicesMap.get(incomingService.getId());
                        existingService.setName(incomingService.getName());
                        existingService.setCharge(incomingService.getCharge());
                        updatedServicesList.add(existingService); // Add the managed and updated service
                    } else {
                        // This is a new service (either no ID or a client-generated temporary ID)
                        com.fixit.FixIt.Backend.model.Service newService = new com.fixit.FixIt.Backend.model.Service();
                        newService.setName(incomingService.getName());
                        newService.setCharge(incomingService.getCharge());
                        newService.setProvider(currentUser); // Link to the current user
                        updatedServicesList.add(newService); // Add the newly created service
                    }
                }
            }

            // Clear the existing collection and add all services from the updated list
            // This will trigger orphanRemoval for services not in updatedServicesList
            currentUser.getServices().clear();
            currentUser.getServices().addAll(updatedServicesList);
        }

        userRepository.save(currentUser);
        return ResponseEntity.ok(currentUser);
    }
} 