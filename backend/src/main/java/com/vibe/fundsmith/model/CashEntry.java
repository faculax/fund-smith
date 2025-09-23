package com.vibe.fundsmith.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.ZonedDateTime;

@Entity
@Table(name = "cash_ledger")
public class CashEntry {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "portfolio_id", nullable = false)
    private String portfolioId;
    
    @Column(nullable = false, precision = 28, scale = 2)
    private BigDecimal delta;
    
    @Column(precision = 28, scale = 2)
    private BigDecimal balance;
    
    @Column(nullable = false)
    private String currency;
    
    @Column(nullable = false)
    private String reason;
    
    @Column(name = "trade_id")
    private String tradeId;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private ZonedDateTime createdAt;
    
    // Default constructor for JPA
    protected CashEntry() {}
    
    public CashEntry(String portfolioId, BigDecimal delta, BigDecimal balance, String currency, String reason, String tradeId) {
        this.portfolioId = portfolioId;
        this.delta = delta;
        this.balance = balance;
        this.currency = currency;
        this.reason = reason;
        this.tradeId = tradeId;
        this.createdAt = ZonedDateTime.now();
    }
    
    // Simplified constructor for backward compatibility
    public CashEntry(String portfolioId, BigDecimal delta, String reason) {
        this(portfolioId, delta, null, "USD", reason, null);
    }
    
    // Getters
    public Long getId() {
        return id;
    }
    
    public String getPortfolioId() {
        return portfolioId;
    }
    
    public BigDecimal getDelta() {
        return delta;
    }
    
    public BigDecimal getBalance() {
        return balance;
    }
    
    public String getCurrency() {
        return currency;
    }
    
    public String getReason() {
        return reason;
    }
    
    public String getTradeId() {
        return tradeId;
    }
    
    public ZonedDateTime getCreatedAt() {
        return createdAt;
    }
}