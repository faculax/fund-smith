package com.vibe.fundsmith.controller;

import com.vibe.fundsmith.dto.TradeRequest;
import com.vibe.fundsmith.dto.TradeResponse;
import com.vibe.fundsmith.exception.ValidationException;
import com.vibe.fundsmith.model.Trade;
import com.vibe.fundsmith.service.TradeService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Optional;

@RestController
@RequestMapping("/api/trades")
public class TradeController {
    private final TradeService tradeService;

    public TradeController(TradeService tradeService) {
        this.tradeService = tradeService;
    }

    /**
     * Book a new trade with idempotency support
     */
    @PostMapping
    public ResponseEntity<?> bookTrade(@RequestBody TradeRequest request) {
        try {
            TradeResponse response = tradeService.bookTrade(request);
            return ResponseEntity.ok(response);
        } catch (ValidationException e) {
            return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Map.of("field", e.getField(), "message", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("message", e.getMessage()));
        }
    }

    /**
     * List trades with optional filtering
     */
    @GetMapping
    public List<Trade> listTrades(
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
        @RequestParam(required = false) String isin,
        @RequestParam(required = false) Integer limit
    ) {
        return tradeService.findTrades(fromDate, toDate, isin, limit);
    }
    
    /**
     * Get a trade by its trade ID
     */
    @GetMapping("/{tradeId}")
    public ResponseEntity<?> getTradeById(@PathVariable UUID tradeId) {
        Optional<Trade> trade = tradeService.findTradeByTradeId(tradeId);
        return trade.map(ResponseEntity::ok)
                  .orElseGet(() -> ResponseEntity.notFound().build());
    }
    
    /**
     * Clears all trades from the database
     * @return A response indicating how many trades were deleted
     */
    @DeleteMapping
    public ResponseEntity<Map<String, Object>> clearAllTrades() {
        long deletedCount = tradeService.deleteAllTrades();
        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "All trades have been cleared",
            "deletedCount", deletedCount
        ));
    }
}