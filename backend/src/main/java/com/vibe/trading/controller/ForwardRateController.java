package com.vibe.trading.controller;

import com.vibe.trading.model.dto.ForwardRateCalculation;
import com.vibe.trading.service.InterestRateService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/forward-rates")
@Slf4j
public class ForwardRateController {

    @Autowired
    private InterestRateService interestRateService;

    // Mock spot rates for supported currency pairs
    private static final Map<String, BigDecimal> MOCK_SPOT_RATES = Map.of(
        "EUR/USD", BigDecimal.valueOf(1.0850),
        "USD/JPY", BigDecimal.valueOf(150.25),
        "GBP/EUR", BigDecimal.valueOf(1.1650),
        "AUD/USD", BigDecimal.valueOf(0.6750),
        "GBP/USD", BigDecimal.valueOf(1.2650)
    );

    @GetMapping("/calculate")
    public ResponseEntity<ForwardRateCalculation> calculateForwardRate(
            @RequestParam String currencyPair,
            @RequestParam String tenor) {
        
        try {
            log.info("Calculating forward rate for {} {}", currencyPair, tenor);
            
            // Parse currency pair
            String[] currencies = currencyPair.split("/");
            if (currencies.length != 2) {
                return ResponseEntity.badRequest().build();
            }
            
            String baseCurrency = currencies[0];
            String quoteCurrency = currencies[1];
            
            // Get spot rate from mock data
            BigDecimal spotRate = MOCK_SPOT_RATES.getOrDefault(currencyPair, BigDecimal.valueOf(1.0000));
            
            // Calculate forward rate
            BigDecimal forwardRate = interestRateService.calculateForwardRate(
                spotRate, baseCurrency, quoteCurrency, tenor);
            
            // Calculate forward points
            BigDecimal forwardPoints = interestRateService.calculateForwardPoints(spotRate, forwardRate);
            
            // Get interest rates
            BigDecimal baseRate = interestRateService.getInterestRate(baseCurrency);
            BigDecimal quoteRate = interestRateService.getInterestRate(quoteCurrency);
            
            ForwardRateCalculation calculation = ForwardRateCalculation.builder()
                .currencyPair(currencyPair)
                .tenor(tenor)
                .spotRate(spotRate)
                .forwardRate(forwardRate)
                .forwardPoints(forwardPoints)
                .baseCurrencyRate(baseRate)
                .quoteCurrencyRate(quoteRate)
                .calculationTime(LocalDateTime.now())
                .baseCurrency(baseCurrency)
                .quoteCurrency(quoteCurrency)
                .build();
            
            log.info("Forward rate calculation completed: {} {} = {}", 
                currencyPair, tenor, forwardRate);
            
            return ResponseEntity.ok(calculation);
            
        } catch (Exception e) {
            log.error("Error calculating forward rate: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/interest-rates")
    public ResponseEntity<Map<String, BigDecimal>> getInterestRates() {
        return ResponseEntity.ok(interestRateService.getCachedRates());
    }

    @GetMapping("/supported-tenors")
    public ResponseEntity<List<String>> getSupportedTenors() {
        List<String> tenors = List.of("1W", "1M", "3M", "6M", "1Y");
        return ResponseEntity.ok(tenors);
    }

    @GetMapping("/supported-pairs")
    public ResponseEntity<List<String>> getSupportedPairs() {
        List<String> pairs = List.of("EUR/USD", "USD/JPY", "GBP/EUR", "AUD/USD", "GBP/USD");
        return ResponseEntity.ok(pairs);
    }
} 