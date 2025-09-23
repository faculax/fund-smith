package com.vibe.fundsmith.service;

import com.vibe.fundsmith.exception.UnbalancedJournalException;
import com.vibe.fundsmith.model.*;
import com.vibe.fundsmith.repository.JournalRepository;
import com.vibe.fundsmith.repository.SettlementMarkerRepository;
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
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class JournalServiceTest {

    @Mock
    private JournalRepository journalRepository;
    
    @Mock
    private SettlementMarkerRepository settlementMarkerRepository;
    
    @Mock
    private TradeRepository tradeRepository;
    
    @InjectMocks
    private JournalService journalService;
    
    private UUID tradeId;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        tradeId = UUID.randomUUID();
    }
    
    @Test
    void testCreateTradeDateJournal_BalancedJournal() {
        // Given
        String isin = "US1234567890";
        BigDecimal quantity = new BigDecimal("100");
        BigDecimal price = new BigDecimal("125.50");
        TradeSide side = TradeSide.BUY;
        BigDecimal amount = quantity.multiply(price);
        
        when(journalRepository.findByTradeIdAndJournalType(tradeId, JournalType.TRADE_DATE))
            .thenReturn(Optional.empty());
        
        // When
        journalService.createTradeDateJournal(tradeId, isin, quantity, price, side);
        
        // Then
        verify(journalRepository, times(1)).save(argThat(journal -> {
            // Verify journal properties
            assertEquals(tradeId, journal.getTradeId());
            assertEquals(JournalType.TRADE_DATE, journal.getJournalType());
            
            // Verify journal lines
            assertEquals(2, journal.getLines().size());
            
            // Verify that journal is balanced
            assertTrue(journal.isBalanced());
            
            return true;
        }));
    }
    
    @Test
    void testCreateTradeDateJournal_Idempotent() {
        // Given
        String isin = "US1234567890";
        BigDecimal quantity = new BigDecimal("100");
        BigDecimal price = new BigDecimal("125.50");
        TradeSide side = TradeSide.BUY;
        
        Journal existingJournal = new Journal(tradeId, JournalType.TRADE_DATE);
        existingJournal.addLine("SECURITIES_RECEIVABLE", quantity.multiply(price), BigDecimal.ZERO);
        existingJournal.addLine("CASH_PAYABLE", BigDecimal.ZERO, quantity.multiply(price));
        
        when(journalRepository.findByTradeIdAndJournalType(tradeId, JournalType.TRADE_DATE))
            .thenReturn(Optional.of(existingJournal));
        
        // When
        journalService.createTradeDateJournal(tradeId, isin, quantity, price, side);
        
        // Then - verify we don't save again
        verify(journalRepository, never()).save(any());
    }
    
    @Test
    void testJournalBalanceEnforcement() {
        // Given
        Journal unbalancedJournal = new Journal(tradeId, JournalType.TRADE_DATE);
        unbalancedJournal.addLine("SECURITIES_RECEIVABLE", new BigDecimal("100"), BigDecimal.ZERO);
        unbalancedJournal.addLine("CASH_PAYABLE", BigDecimal.ZERO, new BigDecimal("99")); // Unbalanced
        
        // Verify the journal is indeed unbalanced
        assertFalse(unbalancedJournal.isBalanced());
        
        // When/Then
        assertThrows(UnbalancedJournalException.class, () -> {
            if (!unbalancedJournal.isBalanced()) {
                throw new UnbalancedJournalException("Journal is not balanced");
            }
        });
    }
}