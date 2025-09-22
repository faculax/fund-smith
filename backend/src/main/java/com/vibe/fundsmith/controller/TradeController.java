package com.vibe.fundsmith.controller;

import com.vibe.fundsmith.dto.TradeRequest;
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

@RestController
@RequestMapping("/api/trades")
public class TradeController {
    private final TradeService tradeService;

    public TradeController(TradeService tradeService) {
        this.tradeService = tradeService;
    }

    @PostMapping
    public ResponseEntity<?> bookTrade(@RequestBody TradeRequest request) {
        try {
            Trade trade = tradeService.bookTrade(request);
            return ResponseEntity.ok(trade);
        } catch (ValidationException e) {
            return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Map.of("field", e.getField(), "message", e.getMessage()));
        }
    }

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