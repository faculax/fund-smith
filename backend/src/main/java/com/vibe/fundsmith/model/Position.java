package com.vibe.fundsmith.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.ZonedDateTime;

@Entity
@Table(name = "positions")
public class Position {
    
    @Id
    @Column(nullable = false, length = 12)
    private String isin;
    
    @Column(nullable = false, precision = 28, scale = 6)
    private BigDecimal quantity;
    
    @Column(name = "updated_at", nullable = false)
    private ZonedDateTime updatedAt;
    
    // Default constructor for JPA
    protected Position() {}
    
    public Position(String isin) {
        this.isin = isin;
        this.quantity = BigDecimal.ZERO;
        this.updatedAt = ZonedDateTime.now();
    }
    
    public Position(String isin, BigDecimal quantity) {
        this.isin = isin;
        this.quantity = quantity;
        this.updatedAt = ZonedDateTime.now();
    }
    
    // Getters and setters
    public String getIsin() {
        return isin;
    }
    
    public BigDecimal getQuantity() {
        return quantity;
    }
    
    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }
    
    public ZonedDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(ZonedDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}