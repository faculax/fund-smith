package com.vibe.trading.model.dto;

import com.vibe.trading.model.RfqOrder;
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
public class RfqResponse {

    private Long id;
    private String rfqId;
    private LocalDate rfqDate;
    private String currencyPair;
    private RfqOrder.TradeDirection direction;
    private BigDecimal notionalAmount;
    private LocalDate valueDate;
    private LocalDate settlementDate;
    private RfqOrder.RfqType rfqType;
    private RfqOrder.RfqStatus status;
    private String clientName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
} 