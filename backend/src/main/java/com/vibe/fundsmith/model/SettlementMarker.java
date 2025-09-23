package com.vibe.fundsmith.model;

import jakarta.persistence.*;
import java.time.ZonedDateTime;
import java.util.UUID;

/**
 * Tracks which trades have had settlement journals created.
 * Used for idempotency in settlement processing.
 */
@Entity
@Table(name = "settlement_markers")
public class SettlementMarker {
    
    @Id
    @Column(name = "trade_id")
    private UUID tradeId;
    
    @Column(name = "settled_at", nullable = false)
    private ZonedDateTime settledAt;
    
    // Default constructor for JPA
    protected SettlementMarker() {}
    
    public SettlementMarker(UUID tradeId) {
        this.tradeId = tradeId;
        this.settledAt = ZonedDateTime.now();
    }
    
    // Getters and setters
    public UUID getTradeId() {
        return tradeId;
    }
    
    public void setTradeId(UUID tradeId) {
        this.tradeId = tradeId;
    }
    
    public ZonedDateTime getSettledAt() {
        return settledAt;
    }
    
    public void setSettledAt(ZonedDateTime settledAt) {
        this.settledAt = settledAt;
    }
}