package com.vibe.fundsmith.service;

import com.vibe.fundsmith.dto.TradeRequest;
import com.vibe.fundsmith.dto.TradeResponse;
import com.vibe.fundsmith.model.CashEntry;
import com.vibe.fundsmith.model.Trade;
import com.vibe.fundsmith.model.TradeSide;
import com.vibe.fundsmith.model.TradeStatus;
import com.vibe.fundsmith.repository.TradeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class TradeServiceTest {

    @Mock
    private TradeRepository tradeRepository;
    
    @Mock
    private PositionService positionService;
    
    @Mock
    private CashService cashService;
    
    @InjectMocks
    private TradeService tradeService;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }
    
    @Test
    void bookTrade_Success() {
        // Given
        UUID tradeId = UUID.randomUUID();
        TradeRequest request = new TradeRequest();
        request.setTradeId(tradeId);
        request.setIsin("US0378331005");  // Apple
        request.setQuantity(100L);
        request.setPrice(new BigDecimal("175.50"));
        request.setSide(TradeSide.BUY);
        request.setTradeCurrency("USD");
        request.setPortfolioId("DEFAULT");
        request.setTradeDate(LocalDate.now());
        
        Trade mockTrade = new Trade(
            tradeId,
            request.getIsin(),
            request.getQuantity(),
            request.getPrice(),
            request.getSide(),
            request.getTradeCurrency(),
            request.getTradeDate(),
            request.getTradeDate().plusDays(2),
            request.getPortfolioId()
        );
        
        when(tradeRepository.findByTradeId(tradeId)).thenReturn(Optional.empty());
        when(tradeRepository.save(any(Trade.class))).thenReturn(mockTrade);
        when(positionService.updatePosition(eq(tradeId), eq(request.getIsin()), any(BigDecimal.class), eq(TradeSide.BUY))).thenReturn(true);
        
        CashEntry mockCashEntry = new CashEntry("DEFAULT", new BigDecimal("100.00"), "BUY:" + tradeId.toString());
        when(cashService.recordTradeImpact(eq(tradeId), eq(TradeSide.BUY), any(BigDecimal.class), any(BigDecimal.class), eq("DEFAULT")))
            .thenReturn(mockCashEntry);
        
        // When
        TradeResponse response = tradeService.bookTrade(request);
        
        // Then
        assertNotNull(response);
        assertEquals(tradeId, response.getTradeId());
        assertEquals(TradeStatus.NEW.toString(), response.getStatus());
        assertFalse(response.isIdempotentHit());
        
        verify(tradeRepository).findByTradeId(tradeId);
        verify(tradeRepository).save(any(Trade.class));
        verify(positionService).updatePosition(eq(tradeId), eq(request.getIsin()), any(BigDecimal.class), eq(TradeSide.BUY));
        verify(cashService).recordTradeImpact(eq(tradeId), eq(TradeSide.BUY), any(BigDecimal.class), any(BigDecimal.class), eq("DEFAULT"));
    }
    
    @Test
    void bookTrade_Idempotent() {
        // Given
        UUID tradeId = UUID.randomUUID();
        TradeRequest request = new TradeRequest();
        request.setTradeId(tradeId);
        request.setIsin("US0378331005");
        request.setQuantity(100L);
        request.setPrice(new BigDecimal("175.50"));
        request.setSide(TradeSide.BUY);
        request.setTradeDate(LocalDate.now());
        
        Trade existingTrade = new Trade(
            tradeId,
            request.getIsin(),
            request.getQuantity(),
            request.getPrice(),
            request.getSide(),
            "USD",
            request.getTradeDate(),
            request.getTradeDate().plusDays(2),
            "DEFAULT"
        );
        existingTrade.setStatus(TradeStatus.NEW);
        
        when(tradeRepository.findByTradeId(tradeId)).thenReturn(Optional.of(existingTrade));
        
        // When
        TradeResponse response = tradeService.bookTrade(request);
        
        // Then
        assertNotNull(response);
        assertEquals(tradeId, response.getTradeId());
        assertEquals(TradeStatus.NEW.toString(), response.getStatus());
        assertTrue(response.isIdempotentHit());
        
        verify(tradeRepository).findByTradeId(tradeId);
        verify(tradeRepository, never()).save(any(Trade.class));
        verify(positionService, never()).updatePosition(any(), any(), any(), any());
        verify(cashService, never()).recordTradeImpact(any(), any(), any(), any(), any());
    }
}