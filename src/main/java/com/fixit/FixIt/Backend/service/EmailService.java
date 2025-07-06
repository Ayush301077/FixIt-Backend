package com.fixit.FixIt.Backend.service;

public interface EmailService {
    void sendSimpleEmail(String to, String subject, String text);
} 