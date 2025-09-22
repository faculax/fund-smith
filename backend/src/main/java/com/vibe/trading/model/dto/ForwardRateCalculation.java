package com.vibe.trading.model.dto;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ForwardRateCalculation {
    private String currencyPair;
    private String tenor;
    private BigDecimal spotRate;
    private BigDecimal forwardRate;
    private BigDecimal forwardPoints;
    private BigDecimal baseCurrencyRate;
    private BigDecimal quoteCurrencyRate;
    private LocalDateTime calculationTime;
    private String baseCurrency;
    private String quoteCurrency;
} 