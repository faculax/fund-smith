package com.vibe.fundsmith.service;

import com.vibe.fundsmith.dto.JournalDto;
import com.vibe.fundsmith.exception.UnbalancedJournalException;
import com.vibe.fundsmith.model.*;
import com.vibe.fundsmith.repository.JournalRepository;
import com.vibe.fundsmith.repository.ProcessedTradeRepository;
import com.vibe.fundsmith.repository.SettlementMarkerRepository;
import com.vibe.fundsmith.repository.TradeRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for managing accounting journals
 */
@Service
public class JournalService {
    private static final Logger log = LoggerFactory.getLogger(JournalService.class);
    
    private final JournalRepository journalRepository;
    private final SettlementMarkerRepository settlementMarkerRepository;
    private final TradeRepository tradeRepository;
    
    @Autowired
    public JournalService(
            JournalRepository journalRepository,
            SettlementMarkerRepository settlementMarkerRepository,
            TradeRepository tradeRepository) {
        this.journalRepository = journalRepository;
        this.settlementMarkerRepository = settlementMarkerRepository;
        this.tradeRepository = tradeRepository;
    }
    
    /**
     * Create a trade date journal for a new trade
     * 
     * @param tradeId The trade ID
     * @param isin The instrument ISIN
     * @param quantity The trade quantity
     * @param price The trade price
     * @param side The trade side (BUY/SELL)
     * @return The created journal DTO
     * @throws UnbalancedJournalException if the journal is not balanced
     */
    @Transactional
    public JournalDto createTradeDateJournal(UUID tradeId, String isin, BigDecimal quantity, 
                                            BigDecimal price, TradeSide side) {
        
        // Check for idempotency - if journal already exists for this trade, return it
        Optional<Journal> existingJournal = journalRepository.findByTradeIdAndJournalType(
                tradeId, JournalType.TRADE_DATE);
        
        if (existingJournal.isPresent()) {
            log.info("Trade date journal already exists for trade {}", tradeId);
            return JournalDto.fromEntity(existingJournal.get());
        }
        
        // Calculate the trade amount
        BigDecimal amount = quantity.multiply(price);
        
        // Create a new journal
        Journal journal = new Journal(tradeId, JournalType.TRADE_DATE);
        
        if (side == TradeSide.BUY) {
            // For BUY: Debit Securities Receivable, Credit Cash Payable
            journal.addLine("SECURITIES_RECEIVABLE", amount, BigDecimal.ZERO);
            journal.addLine("CASH_PAYABLE", BigDecimal.ZERO, amount);
        } else {
            // For SELL: Debit Cash Receivable, Credit Securities Payable
            journal.addLine("CASH_RECEIVABLE", amount, BigDecimal.ZERO);
            journal.addLine("SECURITIES_PAYABLE", BigDecimal.ZERO, amount);
        }
        
        // Ensure the journal is balanced
        if (!journal.isBalanced()) {
            throw new UnbalancedJournalException("Journal is not balanced");
        }
        
        // Save the journal
        journalRepository.save(journal);
        log.info("Created trade date journal for trade {}", tradeId);
        
        return JournalDto.fromEntity(journal);
    }
    
    /**
     * Create a settlement date journal for a trade
     * 
     * @param tradeId The trade ID
     * @return The created journal DTO
     * @throws UnbalancedJournalException if the journal is not balanced
     */
    @Transactional
    public JournalDto createSettlementDateJournal(UUID tradeId) {
        // Check for idempotency - if settlement marker exists, return the existing journal
        if (settlementMarkerRepository.existsByTradeId(tradeId)) {
            Optional<Journal> existingJournal = journalRepository.findByTradeIdAndJournalType(
                    tradeId, JournalType.SETTLEMENT_DATE);
            
            if (existingJournal.isPresent()) {
                log.info("Settlement date journal already exists for trade {}", tradeId);
                return JournalDto.fromEntity(existingJournal.get());
            }
            
            // If marker exists but journal doesn't, this is an error condition
            log.error("Settlement marker exists but journal missing for trade {}", tradeId);
        }
        
        // Get the trade date journal to determine values
        Journal tradeDateJournal = journalRepository.findByTradeIdAndJournalType(
                tradeId, JournalType.TRADE_DATE)
                .orElseThrow(() -> new IllegalStateException(
                        "Cannot create settlement journal without trade date journal for trade: " + tradeId));
        
        // Determine if this is a BUY or SELL from the trade date journal
        boolean isBuy = tradeDateJournal.getLines().stream()
                .anyMatch(line -> "SECURITIES_RECEIVABLE".equals(line.getAccount()));
        
        // Get the amount from the trade date journal
        BigDecimal amount = isBuy 
                ? tradeDateJournal.getLines().stream()
                    .filter(line -> "SECURITIES_RECEIVABLE".equals(line.getAccount()))
                    .findFirst().get().getDebit()
                : tradeDateJournal.getLines().stream()
                    .filter(line -> "CASH_RECEIVABLE".equals(line.getAccount()))
                    .findFirst().get().getDebit();
        
        // Create a new journal for settlement
        Journal journal = new Journal(tradeId, JournalType.SETTLEMENT_DATE);
        
        if (isBuy) {
            // For BUY settlement:
            // 1. Debit Securities, Credit Securities Receivable
            // 2. Debit Cash Payable, Credit Cash
            journal.addLine("SECURITIES", amount, BigDecimal.ZERO);
            journal.addLine("SECURITIES_RECEIVABLE", BigDecimal.ZERO, amount);
            journal.addLine("CASH_PAYABLE", amount, BigDecimal.ZERO);
            journal.addLine("CASH", BigDecimal.ZERO, amount);
        } else {
            // For SELL settlement:
            // 1. Debit Securities Payable, Credit Securities
            // 2. Debit Cash, Credit Cash Receivable
            journal.addLine("SECURITIES_PAYABLE", amount, BigDecimal.ZERO);
            journal.addLine("SECURITIES", BigDecimal.ZERO, amount);
            journal.addLine("CASH", amount, BigDecimal.ZERO);
            journal.addLine("CASH_RECEIVABLE", BigDecimal.ZERO, amount);
        }
        
        // Ensure the journal is balanced
        if (!journal.isBalanced()) {
            throw new UnbalancedJournalException("Settlement journal is not balanced");
        }
        
        // Save the journal and settlement marker
        journalRepository.save(journal);
        settlementMarkerRepository.save(new SettlementMarker(tradeId));
        
        log.info("Created settlement date journal for trade {}", tradeId);
        
        return JournalDto.fromEntity(journal);
    }
    
    /**
     * Process settlements for trades that have reached their settlement date
     * 
     * @param settleDate The settlement date to process (defaults to today if null)
     * @return Number of trades processed
     */
    @Transactional
    public int processSettlements(LocalDate settleDate) {
        LocalDate dateToProcess = settleDate != null ? settleDate : LocalDate.now();
        
        // Find trades that need settlement
        List<Trade> tradesToSettle = tradeRepository.findBySettleDateAndNotSettled(dateToProcess);
        log.info("Found {} trades to settle for date {}", tradesToSettle.size(), dateToProcess);
        
        int processed = 0;
        for (Trade trade : tradesToSettle) {
            try {
                createSettlementDateJournal(trade.getTradeId());
                processed++;
            } catch (Exception e) {
                log.error("Error processing settlement for trade {}: {}", 
                         trade.getTradeId(), e.getMessage());
            }
        }
        
        return processed;
    }
    
    /**
     * Get all journals for a specific trade
     * 
     * @param tradeId The trade ID
     * @return List of journal DTOs
     */
    public List<JournalDto> getJournalsForTrade(UUID tradeId) {
        List<Journal> journals = journalRepository.findByTradeIdOrderByCreatedAtDesc(tradeId);
        return journals.stream()
                .map(JournalDto::fromEntity)
                .collect(Collectors.toList());
    }
    
    /**
     * Get the most recent journals (for dashboard/operations view)
     * 
     * @return List of recent journal DTOs
     */
    public List<JournalDto> getRecentJournals() {
        List<Journal> journals = journalRepository.findTop10ByOrderByCreatedAtDesc();
        return journals.stream()
                .map(JournalDto::fromEntity)
                .collect(Collectors.toList());
    }
}