package com.vibe.trading.controller;

import com.vibe.trading.model.Trade;
import com.vibe.trading.model.dto.TradeRequest;
import com.vibe.trading.model.dto.TradeResponse;
import com.vibe.trading.service.TradeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/trades")
@RequiredArgsConstructor
@Slf4j
public class TradeController {

    private final TradeService tradeService;

    @PostMapping
    public ResponseEntity<TradeResponse> createTrade(@Valid @RequestBody TradeRequest request) {
        log.info("Received trade creation request: {}", request);
        try {
            TradeResponse response = tradeService.createTrade(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            log.error("Error creating trade: {}", e.getMessage());
            throw e;
        }
    }

    @GetMapping
    public ResponseEntity<List<TradeResponse>> getAllTrades() {
        List<TradeResponse> trades = tradeService.getAllTrades();
        return ResponseEntity.ok(trades);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TradeResponse> getTradeById(@PathVariable Long id) {
        return tradeService.getTradeById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/trade-id/{tradeId}")
    public ResponseEntity<TradeResponse> getTradeByTradeId(@PathVariable String tradeId) {
        return tradeService.getTradeByTradeId(tradeId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/date/{tradeDate}")
    public ResponseEntity<List<TradeResponse>> getTradesByDate(@PathVariable String tradeDate) {
        LocalDate date = LocalDate.parse(tradeDate);
        List<TradeResponse> trades = tradeService.getTradesByDate(date);
        return ResponseEntity.ok(trades);
    }

    @GetMapping("/type/{tradeType}")
    public ResponseEntity<List<TradeResponse>> getTradesByType(@PathVariable Trade.TradeType tradeType) {
        List<TradeResponse> trades = tradeService.getTradesByType(tradeType);
        return ResponseEntity.ok(trades);
    }

    @PutMapping("/{tradeId}/status")
    public ResponseEntity<TradeResponse> updateTradeStatus(
            @PathVariable String tradeId,
            @RequestParam Trade.TradeStatus status) {
        try {
            TradeResponse response = tradeService.updateTradeStatus(tradeId, status);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error updating trade status: {}", e.getMessage());
            throw e;
        }
    }

    // User-based filtering endpoints for Story 34.5
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<TradeResponse>> getTradesByUser(@PathVariable String userId) {
        List<TradeResponse> trades = tradeService.getTradesByUser(userId);
        return ResponseEntity.ok(trades);
    }

    @GetMapping("/username/{username}")
    public ResponseEntity<List<TradeResponse>> getTradesByUsername(@PathVariable String username) {
        List<TradeResponse> trades = tradeService.getTradesByUsername(username);
        return ResponseEntity.ok(trades);
    }

    // Debug endpoint to check user tracking fields
    @GetMapping("/debug/user-tracking")
    public ResponseEntity<Map<String, Object>> debugUserTracking() {
        List<TradeResponse> allTrades = tradeService.getAllTrades();
        Map<String, Object> debugInfo = new HashMap<>();
        
        debugInfo.put("totalTrades", allTrades.size());
        debugInfo.put("tradesWithUserTracking", allTrades.stream()
            .filter(t -> t.getBookedByUserId() != null && !t.getBookedByUserId().isEmpty())
            .count());
        debugInfo.put("tradesWithoutUserTracking", allTrades.stream()
            .filter(t -> t.getBookedByUserId() == null || t.getBookedByUserId().isEmpty())
            .count());
        
        // Sample of trades with their user tracking info
        List<Map<String, Object>> sampleTrades = allTrades.stream()
            .limit(5)
            .map(trade -> {
                Map<String, Object> tradeInfo = new HashMap<>();
                tradeInfo.put("tradeId", trade.getTradeId());
                tradeInfo.put("createdBy", trade.getCreatedBy());
                tradeInfo.put("bookedByUserId", trade.getBookedByUserId());
                tradeInfo.put("bookedByUsername", trade.getBookedByUsername());
                return tradeInfo;
            })
            .collect(Collectors.toList());
        
        debugInfo.put("sampleTrades", sampleTrades);
        
        return ResponseEntity.ok(debugInfo);
    }

    // New endpoint for trade creation history
    @GetMapping("/{tradeId}/creation")
    public ResponseEntity<Map<String, Object>> getTradeCreation(@PathVariable String tradeId) {
        try {
            log.info("Getting creation details for trade: {}", tradeId);
            
            Optional<TradeResponse> tradeOpt = tradeService.getTradeByTradeId(tradeId);
            if (tradeOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            TradeResponse trade = tradeOpt.get();
            Map<String, Object> creationDetails = new HashMap<>();
            creationDetails.put("tradeId", trade.getTradeId());
            creationDetails.put("createdAt", trade.getCreatedAt());
            creationDetails.put("createdBy", trade.getCreatedBy());
            creationDetails.put("tradeType", trade.getTradeType());
            creationDetails.put("currencyPair", trade.getCurrencyPair());
            creationDetails.put("direction", trade.getDirection());
            creationDetails.put("notionalAmount", trade.getNotionalAmount());
            creationDetails.put("rate", trade.getRate());
            creationDetails.put("counterparty", trade.getCounterparty());
            creationDetails.put("valueDate", trade.getValueDate());
            creationDetails.put("status", trade.getStatus());
            
            return ResponseEntity.ok(creationDetails);
            
        } catch (Exception e) {
            log.error("Error getting creation details for trade {}: {}", tradeId, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
}
