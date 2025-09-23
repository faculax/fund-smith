package com.vibe.fundsmith.event;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.UUID;

/**
 * Domain event representing a cash movement
 */
public class CashMovementEvent {
    private final String event = "CashMovement";
    private final UUID tradeId;
    private final BigDecimal delta;
    private final String reason;
    private final ZonedDateTime createdAt;
    
    public CashMovementEvent(UUID tradeId, BigDecimal delta, String reason, ZonedDateTime createdAt) {
        this.tradeId = tradeId;
        this.delta = delta;
        this.reason = reason;
        this.createdAt = createdAt;
    }

    public String getEvent() {
        return event;
    }
    
    public UUID getTradeId() {
        return tradeId;
    }
    
    public BigDecimal getDelta() {
        return delta;
    }
    
    public String getReason() {
        return reason;
    }
    
    public String getCreatedAt() {
        return createdAt.toString();
    }
}