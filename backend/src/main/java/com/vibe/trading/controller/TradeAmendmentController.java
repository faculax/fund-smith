package com.vibe.trading.controller;

import com.vibe.trading.model.dto.TradeAmendmentRequest;
import com.vibe.trading.model.dto.TradeAmendmentResponse;
import com.vibe.trading.model.dto.TradeResponse;
import com.vibe.trading.service.TradeAmendmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/trades")
@RequiredArgsConstructor
@Slf4j
public class TradeAmendmentController {

    private final TradeAmendmentService tradeAmendmentService;

    @PostMapping("/{tradeId}/amend")
    public ResponseEntity<TradeResponse> amendTrade(
            @PathVariable String tradeId,
            @Valid @RequestBody TradeAmendmentRequest request) {
        
        try {
            log.info("Received amendment request for trade: {}", tradeId);
            
            // Set the trade ID from the path variable
            request.setTradeId(tradeId);
            
            TradeResponse amendedTrade = tradeAmendmentService.amendTrade(request);
            
            return ResponseEntity.ok(amendedTrade);
            
        } catch (IllegalStateException e) {
            log.error("Trade cannot be amended: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (IllegalArgumentException e) {
            log.error("Invalid amendment request: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (RuntimeException e) {
            log.error("Error amending trade: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{tradeId}/amendments")
    public ResponseEntity<List<TradeAmendmentResponse>> getAmendmentHistory(
            @PathVariable String tradeId) {
        
        try {
            log.info("Getting amendment history for trade: {}", tradeId);
            
            List<TradeAmendmentResponse> amendments = tradeAmendmentService.getAmendmentHistory(tradeId);
            
            return ResponseEntity.ok(amendments);
            
        } catch (Exception e) {
            log.error("Error getting amendment history for trade {}: {}", tradeId, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{tradeId}/can-amend")
    public ResponseEntity<Map<String, Boolean>> canTradeBeAmended(
            @PathVariable String tradeId) {
        
        try {
            log.info("Checking if trade can be amended: {}", tradeId);
            
            boolean canAmend = tradeAmendmentService.canTradeBeAmended(tradeId);
            
            return ResponseEntity.ok(Map.of("canAmend", canAmend));
            
        } catch (Exception e) {
            log.error("Error checking if trade can be amended {}: {}", tradeId, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
}
