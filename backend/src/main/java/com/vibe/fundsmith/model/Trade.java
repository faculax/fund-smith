package com.vibe.fundsmith.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "trades")
public class Trade {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 12)
    private String isin;

    @Column(nullable = false)
    private Long quantity;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "trade_date", nullable = false)
    private LocalDate tradeDate;

    @Column(name = "settle_date", nullable = false)
    private LocalDate settleDate;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Version
    private Long version;

    @Column(name = "is_synthetic", nullable = false)
    private boolean synthetic;

    // Default constructor for JPA
    protected Trade() {}

    public Trade(String isin, Long quantity, BigDecimal price, LocalDate tradeDate, LocalDate settleDate) {
        this.isin = isin;
        this.quantity = quantity;
        this.price = price;
        this.tradeDate = tradeDate;
        this.settleDate = settleDate;
        this.createdAt = LocalDateTime.now();
        this.synthetic = false;
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public String getIsin() {
        return isin;
    }

    public void setIsin(String isin) {
        this.isin = isin;
    }

    public Long getQuantity() {
        return quantity;
    }

    public void setQuantity(Long quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public LocalDate getTradeDate() {
        return tradeDate;
    }

    public void setTradeDate(LocalDate tradeDate) {
        this.tradeDate = tradeDate;
    }

    public LocalDate getSettleDate() {
        return settleDate;
    }

    public void setSettleDate(LocalDate settleDate) {
        this.settleDate = settleDate;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public Long getVersion() {
        return version;
    }

    public boolean isSynthetic() {
        return synthetic;
    }

    public void setSynthetic(boolean synthetic) {
        this.synthetic = synthetic;
    }
}