package com.vibe.fundsmith.service;

import com.vibe.fundsmith.dto.TradeRequest;
import com.vibe.fundsmith.dto.TradeResponse;
import com.vibe.fundsmith.model.CashEntry;
import com.vibe.fundsmith.model.Position;
import com.vibe.fundsmith.model.TradeSide;
import com.vibe.fundsmith.repository.CashRepository;
import com.vibe.fundsmith.repository.PositionRepository;
import com.vibe.fundsmith.repository.ProcessedTradeRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class IborIntegrationTest {

    @Autowired
    private TradeService tradeService;
    
    @Autowired
    private PositionService positionService;
    
    @Autowired
    private CashService cashService;
    
    @Autowired
    private PositionRepository positionRepository;
    
    @Autowired
    private CashRepository cashRepository;
    
    @Autowired
    private ProcessedTradeRepository processedTradeRepository;
    
    @Test
    void fullTradeFlow_UpdatesPositionsAndCash() {
        // Given - Create a trade request
        String isin = "US0378331005";  // Apple
        long quantity = 100L;
        BigDecimal price = new BigDecimal("175.50");
        String portfolioId = "DEFAULT";
        
        TradeRequest request = new TradeRequest();
        request.setIsin(isin);
        request.setQuantity(quantity);
        request.setPrice(price);
        request.setSide(TradeSide.BUY);
        request.setTradeDate(LocalDate.now());
        request.setPortfolioId(portfolioId);
        
        // When - Book the trade
        TradeResponse response = tradeService.bookTrade(request);
        
        // Then - Verify the response
        assertNotNull(response);
        assertNotNull(response.getTradeId());
        assertFalse(response.isIdempotentHit());
        
        // Verify position was updated
        Optional<Position> positionOpt = positionRepository.findById(isin);
        assertTrue(positionOpt.isPresent(), "Position should be created for ISIN");
        Position position = positionOpt.get();
        assertEquals(0, new BigDecimal(quantity).compareTo(position.getQuantity()), 
            "Position quantity should match trade quantity (ignoring scale)");
        
        // Verify cash impact
        List<CashEntry> cashEntries = cashRepository.findByPortfolioIdOrderByCreatedAtDesc(portfolioId);
        assertFalse(cashEntries.isEmpty(), "Cash entries should be created");
        
        CashEntry latestEntry = cashEntries.get(0);
        BigDecimal expectedCashImpact = price.multiply(new BigDecimal(quantity)).negate();
        assertEquals(expectedCashImpact, latestEntry.getDelta(), "Cash impact should be price * quantity (negative for BUY)");
        String expectedReason = request.getSide().toString() + ":" + response.getTradeId().toString();
        assertEquals(expectedReason, latestEntry.getReason(), "Cash reason should be in format SIDE:TRADE_ID");
        
        // Verify the cash entry references the trade ID (if trade ID is set)
        if (latestEntry.getTradeId() != null) {
            assertEquals(response.getTradeId().toString(), latestEntry.getTradeId(), "Cash entry should reference trade ID");
        }
        
        // Verify processed trade record (idempotency)
        assertTrue(processedTradeRepository.existsByTradeId(response.getTradeId()), 
                  "Processed trade record should exist for idempotency");
        
        // Book the same trade again (with same trade ID) to test idempotency
        TradeRequest duplicateRequest = new TradeRequest();
        duplicateRequest.setTradeId(response.getTradeId());
        duplicateRequest.setIsin(isin);
        duplicateRequest.setQuantity(quantity);
        duplicateRequest.setPrice(price);
        duplicateRequest.setSide(TradeSide.BUY);
        duplicateRequest.setTradeDate(LocalDate.now());
        
        TradeResponse duplicateResponse = tradeService.bookTrade(duplicateRequest);
        assertTrue(duplicateResponse.isIdempotentHit(), "Duplicate trade should be detected as idempotent");
        
        // Verify position was not updated twice
        Optional<Position> positionOptAfterDuplicate = positionRepository.findById(isin);
        assertEquals(0, new BigDecimal(quantity).compareTo(positionOptAfterDuplicate.get().getQuantity()), 
                    "Position quantity should not change after duplicate trade (ignoring scale)");
        
        // Verify no additional cash entry
        List<CashEntry> cashEntriesAfterDuplicate = cashRepository.findByPortfolioIdOrderByCreatedAtDesc(portfolioId);
        assertEquals(cashEntries.size(), cashEntriesAfterDuplicate.size(), 
                    "No additional cash entries should be created for duplicate trade");
    }
}