package com.vibe.fundsmith.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.UUID;

/**
 * Entity representing a NAV snapshot.
 * - The snapshot captures gross value, fee accrual (liability), net value,
 *   shares outstanding at time of calculation and NAV per share.
 */
@Entity
@Table(name = "nav_snapshots")
public class NavCalculation {

    @Id
    @Column(name = "id")
    private UUID id;

    // Scope key used across the app (kept as String to match cash_ledger usage)
    @Column(name = "portfolio_id", nullable = false)
    private String portfolioId;

    @Column(name = "calculation_date", nullable = false)
    private ZonedDateTime calculationDate;

    // gross asset value (positions + cash) before fee accrual
    @Column(name = "gross_value", precision = 19, scale = 4, nullable = false)
    private BigDecimal totalAssets;

    // fee accrual recorded as liability for the snapshot
    @Column(name = "fee_accrual", precision = 19, scale = 4, nullable = false)
    private BigDecimal totalLiabilities;

    // net asset value after fee accrual
    @Column(name = "net_value", precision = 19, scale = 4, nullable = false)
    private BigDecimal netAssetValue;

    // shares outstanding captured at calculation time
    @Column(name = "shares_outstanding", nullable = false)
    private Long sharesOutstanding;

    // NAV per share (netAssetValue / sharesOutstanding)
    @Column(name = "nav_per_share", precision = 19, scale = 4, nullable = false)
    private BigDecimal navPerShare;

    // JPA
    protected NavCalculation() {}

    /**
     * Create a new snapshot for a given portfolio scope key and shares outstanding.
     * calculationDate is set to now.
     */
    public NavCalculation(String portfolioId, Long sharesOutstanding) {
        this.id = UUID.randomUUID();
        this.portfolioId = portfolioId;
        this.calculationDate = ZonedDateTime.now();
        this.sharesOutstanding = sharesOutstanding;
    }

    // Getters and setters

    public UUID getId() {
        return id;
    }

    public String getPortfolioId() {
        return portfolioId;
    }

    public void setPortfolioId(String portfolioId) {
        this.portfolioId = portfolioId;
    }

    public ZonedDateTime getCalculationDate() {
        return calculationDate;
    }

    public void setCalculationDate(ZonedDateTime calculationDate) {
        this.calculationDate = calculationDate;
    }

    public BigDecimal getTotalAssets() {
        return totalAssets;
    }

    public void setTotalAssets(BigDecimal totalAssets) {
        this.totalAssets = totalAssets;
    }

    public BigDecimal getTotalLiabilities() {
        return totalLiabilities;
    }

    public void setTotalLiabilities(BigDecimal totalLiabilities) {
        this.totalLiabilities = totalLiabilities;
    }

    public BigDecimal getNetAssetValue() {
        return netAssetValue;
    }

    public void setNetAssetValue(BigDecimal netAssetValue) {
        this.netAssetValue = netAssetValue;
    }

    public Long getSharesOutstanding() {
        return sharesOutstanding;
    }

    public void setSharesOutstanding(Long sharesOutstanding) {
        this.sharesOutstanding = sharesOutstanding;
    }

    public BigDecimal getNavPerShare() {
        return navPerShare;
    }

    public void setNavPerShare(BigDecimal navPerShare) {
        this.navPerShare = navPerShare;
    }
}