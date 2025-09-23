package com.vibe.fundsmith.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.UUID;

/**
 * Entity representing a NAV snapshot.
 * Stores the results of each NAV calculation for audit and history.
 */
@Entity
@Table(name = "nav_snapshots")
public class NavCalculation {

    @Id
    @Column(name = "id")
    private UUID id;

    @Column(name = "portfolio_id", nullable = false)
    private String portfolioId;

    @Column(name = "calculation_date", nullable = false)
    private ZonedDateTime calculationDate;

    @Column(name = "gross_value", precision = 19, scale = 4, nullable = false)
    private BigDecimal totalAssets;

    @Column(name = "fee_accrual", precision = 19, scale = 4, nullable = false)
    private BigDecimal totalLiabilities;

    @Column(name = "net_value", precision = 19, scale = 4, nullable = false)
    private BigDecimal netAssetValue;

    @Column(name = "shares_outstanding", nullable = false)
    private Long sharesOutstanding;

    @Column(name = "nav_per_share", precision = 19, scale = 4, nullable = false)
    private BigDecimal navPerShare;

    /**
     * Protected default constructor required by JPA
     */
    protected NavCalculation() {}

    /**
     * Creates a new NAV calculation for a portfolio
     *
     * @param portfolioId UUID of the portfolio to calculate NAV for
     * @param sharesOutstanding Number of shares outstanding for the portfolio
     */
    public NavCalculation(String portfolioId, Long sharesOutstanding) {
        this.id = UUID.randomUUID();
        this.portfolioId = portfolioId;
        this.calculationDate = ZonedDateTime.now();
        this.sharesOutstanding = sharesOutstanding;
    }

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