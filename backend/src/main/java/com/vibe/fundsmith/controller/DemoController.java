package com.vibe.fundsmith.controller;

import com.vibe.fundsmith.service.DemoTradeGenerator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("api/admin/auto-trades")
public class DemoController {
    private static final Logger log = LoggerFactory.getLogger(DemoController.class);
    private final DemoTradeGenerator generator;

    public DemoController(DemoTradeGenerator generator) {
        this.generator = generator;
        log.info("DemoController initialized with generator: {}", generator.getClass().getName());
    }

    @PostMapping("/{action}")
    public ResponseEntity<String> control(@PathVariable String action) {
        log.info("Received action: {}", action);
        try {
            switch (action.toLowerCase()) {
                case "start":
                    log.info("Starting demo trade generator");
                    generator.start();
                    return ResponseEntity.ok("Demo trade generator started");
                case "stop":
                    log.info("Stopping demo trade generator");
                    generator.stop();
                    return ResponseEntity.ok("Demo trade generator stopped");
                case "backdated-mode":
                    log.info("Enabling backdated trade mode");
                    generator.enableBackdatedTradeMode();
                    return ResponseEntity.ok("Backdated trade mode enabled - trades will be generated with settlement date = today");
                case "regular-mode":
                    log.info("Enabling regular trade mode");
                    generator.enableRegularTradeMode();
                    return ResponseEntity.ok("Regular trade mode enabled - trades will be generated with settlement date = trade date + 2 business days");
                case "stopped-mode":
                    log.info("Enabling stopped mode");
                    generator.enableStoppedMode();
                    return ResponseEntity.ok("Stopped mode enabled - no trades will be generated even if the scheduler is running");
                default:
                    log.warn("Invalid action received: {}", action);
                    return ResponseEntity.badRequest().body("Invalid action. Use 'start', 'stop', 'backdated-mode', 'regular-mode', or 'stopped-mode'");
            }
        } catch (Exception e) {
            log.error("Error processing action: " + action, e);
            return ResponseEntity.status(500).body("Error processing request: " + e.getMessage());
        }
    }
    
    @GetMapping("/status")
    public ResponseEntity<java.util.Map<String, Object>> getStatus() {
        log.info("Getting demo trade generator status");
        try {
            java.util.Map<String, Object> status = new java.util.HashMap<>();
            status.put("running", generator.isRunning());
            status.put("mode", generator.getGenerationMode().toString());
            status.put("tradeCount", generator.getTradeCount());
            log.info("Status: running={}, mode={}, tradeCount={}",
                generator.isRunning(), generator.getGenerationMode(), generator.getTradeCount());
            return ResponseEntity.ok(status);
        } catch (Exception e) {
            log.error("Error getting status", e);
            java.util.Map<String, Object> errorStatus = new java.util.HashMap<>();
            errorStatus.put("error", e.getMessage());
            return ResponseEntity.status(500).body(errorStatus);
        }
    }
    
    @GetMapping("/ping")
    public ResponseEntity<String> ping() {
        log.info("Ping endpoint called");
        return ResponseEntity.ok("Pong from DemoController");
    }
}