package com.fixit.FixIt.Backend.controller;

import com.fixit.FixIt.Backend.exception.ResourceNotFoundException;
import com.fixit.FixIt.Backend.model.Favourite;
import com.fixit.FixIt.Backend.model.User;
import com.fixit.FixIt.Backend.repository.UserRepository;
import com.fixit.FixIt.Backend.service.FavouriteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/favourites")
public class FavouriteController {

    @Autowired
    private FavouriteService favouriteService;

    @Autowired
    private UserRepository userRepository;

    private User getAuthenticatedUser(UserDetails userDetails) {
        return userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    @PostMapping("/add/{providerId}")
    public ResponseEntity<Favourite> addFavourite(@AuthenticationPrincipal UserDetails userDetails, @PathVariable Long providerId) {
        User customer = getAuthenticatedUser(userDetails);
        User provider = userRepository.findById(providerId)
                .orElseThrow(() -> new ResourceNotFoundException("Provider not found"));

        Favourite favourite = favouriteService.addFavourite(customer, provider);
        return new ResponseEntity<>(favourite, HttpStatus.CREATED);
    }

    @DeleteMapping("/remove/{providerId}")
    public ResponseEntity<Void> removeFavourite(@AuthenticationPrincipal UserDetails userDetails, @PathVariable Long providerId) {
        User customer = getAuthenticatedUser(userDetails);
        User provider = userRepository.findById(providerId)
                .orElseThrow(() -> new ResourceNotFoundException("Provider not found"));

        favouriteService.removeFavourite(customer, provider);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping
    public ResponseEntity<List<User>> getCustomerFavourites(@AuthenticationPrincipal UserDetails userDetails) {
        User customer = getAuthenticatedUser(userDetails);
        System.out.println("Authenticated customer ID: " + customer.getId()); // Debugging line
        List<Favourite> favourites = favouriteService.getCustomerFavourites(customer);
        System.out.println("Number of favourites fetched from service: " + favourites.size()); // Debugging line
        
        // Initialize the providers to avoid lazy loading issues
        List<User> favouriteProviders = favourites.stream()
                .map(Favourite::getProvider)
                .map(provider -> {
                    // Force initialization of the provider
                    User initializedProvider = userRepository.findById(provider.getId())
                            .orElseThrow(() -> new ResourceNotFoundException("Provider not found"));
                    // Initialize any lazy-loaded collections
                    if (initializedProvider.getServices() != null) {
                        initializedProvider.getServices().size();
                    }
                    return initializedProvider;
                })
                .collect(Collectors.toList());
        
        return new ResponseEntity<>(favouriteProviders, HttpStatus.OK);
    }

    @GetMapping("/is-favourite/{providerId}")
    public ResponseEntity<Boolean> isFavourite(@AuthenticationPrincipal UserDetails userDetails, @PathVariable Long providerId) {
        User customer = getAuthenticatedUser(userDetails);
        User provider = userRepository.findById(providerId)
                .orElseThrow(() -> new ResourceNotFoundException("Provider not found"));
        boolean isFav = favouriteService.isFavourite(customer, provider);
        return new ResponseEntity<>(isFav, HttpStatus.OK);
    }
} 