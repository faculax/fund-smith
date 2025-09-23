package com.vibe.fundsmith.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "trades")
public class Trade {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "trade_id", nullable = false, unique = true)
    private UUID tradeId;

    @Column(nullable = false, length = 12)
    private String isin;

    @Column(nullable = false)
    private Long quantity;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 4)
    private TradeSide side;

    @Column(name = "trade_currency", nullable = false, length = 3)
    private String tradeCurrency;

    @Column(name = "portfolio_id", nullable = false)
    private String portfolioId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private TradeStatus status;

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

    public Trade(UUID tradeId, String isin, Long quantity, BigDecimal price, TradeSide side, String tradeCurrency, 
                 LocalDate tradeDate, LocalDate settleDate, String portfolioId) {
        this.tradeId = tradeId != null ? tradeId : UUID.randomUUID();
        this.isin = isin;
        this.quantity = quantity;
        this.price = price;
        this.side = side;
        this.tradeCurrency = tradeCurrency;
        this.portfolioId = portfolioId != null ? portfolioId : "DEFAULT";
        this.tradeDate = tradeDate;
        this.settleDate = settleDate;
        this.status = TradeStatus.NEW;
        this.createdAt = LocalDateTime.now();
        this.synthetic = false;
    }

    public Trade(String isin, Long quantity, BigDecimal price, TradeSide side,
                 LocalDate tradeDate, LocalDate settleDate) {
        this(UUID.randomUUID(), isin, quantity, price, side, "USD", tradeDate, settleDate, "DEFAULT");
    }

    // Legacy constructor for backward compatibility
    public Trade(String isin, Long quantity, BigDecimal price, LocalDate tradeDate, LocalDate settleDate) {
        this(UUID.randomUUID(), isin, quantity, price, TradeSide.BUY, "USD", tradeDate, settleDate, "DEFAULT");
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public UUID getTradeId() {
        return tradeId;
    }

    public void setTradeId(UUID tradeId) {
        this.tradeId = tradeId;
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

    public TradeSide getSide() {
        return side;
    }

    public void setSide(TradeSide side) {
        this.side = side;
    }

    public String getTradeCurrency() {
        return tradeCurrency;
    }

    public void setTradeCurrency(String tradeCurrency) {
        this.tradeCurrency = tradeCurrency;
    }

    public String getPortfolioId() {
        return portfolioId;
    }

    public void setPortfolioId(String portfolioId) {
        this.portfolioId = portfolioId;
    }

    public TradeStatus getStatus() {
        return status;
    }

    public void setStatus(TradeStatus status) {
        this.status = status;
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