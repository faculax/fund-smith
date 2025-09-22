package com.vibe.trading.model.dto;

import com.vibe.trading.model.Trade;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TradeCancellationResponse {
    
    private Long id;
    private String tradeId;
    private String cancelledBy;
    private LocalDateTime cancelledAt;
    private String cancellationReason;
    private Trade.TradeStatus originalStatus;
}
