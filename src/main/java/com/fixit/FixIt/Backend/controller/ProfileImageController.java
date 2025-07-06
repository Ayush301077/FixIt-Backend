package com.fixit.FixIt.Backend.controller;

import com.fixit.FixIt.Backend.model.User;
import com.fixit.FixIt.Backend.service.FileStorageService;
import com.fixit.FixIt.Backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

import com.fixit.FixIt.Backend.repository.UserRepository;

@RestController
@RequestMapping("/api/profile")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true", maxAge = 3600)
public class ProfileImageController {

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/image")
    public ResponseEntity<?> uploadProfileImage(@RequestParam("file") MultipartFile file, Authentication authentication) {
        try {
            // Validate file
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body("Please select a file to upload");
            }

            // Check file type
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return ResponseEntity.badRequest().body("Only image files are allowed");
            }

            // Get current user from DB using email
            String email = authentication.getName();
            User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

            // Delete old profile image if exists
            if (currentUser.getProfileImagePath() != null) {
                try {
                    fileStorageService.deleteFile(currentUser.getProfileImagePath());
                } catch (Exception e) {
                    // Log the error but continue with the upload
                    System.err.println("Error deleting old profile image: " + e.getMessage());
                }
            }

            // Save the file and get the file path
            String filePath = fileStorageService.storeFile(file, currentUser.getId().toString());

            // Update user's profile image path
            currentUser.setProfileImagePath(filePath);
            userRepository.save(currentUser);

            // Return the updated user object with the image path
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Profile image uploaded successfully");
            response.put("user", currentUser);
            response.put("imagePath", filePath);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", "Failed to upload image: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }

    @DeleteMapping("/image")
    public ResponseEntity<?> deleteProfileImage(Authentication authentication) {
        try {
            String email = authentication.getName();
            User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

            if (currentUser.getProfileImagePath() != null) {
                try {
                    fileStorageService.deleteFile(currentUser.getProfileImagePath());
                } catch (Exception e) {
                    System.err.println("Error deleting profile image file: " + e.getMessage());
                }
                
                currentUser.setProfileImagePath(null);
                userRepository.save(currentUser);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Profile image deleted successfully");
            response.put("user", currentUser);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", "Failed to delete image: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }
} 