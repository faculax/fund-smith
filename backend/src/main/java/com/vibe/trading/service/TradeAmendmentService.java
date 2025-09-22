package com.vibe.trading.service;

import com.vibe.trading.model.Trade;
import com.vibe.trading.model.TradeAmendment;
import com.vibe.trading.model.dto.TradeAmendmentRequest;
import com.vibe.trading.model.dto.TradeAmendmentResponse;
import com.vibe.trading.model.dto.TradeResponse;
import com.vibe.trading.repository.TradeAmendmentRepository;
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
public class TradeAmendmentService {

    private final TradeRepository tradeRepository;
    private final TradeAmendmentRepository tradeAmendmentRepository;

    @Transactional
    public TradeResponse amendTrade(TradeAmendmentRequest request) {
        log.info("Amending trade: {}", request.getTradeId());

        // Find the trade
        Optional<Trade> tradeOpt = tradeRepository.findByTradeId(request.getTradeId());
        if (tradeOpt.isEmpty()) {
            throw new RuntimeException("Trade not found: " + request.getTradeId());
        }

        Trade trade = tradeOpt.get();

        // Validate trade can be amended
        validateTradeCanBeAmended(trade);

        // Store original values for audit trail
        TradeAmendment amendment = createAmendmentRecord(trade, request);

        // Apply amendments to trade
        applyAmendments(trade, request);

        // Save the amendment record
        tradeAmendmentRepository.save(amendment);

        // Save the updated trade
        Trade updatedTrade = tradeRepository.save(trade);

        log.info("Trade {} amended successfully. Amendment version: {}", 
                trade.getTradeId(), amendment.getAmendmentVersion());

        return mapToTradeResponse(updatedTrade);
    }

    @Transactional(readOnly = true)
    public List<TradeAmendmentResponse> getAmendmentHistory(String tradeId) {
        log.info("Getting amendment history for trade: {}", tradeId);
        
        List<TradeAmendment> amendments = tradeAmendmentRepository
                .findByTradeIdOrderByAmendmentVersionDesc(tradeId);
        
        return amendments.stream()
                .map(this::mapToAmendmentResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public boolean canTradeBeAmended(String tradeId) {
        Optional<Trade> tradeOpt = tradeRepository.findByTradeId(tradeId);
        if (tradeOpt.isEmpty()) {
            return false;
        }
        
        try {
            validateTradeCanBeAmended(tradeOpt.get());
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private void validateTradeCanBeAmended(Trade trade) {
        // Trade must be in VERIFIED status
        if (trade.getStatus() != Trade.TradeStatus.VERIFIED) {
            throw new IllegalStateException(
                    String.format("Trade %s cannot be amended. Status is %s, must be VERIFIED", 
                            trade.getTradeId(), trade.getStatus()));
        }

        // Trade must not be settled (value date not passed)
        if (trade.getValueDate().isBefore(LocalDate.now())) {
            throw new IllegalStateException(
                    String.format("Trade %s cannot be amended. Trade has already settled (value date: %s)", 
                            trade.getTradeId(), trade.getValueDate()));
        }
    }

    private TradeAmendment createAmendmentRecord(Trade trade, TradeAmendmentRequest request) {
        // Get next amendment version
        Integer maxVersion = tradeAmendmentRepository.findMaxAmendmentVersionByTradeId(trade.getTradeId());
        int nextVersion = (maxVersion == null) ? 1 : maxVersion + 1;

        return TradeAmendment.builder()
                .tradeId(trade.getTradeId())
                .amendmentVersion(nextVersion)
                .amendedBy(request.getAmendedBy())
                .amendmentReason(request.getAmendmentReason())
                // Store original values
                .originalNotionalAmount(trade.getNotionalAmount())
                .originalRate(trade.getRate())
                .originalValueDate(trade.getValueDate())
                .originalLei(trade.getLei())
                .originalUti(trade.getUti())
                .originalEmirMifidClassification(trade.getEmirMifidClassification())
                .originalReportingParty(trade.getReportingParty())
                // Store new values
                .newNotionalAmount(request.getNotionalAmount())
                .newRate(request.getRate())
                .newValueDate(request.getValueDate())
                .newLei(request.getLei())
                .newUti(request.getUti())
                .newEmirMifidClassification(request.getEmirMifidClassification())
                .newReportingParty(request.getReportingParty())
                .build();
    }

    private void applyAmendments(Trade trade, TradeAmendmentRequest request) {
        // Apply amendments only if new values are provided
        if (request.getNotionalAmount() != null) {
            trade.setNotionalAmount(request.getNotionalAmount());
        }
        if (request.getRate() != null) {
            trade.setRate(request.getRate());
        }
        if (request.getValueDate() != null) {
            validateValueDate(trade, request.getValueDate());
            trade.setValueDate(request.getValueDate());
        }
        if (request.getLei() != null) {
            trade.setLei(request.getLei());
        }
        if (request.getUti() != null) {
            trade.setUti(request.getUti());
        }
        if (request.getEmirMifidClassification() != null) {
            trade.setEmirMifidClassification(request.getEmirMifidClassification());
        }
        if (request.getReportingParty() != null) {
            trade.setReportingParty(request.getReportingParty());
        }
    }

    private void validateValueDate(Trade trade, LocalDate newValueDate) {
        LocalDate spotDate = LocalDate.now().plusDays(2); // T+2 for spot
        
        if (trade.getTradeType() == Trade.TradeType.FX_SPOT) {
            if (!newValueDate.equals(spotDate)) {
                throw new IllegalArgumentException("FX Spot trades must have value date T+2");
            }
        } else if (trade.getTradeType() == Trade.TradeType.FX_FORWARD) {
            if (newValueDate.isBefore(spotDate.plusDays(1))) {
                throw new IllegalArgumentException("FX Forward trades must have value date after T+2");
            }
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
                .build();
    }

    private TradeAmendmentResponse mapToAmendmentResponse(TradeAmendment amendment) {
        return TradeAmendmentResponse.builder()
                .id(amendment.getId())
                .tradeId(amendment.getTradeId())
                .amendmentVersion(amendment.getAmendmentVersion())
                .amendedBy(amendment.getAmendedBy())
                .amendedAt(amendment.getAmendedAt())
                .amendmentReason(amendment.getAmendmentReason())
                .originalNotionalAmount(amendment.getOriginalNotionalAmount())
                .originalRate(amendment.getOriginalRate())
                .originalValueDate(amendment.getOriginalValueDate())
                .originalLei(amendment.getOriginalLei())
                .originalUti(amendment.getOriginalUti())
                .originalEmirMifidClassification(amendment.getOriginalEmirMifidClassification())
                .originalReportingParty(amendment.getOriginalReportingParty())
                .newNotionalAmount(amendment.getNewNotionalAmount())
                .newRate(amendment.getNewRate())
                .newValueDate(amendment.getNewValueDate())
                .newLei(amendment.getNewLei())
                .newUti(amendment.getNewUti())
                .newEmirMifidClassification(amendment.getNewEmirMifidClassification())
                .newReportingParty(amendment.getNewReportingParty())
                .build();
    }
}
