package com.vibe.trading.model.dto;

import com.vibe.trading.model.Trade;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TradeResponse {

    private Long id;
    private String tradeId;
    private LocalDate tradeDate;
    private String currencyPair;
    private Trade.TradeDirection direction;
    private BigDecimal notionalAmount;
    private BigDecimal rate;
    private String counterparty;
    private LocalDate valueDate;
    private LocalDateTime executionTime;
    private String lei;
    private String uti;
    private Trade.TradeType tradeType;
    private Trade.TradeStatus status;
    private String createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // User audit tracking fields for Story 34.4
    private String bookedByUserId;
    private String bookedByUsername;
    
    // New regulatory fields
    private Trade.EmirMifidClassification emirMifidClassification;
    private String reportingParty;
    
    // Forward-specific fields
    private LocalDate forwardValueDate;
    private BigDecimal forwardPoints;
    private BigDecimal netForwardRate;
    private Trade.PricingSource pricingSource;
    private String tenor;
} 