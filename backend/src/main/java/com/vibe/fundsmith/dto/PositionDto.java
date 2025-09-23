package com.vibe.fundsmith.dto;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

/**
 * DTO representing position data for API responses
 */
public class PositionDto {
    private String isin;
    private BigDecimal quantity;
    private String lastUpdated; // ISO-8601 string with timezone
    
    public PositionDto() {}
    
    public PositionDto(String isin, BigDecimal quantity, ZonedDateTime lastUpdated) {
        this.isin = isin;
        this.quantity = quantity;
        this.lastUpdated = lastUpdated.toString();
    }
    
    // Getters and setters
    public String getIsin() {
        return isin;
    }
    
    public void setIsin(String isin) {
        this.isin = isin;
    }
    
    public BigDecimal getQuantity() {
        return quantity;
    }
    
    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }
    
    public String getLastUpdated() {
        return lastUpdated;
    }
    
    public void setLastUpdated(String lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
}