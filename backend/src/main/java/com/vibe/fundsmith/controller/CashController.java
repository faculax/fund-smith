package com.vibe.fundsmith.controller;

import com.vibe.fundsmith.dto.CashBalanceDto;
import com.vibe.fundsmith.dto.CashResetResultDto;
import com.vibe.fundsmith.model.CashEntry;
import com.vibe.fundsmith.repository.CashLedgerRepository;
import com.vibe.fundsmith.service.CashService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/cash")
public class CashController {
    
    private final CashService cashService;
    private final CashLedgerRepository cashLedgerRepository;
    
    @Autowired
    public CashController(CashService cashService, CashLedgerRepository cashLedgerRepository) {
        this.cashService = cashService;
        this.cashLedgerRepository = cashLedgerRepository;
    }
    
    /**
     * Get current cash balance
     * 
     * @return Current cash balance DTO
     */
    @GetMapping
    public CashBalanceDto getCurrentBalance() {
        return cashService.getCurrentBalance();
    }
    
    /**
     * Get cash balance for a specific portfolio
     * 
     * @param portfolioId Portfolio identifier
     * @return Cash balance DTO
     */
    @GetMapping("/{portfolioId}")
    public CashBalanceDto getPortfolioCashBalance(@PathVariable String portfolioId) {
        return cashService.getCurrentBalance(portfolioId);
    }
    
    /**
     * Get cash history for a portfolio
     * 
     * @param portfolioId Portfolio identifier (optional, defaults to "DEFAULT")
     * @param limit Maximum number of entries (optional, defaults to 50)
     * @return List of cash entries
     */
    @GetMapping("/history")
    public List<CashEntry> getCashHistory(
            @RequestParam(required = false, defaultValue = "DEFAULT") String portfolioId,
            @RequestParam(required = false, defaultValue = "50") int limit) {
        
        return cashLedgerRepository.findByPortfolioIdOrderByCreatedAtDesc(portfolioId).stream()
                .limit(limit)
                .toList();
    }
    
    /**
     * Reset cash balance to a specified amount and clear all cash history
     * 
     * @param portfolioId Portfolio identifier (optional, defaults to "DEFAULT")
     * @param targetAmount Target amount to reset to (optional, defaults to 10,000,000.00)
     * @return Result of the reset operation
     */
    @PostMapping("/reset")
    public ResponseEntity<CashResetResultDto> resetCashBalance(
            @RequestParam(required = false, defaultValue = "DEFAULT") String portfolioId,
            @RequestParam(required = false, defaultValue = "10000000.00") BigDecimal targetAmount) {
        
        CashResetResultDto result = cashService.resetCashBalance(portfolioId, targetAmount);
        return ResponseEntity.ok(result);
    }
}