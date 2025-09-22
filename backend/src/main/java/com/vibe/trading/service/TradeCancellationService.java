package com.vibe.trading.service;

import com.vibe.trading.model.Trade;
import com.vibe.trading.model.TradeCancellation;
import com.vibe.trading.model.dto.TradeCancellationRequest;
import com.vibe.trading.model.dto.TradeCancellationResponse;
import com.vibe.trading.model.dto.TradeResponse;
import com.vibe.trading.repository.TradeCancellationRepository;
import com.vibe.trading.repository.TradeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TradeCancellationService {

    private final TradeRepository tradeRepository;
    private final TradeCancellationRepository tradeCancellationRepository;

    @Transactional
    public TradeResponse cancelTrade(TradeCancellationRequest request) {
        log.info("Cancelling trade: {}", request.getTradeId());

        // Find the trade
        Optional<Trade> tradeOpt = tradeRepository.findByTradeId(request.getTradeId());
        if (tradeOpt.isEmpty()) {
            throw new RuntimeException("Trade not found: " + request.getTradeId());
        }

        Trade trade = tradeOpt.get();

        // Validate trade can be cancelled
        validateTradeCanBeCancelled(trade);

        // Check if trade is already cancelled
        if (trade.getStatus() == Trade.TradeStatus.CANCELLED) {
            throw new IllegalStateException("Trade " + trade.getTradeId() + " is already cancelled");
        }

        // Store original status for audit trail
        Trade.TradeStatus originalStatus = trade.getStatus();

        // Create cancellation record
        TradeCancellation cancellation = TradeCancellation.builder()
                .tradeId(trade.getTradeId())
                .cancelledBy(request.getCancelledBy())
                .cancellationReason(request.getCancellationReason())
                .originalStatus(originalStatus)
                .build();

        // Save the cancellation record
        tradeCancellationRepository.save(cancellation);

        // Update trade status to CANCELLED
        trade.setStatus(Trade.TradeStatus.CANCELLED);

        // Save the updated trade
        Trade updatedTrade = tradeRepository.save(trade);

        log.info("Trade {} cancelled successfully by {}", trade.getTradeId(), request.getCancelledBy());

        return mapToTradeResponse(updatedTrade);
    }

    @Transactional(readOnly = true)
    public List<TradeCancellationResponse> getCancellationHistory(String tradeId) {
        log.info("Getting cancellation history for trade: {}", tradeId);
        
        List<TradeCancellation> cancellations = tradeCancellationRepository
                .findByTradeIdOrderByCancelledAtDesc(tradeId);
        
        return cancellations.stream()
                .map(this::mapToCancellationResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public boolean canTradeBeCancelled(String tradeId) {
        Optional<Trade> tradeOpt = tradeRepository.findByTradeId(tradeId);
        if (tradeOpt.isEmpty()) {
            return false;
        }
        
        try {
            validateTradeCanBeCancelled(tradeOpt.get());
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private void validateTradeCanBeCancelled(Trade trade) {
        // Trade must be in VERIFIED status only
        if (trade.getStatus() != Trade.TradeStatus.VERIFIED) {
            throw new IllegalStateException(
                    String.format("Trade %s cannot be cancelled. Status is %s, must be VERIFIED", 
                            trade.getTradeId(), trade.getStatus()));
        }

        // Additional validation: Trade must not have settled (value date not passed)
        if (trade.getValueDate().isBefore(LocalDate.now())) {
            throw new IllegalStateException(
                    String.format("Trade %s cannot be cancelled. Trade has already settled (value date: %s)", 
                            trade.getTradeId(), trade.getValueDate()));
        }
    }

    private TradeResponse mapToTradeResponse(Trade trade) {
        return TradeResponse.builder()
                .id(trade.getId())
                .tradeId(trade.getTradeId())
                .tradeDate(trade.getTradeDate())
                .currencyPair(trade.getCurrencyPair())
                .direction(trade.getDirection())
                .notionalAmount(trade.getNotionalAmount())
                .rate(trade.getRate())
                .counterparty(trade.getCounterparty())
                .valueDate(trade.getValueDate())
                .executionTime(trade.getExecutionTime())
                .lei(trade.getLei())
                .uti(trade.getUti())
                .tradeType(trade.getTradeType())
                .status(trade.getStatus())
                .createdBy(trade.getCreatedBy())
                .createdAt(trade.getCreatedAt())
                .updatedAt(trade.getUpdatedAt())
                .emirMifidClassification(trade.getEmirMifidClassification())
                .reportingParty(trade.getReportingParty())
                .forwardValueDate(trade.getForwardValueDate())
                .forwardPoints(trade.getForwardPoints())
                .netForwardRate(trade.getNetForwardRate())
                .pricingSource(trade.getPricingSource())
                .tenor(trade.getTenor())
                .build();
    }

    private TradeCancellationResponse mapToCancellationResponse(TradeCancellation cancellation) {
        return TradeCancellationResponse.builder()
                .id(cancellation.getId())
                .tradeId(cancellation.getTradeId())
                .cancelledBy(cancellation.getCancelledBy())
                .cancelledAt(cancellation.getCancelledAt())
                .cancellationReason(cancellation.getCancellationReason())
                .originalStatus(cancellation.getOriginalStatus())
                .build();
    }
}
