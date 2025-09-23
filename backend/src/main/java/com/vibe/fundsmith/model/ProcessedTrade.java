package com.vibe.fundsmith.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.UUID;

@Entity
@Table(name = "processed_trades")
public class ProcessedTrade {
    
    @Id
    @Column(name = "trade_id")
    private UUID tradeId;
    
    @Column(nullable = false, length = 12)
    private String isin;
    
    @Column(name = "applied_delta", nullable = false, precision = 28, scale = 6)
    private BigDecimal appliedDelta;
    
    @Column(name = "processed_at", nullable = false)
    private ZonedDateTime processedAt;
    
    // Default constructor for JPA
    protected ProcessedTrade() {}
    
    public ProcessedTrade(UUID tradeId, String isin, BigDecimal appliedDelta) {
        this.tradeId = tradeId;
        this.isin = isin;
        this.appliedDelta = appliedDelta;
        this.processedAt = ZonedDateTime.now();
    }
    
    // Getters
    public UUID getTradeId() {
        return tradeId;
    }
    
    public String getIsin() {
        return isin;
    }
    
    public BigDecimal getAppliedDelta() {
        return appliedDelta;
    }
    
    public ZonedDateTime getProcessedAt() {
        return processedAt;
    }
}