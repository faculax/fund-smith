package com.vibe.trading.model.dto;

import com.vibe.trading.model.Trade;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TradeAmendmentRequest {
    
    private String tradeId;
    private String amendmentReason;
    private String amendedBy;
    
    // Fields that can be amended
    private BigDecimal notionalAmount;
    private BigDecimal rate;
    private LocalDate valueDate;
    private String lei;
    private String uti;
    private Trade.EmirMifidClassification emirMifidClassification;
    private String reportingParty;
}
