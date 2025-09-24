package com.vibe.fundsmith.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.HashMap;
import java.util.Map;

/**
 * Controller for system health endpoints
 */
@RestController
@RequestMapping({"/api/health", "/health"}) // Support both paths
@CrossOrigin(origins = "*")
public class HealthController {
    
    private static final Logger logger = LoggerFactory.getLogger(HealthController.class);
    
    @Value("${spring.application.name:trading-platform}")
    private String applicationName;
    
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getStatus() {
        logger.info("Health status endpoint called");
        Map<String, Object> response = new HashMap<>();
        
        response.put("status", "UP");
        response.put("serviceName", applicationName);
        response.put("timestamp", System.currentTimeMillis());
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/ping")
    public ResponseEntity<Map<String, String>> ping() {
        logger.info("Health ping endpoint called");
        Map<String, String> response = new HashMap<>();
        response.put("message", "pong");
        return ResponseEntity.ok(response);
    }
    
    // Root endpoint for simple health check
    @GetMapping({"", "/"})
    public ResponseEntity<Map<String, Object>> root() {
        logger.info("Health root endpoint called");
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", applicationName);
        return ResponseEntity.ok(response);
    }
}