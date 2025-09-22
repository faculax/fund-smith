package com.vibe.trading.controller;

import com.vibe.trading.model.Trade;
import com.vibe.trading.model.dto.TradeResponse;
import com.vibe.trading.service.TradeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/trade-blotter")
@RequiredArgsConstructor
@Slf4j
public class TradeBlotterController {

    private final TradeService tradeService;

    @GetMapping("/all")
    public ResponseEntity<List<TradeResponse>> getAllTrades() {
        log.info("Fetching all trades for blotter");
        List<TradeResponse> trades = tradeService.getAllTrades();
        return ResponseEntity.ok(trades);
    }

    @GetMapping("/by-status/{status}")
    public ResponseEntity<List<TradeResponse>> getTradesByStatus(@PathVariable Trade.TradeStatus status) {
        log.info("Fetching trades by status: {}", status);
        List<TradeResponse> trades = tradeService.getTradesByStatus(status);
        return ResponseEntity.ok(trades);
    }

    @GetMapping("/by-date-range")
    public ResponseEntity<List<TradeResponse>> getTradesByDateRange(
            @RequestParam String startDate,
            @RequestParam String endDate) {
        log.info("Fetching trades by date range: {} to {}", startDate, endDate);
        LocalDate start = LocalDate.parse(startDate);
        LocalDate end = LocalDate.parse(endDate);
        List<TradeResponse> trades = tradeService.getTradesByDateRange(start, end);
        return ResponseEntity.ok(trades);
    }

    @GetMapping("/by-counterparty/{counterparty}")
    public ResponseEntity<List<TradeResponse>> getTradesByCounterparty(@PathVariable String counterparty) {
        log.info("Fetching trades by counterparty: {}", counterparty);
        List<TradeResponse> trades = tradeService.getTradesByCounterparty(counterparty);
        return ResponseEntity.ok(trades);
    }

    @GetMapping("/by-currency-pair/{currencyPair}")
    public ResponseEntity<List<TradeResponse>> getTradesByCurrencyPair(@PathVariable String currencyPair) {
        log.info("Fetching trades by currency pair: {}", currencyPair);
        List<TradeResponse> trades = tradeService.getTradesByCurrencyPair(currencyPair);
        return ResponseEntity.ok(trades);
    }

    @GetMapping("/summary")
    public ResponseEntity<Map<String, Object>> getTradeSummary() {
        log.info("Fetching trade summary");
        Map<String, Object> summary = tradeService.getTradeSummary();
        return ResponseEntity.ok(summary);
    }
}
