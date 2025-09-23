package com.vibe.fundsmith.dto;

import java.util.UUID;

/**
 * Response for trade booking operations with idempotency detection
 */
public class TradeResponse {
    private UUID tradeId;
    private String status;
    private boolean idempotentHit;
    
    public TradeResponse() {}
    
    public TradeResponse(UUID tradeId, String status, boolean idempotentHit) {
        this.tradeId = tradeId;
        this.status = status;
        this.idempotentHit = idempotentHit;
    }
    
    // Getters and setters
    public UUID getTradeId() {
        return tradeId;
    }
    
    public void setTradeId(UUID tradeId) {
        this.tradeId = tradeId;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public boolean isIdempotentHit() {
        return idempotentHit;
    }
    
    public void setIdempotentHit(boolean idempotentHit) {
        this.idempotentHit = idempotentHit;
    }
}