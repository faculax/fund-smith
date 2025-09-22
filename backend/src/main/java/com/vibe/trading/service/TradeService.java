package com.vibe.trading.service;

import com.vibe.trading.model.Trade;
import com.vibe.trading.model.dto.TradeRequest;
import com.vibe.trading.model.dto.TradeResponse;
import com.vibe.trading.repository.TradeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TradeService {

    private final TradeRepository tradeRepository;

    @Transactional
    public TradeResponse createTrade(TradeRequest request) {
        log.info("Creating trade: {}", request);

        // Validate value date based on trade type
        validateValueDate(request);

        // Generate unique trade ID
        String tradeId = generateTradeId();

        // Create trade entity
        Trade trade = Trade.builder()
                .tradeId(tradeId)
                .tradeDate(request.getTradeDate())
                .currencyPair(request.getCurrencyPair())
                .direction(request.getDirection())
                .notionalAmount(request.getNotionalAmount())
                .rate(request.getRate())
                .counterparty(request.getCounterparty())
                .valueDate(request.getValueDate())
                .executionTime(LocalDateTime.now())
                .lei(request.getLei())
                .uti(request.getUti())
                .tradeType(request.getTradeType())
                .status(Trade.TradeStatus.PENDING)
                .createdBy(request.getCreatedBy())
                .emirMifidClassification(request.getEmirMifidClassification())
                .reportingParty(request.getReportingParty())
                .forwardValueDate(request.getForwardValueDate())
                .forwardPoints(request.getForwardPoints())
                .netForwardRate(request.getNetForwardRate())
                .pricingSource(request.getPricingSource())
                // User tracking fields for Story 34.4
                .bookedByUserId(extractUserIdFromName(request.getCreatedBy()))
                .bookedByUsername(request.getCreatedBy())
                .build();

        Trade savedTrade = tradeRepository.save(trade);
        log.info("Trade created successfully: {}", savedTrade.getTradeId());

        // Auto-verify trade if it meets all criteria (Epic 30)
        Trade verifiedTrade = attemptAutoVerification(savedTrade);

        return mapToResponse(verifiedTrade);
    }

    @Transactional(readOnly = true)
    public List<TradeResponse> getAllTrades() {
        return tradeRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Optional<TradeResponse> getTradeById(Long id) {
        return tradeRepository.findById(id)
                .map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public Optional<TradeResponse> getTradeByTradeId(String tradeId) {
        return tradeRepository.findByTradeId(tradeId)
                .map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public List<TradeResponse> getTradesByDate(LocalDate tradeDate) {
        return tradeRepository.findByTradeDate(tradeDate).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TradeResponse> getTradesByType(Trade.TradeType tradeType) {
        return tradeRepository.findByTradeType(tradeType).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TradeResponse> getTradesByStatus(Trade.TradeStatus status) {
        return tradeRepository.findByStatus(status).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TradeResponse> getTradesByDateRange(LocalDate startDate, LocalDate endDate) {
        return tradeRepository.findByTradeDateBetweenOrderByCreatedAtDesc(startDate, endDate).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TradeResponse> getTradesByCounterparty(String counterparty) {
        return tradeRepository.findByCounterparty(counterparty).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TradeResponse> getTradesByCurrencyPair(String currencyPair) {
        return tradeRepository.findByCurrencyPair(currencyPair).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TradeResponse> getTradesByUser(String userId) {
        log.info("Fetching trades for user ID: {}", userId);
        List<Trade> trades = tradeRepository.findByBookedByUserId(userId);
        log.info("Found {} trades for user ID: {}", trades.size(), userId);
        return trades.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TradeResponse> getTradesByUsername(String username) {
        log.info("Fetching trades for username: {}", username);
        List<Trade> trades = tradeRepository.findByBookedByUsername(username);
        log.info("Found {} trades for username: {}", trades.size(), username);
        return trades.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getTradeSummary() {
        Map<String, Object> summary = new HashMap<>();
        
        // Count by status
        summary.put("totalTrades", tradeRepository.count());
        summary.put("pendingTrades", tradeRepository.countByStatus(Trade.TradeStatus.PENDING));
        summary.put("verifiedTrades", tradeRepository.countByStatus(Trade.TradeStatus.VERIFIED));
        summary.put("confirmedTrades", tradeRepository.countByStatus(Trade.TradeStatus.CONFIRMED));
        summary.put("cancelledTrades", tradeRepository.countByStatus(Trade.TradeStatus.CANCELLED));
        summary.put("settledTrades", tradeRepository.countByStatus(Trade.TradeStatus.SETTLED));
        
        // Count by type
        summary.put("spotTrades", tradeRepository.countByTradeType(Trade.TradeType.FX_SPOT));
        summary.put("forwardTrades", tradeRepository.countByTradeType(Trade.TradeType.FX_FORWARD));
        
        // Sum notional amounts
        summary.put("totalNotional", tradeRepository.sumNotionalAmountByStatus(Trade.TradeStatus.CONFIRMED));
        
        return summary;
    }

    @Transactional
    public TradeResponse updateTradeStatus(String tradeId, Trade.TradeStatus status) {
        Optional<Trade> tradeOpt = tradeRepository.findByTradeId(tradeId);
        if (tradeOpt.isEmpty()) {
            throw new RuntimeException("Trade not found: " + tradeId);
        }

        Trade trade = tradeOpt.get();
        trade.setStatus(status);
        Trade updatedTrade = tradeRepository.save(trade);
        
        log.info("Trade status updated: {} -> {}", tradeId, status);
        return mapToResponse(updatedTrade);
    }

    /**
     * Attempts to auto-verify a trade based on Epic 30 criteria.
     * Checks if trade meets all validation requirements and updates status to VERIFIED if so.
     */
    @Transactional
    private Trade attemptAutoVerification(Trade trade) {
        if (trade.getStatus() != Trade.TradeStatus.PENDING) {
            log.debug("Trade {} is not in PENDING status, skipping auto-verification", trade.getTradeId());
            return trade;
        }

        StringBuilder validationErrors = new StringBuilder();
        boolean isValid = true;

        // Check if notional is positive
        if (trade.getNotionalAmount() == null || trade.getNotionalAmount().compareTo(java.math.BigDecimal.ZERO) <= 0) {
            validationErrors.append("Notional amount must be positive; ");
            isValid = false;
        }

        // Check if direction is selected
        if (trade.getDirection() == null) {
            validationErrors.append("Direction (Buy/Sell) must be selected; ");
            isValid = false;
        }

        // Check if value date is valid (current or future business date)
        if (trade.getValueDate() == null || trade.getValueDate().isBefore(LocalDate.now())) {
            validationErrors.append("Value date must be current or future date; ");
            isValid = false;
        }

        // Check if counterparty is selected
        if (trade.getCounterparty() == null || trade.getCounterparty().trim().isEmpty()) {
            validationErrors.append("Counterparty must be selected; ");
            isValid = false;
        }

        // Check if rate is provided
        if (trade.getRate() == null || trade.getRate().compareTo(java.math.BigDecimal.ZERO) <= 0) {
            validationErrors.append("Rate must be provided and positive; ");
            isValid = false;
        }

        // Note: Currency pair is NOT required for this check per Epic 30 requirements

        if (isValid) {
            trade.setStatus(Trade.TradeStatus.VERIFIED);
            Trade verifiedTrade = tradeRepository.save(trade);
            log.info("Trade {} auto-verified successfully: PENDING -> VERIFIED", trade.getTradeId());
            return verifiedTrade;
        } else {
            log.warn("Trade {} failed auto-verification and remains PENDING. Reasons: {}", 
                    trade.getTradeId(), validationErrors.toString().trim());
            return trade;
        }
    }

    private void validateValueDate(TradeRequest request) {
        LocalDate spotDate = LocalDate.now().plusDays(2); // T+2 for spot
        
        if (request.getTradeType() == Trade.TradeType.FX_SPOT) {
            if (!request.getValueDate().equals(spotDate)) {
                throw new IllegalArgumentException("FX Spot trades must have value date T+2");
            }
        } else if (request.getTradeType() == Trade.TradeType.FX_FORWARD) {
            if (request.getValueDate().isBefore(spotDate.plusDays(1))) {
                throw new IllegalArgumentException("FX Forward trades must have value date after T+2");
            }
        }
    }

    private String generateTradeId() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String uuid = UUID.randomUUID().toString().substring(0, 8);
        return "TRD" + timestamp + uuid;
    }

    /**
     * Extract user ID from username for backward compatibility
     * Handles cases like "FX Trader 1" -> "1", "FX Trader 2" -> "2", etc.
     */
    private String extractUserIdFromName(String username) {
        if (username == null || username.trim().isEmpty()) {
            return "UNKNOWN";
        }
        
        // Handle "FX Trader X" format
        if (username.matches("FX Trader \\d+")) {
            return username.replaceAll("FX Trader ", "");
        }
        
        // For other formats, try to extract number or return first part
        String[] parts = username.split("\\s+");
        for (String part : parts) {
            if (part.matches("\\d+")) {
                return part;
            }
        }
        
        // If no number found, return first word or fallback
        return parts.length > 0 ? parts[0] : "USER";
    }

    private TradeResponse mapToResponse(Trade trade) {
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
                // User audit tracking fields for Story 34.4
                .bookedByUserId(trade.getBookedByUserId())
                .bookedByUsername(trade.getBookedByUsername())
                .emirMifidClassification(trade.getEmirMifidClassification())
                .reportingParty(trade.getReportingParty())
                .forwardValueDate(trade.getForwardValueDate())
                .forwardPoints(trade.getForwardPoints())
                .netForwardRate(trade.getNetForwardRate())
                .pricingSource(trade.getPricingSource())
                .build();
    }
}
