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
public class TradeAmendmentResponse {
    
    private Long id;
    private String tradeId;
    private Integer amendmentVersion;
    private String amendedBy;
    private LocalDateTime amendedAt;
    private String amendmentReason;
    
    // Original values
    private BigDecimal originalNotionalAmount;
    private BigDecimal originalRate;
    private LocalDate originalValueDate;
    private String originalLei;
    private String originalUti;
    private Trade.EmirMifidClassification originalEmirMifidClassification;
    private String originalReportingParty;
    
    // New values
    private BigDecimal newNotionalAmount;
    private BigDecimal newRate;
    private LocalDate newValueDate;
    private String newLei;
    private String newUti;
    private Trade.EmirMifidClassification newEmirMifidClassification;
    private String newReportingParty;
}
