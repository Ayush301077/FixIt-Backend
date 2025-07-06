package com.fixit.FixIt.Backend.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {
    @GetMapping("/healthz")
    public String healthz() {
        return "OK";
    }
} 