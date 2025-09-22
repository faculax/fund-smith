package com.vibe.trading.model.dto;

import com.vibe.trading.model.RfqOrder;
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
public class RfqRequest {

    @NotBlank(message = "Currency pair is required")
    @Pattern(regexp = "^[A-Z]{3}/[A-Z]{3}$", message = "Currency pair must be in format XXX/YYY")
    private String currencyPair;

    @NotNull(message = "Direction is required")
    private RfqOrder.TradeDirection direction;

    @NotNull(message = "Notional amount is required")
    @DecimalMin(value = "0.0001", message = "Notional amount must be greater than 0")
    private BigDecimal notionalAmount;

    @NotNull(message = "RFQ type is required")
    private RfqOrder.RfqType rfqType;

    @NotBlank(message = "Client name is required")
    private String clientName;

    // Optional value date for forward RFQs
    private LocalDate valueDate;
} 