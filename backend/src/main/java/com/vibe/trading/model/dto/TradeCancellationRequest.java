package com.vibe.trading.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TradeCancellationRequest {
    
    private String tradeId;
    
    @NotBlank(message = "Cancelled by is required")
    private String cancelledBy;
    
    @NotBlank(message = "Cancellation reason is required")
    private String cancellationReason;
}
