package com.vibe.fundsmith.event;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

/**
 * Domain event representing a position update
 */
public class PositionUpdatedEvent {
    private final String event = "PositionUpdated";
    private final String isin;
    private final BigDecimal delta;
    private final BigDecimal newQuantity;
    private final ZonedDateTime updatedAt;
    
    public PositionUpdatedEvent(String isin, BigDecimal delta, BigDecimal newQuantity, ZonedDateTime updatedAt) {
        this.isin = isin;
        this.delta = delta;
        this.newQuantity = newQuantity;
        this.updatedAt = updatedAt;
    }

    public String getEvent() {
        return event;
    }
    
    public String getIsin() {
        return isin;
    }
    
    public BigDecimal getDelta() {
        return delta;
    }
    
    public BigDecimal getNewQuantity() {
        return newQuantity;
    }
    
    public String getUpdatedAt() {
        return updatedAt.toString();
    }
}