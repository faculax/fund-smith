package com.vibe.fundsmith.controller;

import com.vibe.fundsmith.service.DemoTradeGenerator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/auto-trades")
@ConditionalOnBean(DemoTradeGenerator.class)
public class DemoController {
    private final DemoTradeGenerator generator;

    public DemoController(DemoTradeGenerator generator) {
        this.generator = generator;
    }

    @PostMapping("/{action}")
    public ResponseEntity<String> control(@PathVariable String action) {
        switch (action.toLowerCase()) {
            case "start":
                generator.start();
                return ResponseEntity.ok("Demo trade generator started");
            case "stop":
                generator.stop();
                return ResponseEntity.ok("Demo trade generator stopped");
            default:
                return ResponseEntity.badRequest().body("Invalid action. Use 'start' or 'stop'");
        }
    }
}