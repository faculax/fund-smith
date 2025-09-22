package com.vibe.trading.model.dto;

import com.vibe.trading.model.Trade;
import jakarta.validation.constraints.*;
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
public class TradeRequest {

    @NotNull(message = "Trade date is required")
    private LocalDate tradeDate;

    @NotBlank(message = "Currency pair is required")
    @Pattern(regexp = "^[A-Z]{3}/[A-Z]{3}$", message = "Currency pair must be in format XXX/YYY")
    private String currencyPair;

    @NotNull(message = "Direction is required")
    private Trade.TradeDirection direction;

    @NotNull(message = "Notional amount is required")
    @DecimalMin(value = "0.0001", message = "Notional amount must be greater than 0")
    private BigDecimal notionalAmount;

    @NotNull(message = "Rate is required")
    @DecimalMin(value = "0.000001", message = "Rate must be greater than 0")
    private BigDecimal rate;

    @NotBlank(message = "Counterparty is required")
    private String counterparty;

    @NotNull(message = "Value date is required")
    private LocalDate valueDate;

    @NotBlank(message = "LEI is required")
    private String lei;

    private String uti;

    @NotNull(message = "Trade type is required")
    private Trade.TradeType tradeType;

    @NotBlank(message = "Created by is required")
    private String createdBy;

    // New regulatory fields
    private Trade.EmirMifidClassification emirMifidClassification;
    private String reportingParty;

    // Forward-specific fields
    private LocalDate forwardValueDate;
    private BigDecimal forwardPoints;
    private BigDecimal netForwardRate;
    private Trade.PricingSource pricingSource;
} 