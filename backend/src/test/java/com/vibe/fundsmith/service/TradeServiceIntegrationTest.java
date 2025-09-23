package com.vibe.fundsmith.service;

import com.vibe.fundsmith.dto.TradeRequest;
import com.vibe.fundsmith.dto.TradeResponse;
import com.vibe.fundsmith.exception.ValidationException;
import com.vibe.fundsmith.model.Trade;
import com.vibe.fundsmith.repository.TradeRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.DayOfWeek;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class TradeServiceIntegrationTest {
    
    @Autowired
    private TradeService tradeService;
    
    @Autowired
    private TradeRepository tradeRepository;

    @Test
    void shouldBookValidTrade() {
        // Given
        TradeRequest request = new TradeRequest();
        request.setIsin("US0378331005");
        request.setQuantity(1000L);
        request.setPrice(new BigDecimal("175.50"));
        request.setTradeDate(LocalDate.now());

        // When
        TradeResponse response = tradeService.bookTrade(request);
        
        // Then
        assertNotNull(response.getTradeId());
        
        // Fetch the actual trade
        Optional<Trade> tradeOpt = tradeRepository.findByTradeId(response.getTradeId());
        assertTrue(tradeOpt.isPresent());
        
        Trade trade = tradeOpt.get();
        assertEquals(request.getIsin(), trade.getIsin());
        assertEquals(request.getQuantity(), trade.getQuantity());
        assertEquals(request.getPrice(), trade.getPrice());
        assertEquals(request.getTradeDate(), trade.getTradeDate());
        assertNotNull(trade.getSettleDate());
        assertTrue(trade.getSettleDate().isAfter(trade.getTradeDate()));
        assertFalse(isWeekend(trade.getSettleDate()));
    }

    @Test
    void shouldRejectInvalidIsin() {
        // Given
        TradeRequest request = new TradeRequest();
        request.setIsin("INVALID");  // Too short
        request.setQuantity(1000L);
        request.setPrice(new BigDecimal("175.50"));
        request.setTradeDate(LocalDate.now());

        // When/Then
        ValidationException exception = assertThrows(
            ValidationException.class,
            () -> tradeService.bookTrade(request)
        );
        assertEquals("isin", exception.getField());
    }

    @Test
    void shouldCalculateSettleDateSkippingWeekends() {
        // Given - Create trade on the most recent Friday
        LocalDate friday = LocalDate.now();
        while (friday.getDayOfWeek() != DayOfWeek.FRIDAY) {
            friday = friday.minusDays(1);
        }

        TradeRequest request = new TradeRequest();
        request.setIsin("US0378331005");
        request.setQuantity(1000L);
        request.setPrice(new BigDecimal("175.50"));
        request.setTradeDate(friday);

        // When
        TradeResponse response = tradeService.bookTrade(request);
        
        // Then 
        Optional<Trade> tradeOpt = tradeRepository.findByTradeId(response.getTradeId());
        assertTrue(tradeOpt.isPresent());
        
        Trade trade = tradeOpt.get();
        // Then - Settle date should be Tuesday (T+2 business days)
        assertEquals(
            friday.plusDays(4), // Skip Sat/Sun
            trade.getSettleDate()
        );
    }

    @Test
    void shouldListTradesWithFiltering() {
        // Given - Create some test trades
        createTestTrade("US0378331005", LocalDate.now());
        createTestTrade("US5949181045", LocalDate.now());
        createTestTrade("US0378331005", LocalDate.now().minusDays(1));

        // When - Filter by ISIN
        List<Trade> trades = tradeService.findTrades(
            LocalDate.now().minusDays(7),
            LocalDate.now(),
            "US0378331005",
            10
        );

        // Then
        assertEquals(2, trades.size());
        assertTrue(trades.stream().allMatch(t -> t.getIsin().equals("US0378331005")));
    }

    private UUID createTestTrade(String isin, LocalDate tradeDate) {
        TradeRequest request = new TradeRequest();
        request.setIsin(isin);
        request.setQuantity(1000L);
        request.setPrice(new BigDecimal("175.50"));
        request.setTradeDate(tradeDate);
        TradeResponse response = tradeService.bookTrade(request);
        return response.getTradeId();
    }

    private boolean isWeekend(LocalDate date) {
        DayOfWeek day = date.getDayOfWeek();
        return day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY;
    }
}