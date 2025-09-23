package com.vibe.fundsmith.controller;

import com.vibe.fundsmith.dto.JournalDto;
import com.vibe.fundsmith.exception.UnbalancedJournalException;
import com.vibe.fundsmith.service.JournalService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class JournalController {
    private static final Logger log = LoggerFactory.getLogger(JournalController.class);
    
    private final JournalService journalService;
    
    @Autowired
    public JournalController(JournalService journalService) {
        this.journalService = journalService;
    }
    
    /**
     * Get journals for a trade
     * 
     * @param tradeId The trade ID
     * @return List of journal DTOs
     */
    @GetMapping("/journals")
    public ResponseEntity<List<JournalDto>> getJournals(@RequestParam(required = true) String tradeId) {
        try {
            UUID tradeUuid = UUID.fromString(tradeId);
            List<JournalDto> journals = journalService.getJournalsForTrade(tradeUuid);
            return ResponseEntity.ok(journals);
        } catch (IllegalArgumentException e) {
            log.error("Invalid trade ID format: {}", tradeId);
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error getting journals for trade {}: {}", tradeId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Get recent journals
     * 
     * @return List of recent journal DTOs
     */
    @GetMapping("/journals/recent")
    public ResponseEntity<List<JournalDto>> getRecentJournals() {
        try {
            List<JournalDto> journals = journalService.getRecentJournals();
            return ResponseEntity.ok(journals);
        } catch (Exception e) {
            log.error("Error getting recent journals: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Process settlements for today
     * 
     * @return Number of trades processed
     */
    @PostMapping("/journals/process-settlements")
    public ResponseEntity<Map<String, Object>> processSettlements(
            @RequestParam(required = false) String date) {
        
        try {
            LocalDate settleDate = date != null ? LocalDate.parse(date) : LocalDate.now();
            int processed = journalService.processSettlements(settleDate);
            
            Map<String, Object> response = new HashMap<>();
            response.put("processed", processed);
            response.put("date", settleDate.toString());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error processing settlements: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Handle unbalanced journal exceptions
     */
    @ExceptionHandler(UnbalancedJournalException.class)
    public ResponseEntity<Map<String, String>> handleUnbalancedJournalException(
            UnbalancedJournalException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("error", UnbalancedJournalException.ERROR_CODE);
        error.put("message", ex.getMessage());
        
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(error);
    }
}