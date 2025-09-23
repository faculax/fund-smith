package com.vibe.fundsmith.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vibe.fundsmith.dto.CashBalanceDto;
import com.vibe.fundsmith.dto.CashResetResultDto;
import com.vibe.fundsmith.event.CashMovementEvent;
import com.vibe.fundsmith.model.CashEntry;
import com.vibe.fundsmith.model.TradeSide;
import com.vibe.fundsmith.repository.CashLedgerRepository;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.ZonedDateTime;
import java.util.UUID;

@Service
public class CashService {
    private static final Logger log = LoggerFactory.getLogger(CashService.class);
    
    private final CashLedgerRepository cashLedgerRepository;
    private final ObjectMapper objectMapper;
    private final String defaultCurrency;
    
    @Autowired
    public CashService(
            CashLedgerRepository cashLedgerRepository,
            ObjectMapper objectMapper,
            @Value("${ibor.cash.default-currency:USD}") String defaultCurrency) {
        this.cashLedgerRepository = cashLedgerRepository;
        this.objectMapper = objectMapper;
        this.defaultCurrency = defaultCurrency;
    }
    
    /**
     * Record a cash movement for a trade
     * 
     * @param tradeId Unique trade identifier
     * @param side Trade side (BUY/SELL)
     * @param quantity Trade quantity
     * @param price Trade price
     * @param portfolioId Portfolio identifier
     * @return Cash entry created
     */
    @Transactional
    public CashEntry recordTradeImpact(UUID tradeId, TradeSide side, 
                                      BigDecimal quantity, BigDecimal price, String portfolioId) {
        // Calculate cash impact based on side
        // BUY: negative impact (cash decreases)
        // SELL: positive impact (cash increases)
        BigDecimal totalCost = quantity.multiply(price).setScale(2, RoundingMode.HALF_UP);
        BigDecimal cashDelta = (side == TradeSide.BUY) ? totalCost.negate() : totalCost;
        
        // Format reason (e.g., "BUY:a5f0...")
        String reason = side.toString() + ":" + tradeId.toString();
        
        // Record in ledger
        CashEntry entry = new CashEntry(portfolioId, cashDelta, reason);
        CashEntry savedEntry = cashLedgerRepository.save(entry);
        
        // Emit cash movement event
        emitCashMovementEvent(tradeId, cashDelta, reason, savedEntry.getCreatedAt());
        
        log.info("Recorded cash movement for trade {}: delta={}", tradeId, cashDelta);
        return savedEntry;
    }
    
    /**
     * Get current cash balance for default portfolio
     * 
     * @return Cash balance DTO
     */
    public CashBalanceDto getCurrentBalance() {
        return getCurrentBalance("DEFAULT");
    }
    
    /**
     * Get current cash balance for a specific portfolio
     * 
     * @param portfolioId Portfolio identifier
     * @return Cash balance DTO
     */
    public CashBalanceDto getCurrentBalance(String portfolioId) {
        BigDecimal balance = cashLedgerRepository.getCurrentBalance(portfolioId);
        return new CashBalanceDto(
            balance.setScale(2, RoundingMode.HALF_UP).toString(),
            defaultCurrency
        );
    }
    
    /**
     * Emit cash movement event
     */
    private void emitCashMovementEvent(UUID tradeId, BigDecimal delta, String reason, ZonedDateTime createdAt) {
        CashMovementEvent event = new CashMovementEvent(tradeId, delta, reason, createdAt);
        try {
            // For now, just log the event as JSON
            log.info("EVENT: {}", objectMapper.writeValueAsString(event));
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize cash movement event", e);
        }
        // In the future, publish to message bus or event stream
    }
    
    /**
     * Reset cash balance to a specified amount for a portfolio and clear all history
     * 
     * @param portfolioId Portfolio identifier
     * @param targetAmount Target amount to reset to
     * @return Result of the reset operation
     */
    @Transactional
    public CashResetResultDto resetCashBalance(String portfolioId, BigDecimal targetAmount) {
        try {
            // Delete all cash entries for this portfolio to clear history
            List<CashEntry> existingEntries = cashLedgerRepository.findByPortfolioIdOrderByCreatedAtDesc(portfolioId);
            int entriesDeleted = existingEntries.size();
            if (!existingEntries.isEmpty()) {
                cashLedgerRepository.deleteAll(existingEntries);
                log.info("Deleted {} cash entries for portfolio {}", entriesDeleted, portfolioId);
            }
            
            // Create a new initial balance entry
            CashEntry initialEntry = new CashEntry(
                portfolioId, 
                targetAmount, // Direct amount instead of adjustment 
                targetAmount, // Running balance is the same as initial amount
                defaultCurrency, 
                "ADMIN:RESET_BALANCE", 
                null
            );
            
            cashLedgerRepository.save(initialEntry);
            log.info("Cash balance reset for portfolio {}: new balance={}", 
                     portfolioId, targetAmount);
            
            return new CashResetResultDto(
                true,
                String.format("Cash balance successfully reset to %s. %d history entries cleared.", 
                        targetAmount.toPlainString(), entriesDeleted),
                targetAmount.setScale(2, RoundingMode.HALF_UP).toString(),
                defaultCurrency
            );
        } catch (Exception e) {
            log.error("Failed to reset cash balance for portfolio " + portfolioId, e);
            return new CashResetResultDto(
                false,
                "Failed to reset cash balance: " + e.getMessage(),
                null,
                defaultCurrency
            );
        }
    }
}