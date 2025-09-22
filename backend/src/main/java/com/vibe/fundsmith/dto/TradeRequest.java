package com.vibe.fundsmith.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class TradeRequest {
    private String isin;
    private Long quantity;
    private BigDecimal price;
    private LocalDate tradeDate;
    private LocalDate settleDate;

    // Default constructor for JSON deserialization
    public TradeRequest() {}

    // Getters and setters
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
}