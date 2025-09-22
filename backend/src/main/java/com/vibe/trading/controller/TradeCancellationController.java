package com.vibe.trading.controller;

import com.vibe.trading.model.dto.TradeCancellationRequest;
import com.vibe.trading.model.dto.TradeCancellationResponse;
import com.vibe.trading.model.dto.TradeResponse;
import com.vibe.trading.service.TradeCancellationService;
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
public class TradeCancellationController {

    private final TradeCancellationService tradeCancellationService;

    @PostMapping("/{tradeId}/cancel")
    public ResponseEntity<TradeResponse> cancelTrade(
            @PathVariable String tradeId,
            @Valid @RequestBody TradeCancellationRequest request) {
        
        try {
            log.info("Received cancellation request for trade: {}", tradeId);
            
            // Set the trade ID from the path variable
            request.setTradeId(tradeId);
            
            TradeResponse cancelledTrade = tradeCancellationService.cancelTrade(request);
            
            return ResponseEntity.ok(cancelledTrade);
            
        } catch (IllegalStateException e) {
            log.error("Trade cannot be cancelled: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (IllegalArgumentException e) {
            log.error("Invalid cancellation request: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (RuntimeException e) {
            log.error("Error cancelling trade: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{tradeId}/cancellations")
    public ResponseEntity<List<TradeCancellationResponse>> getCancellationHistory(
            @PathVariable String tradeId) {
        
        try {
            log.info("Getting cancellation history for trade: {}", tradeId);
            
            List<TradeCancellationResponse> cancellations = tradeCancellationService.getCancellationHistory(tradeId);
            
            return ResponseEntity.ok(cancellations);
            
        } catch (Exception e) {
            log.error("Error getting cancellation history for trade {}: {}", tradeId, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{tradeId}/can-cancel")
    public ResponseEntity<Map<String, Boolean>> canTradeBeCancelled(
            @PathVariable String tradeId) {
        
        try {
            log.info("Checking if trade can be cancelled: {}", tradeId);
            
            boolean canCancel = tradeCancellationService.canTradeBeCancelled(tradeId);
            
            return ResponseEntity.ok(Map.of("canCancel", canCancel));
            
        } catch (Exception e) {
            log.error("Error checking if trade can be cancelled {}: {}", tradeId, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
}
