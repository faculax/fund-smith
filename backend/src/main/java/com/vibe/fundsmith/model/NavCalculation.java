package com.vibe.fundsmith.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.UUID;

/**
 * Entity representing a Net Asset Value (NAV) calculation snapshot.
 * Captures the total value of a portfolio at a specific point in time,
 * including all assets, liabilities, and the calculated per-share value.
 *
 * This entity supports:
 * - Point-in-time portfolio valuation
 * - Historical NAV tracking
 * - Fee calculation basis (Story 5.2)
 * - Performance measurement basis
 */
@Entity
@Table(name = "nav_calculations")
public class NavCalculation {
    
    /**
     * Unique identifier for the NAV calculation
     * Uses UUID to ensure global uniqueness across potential future system scale-out
     */
    @Id
    @Column(name = "id")
    private UUID id;
    
    /**
     * Reference to the portfolio this NAV belongs to
     * Not a foreign key to allow independent scaling of NAV and portfolio services
     */
    @Column(name = "portfolio_id", nullable = false)
    private String portfolioId;
    
    /**
     * Timestamp when this NAV was calculated
     * Stored with timezone for accurate historical tracking
     */
    @Column(name = "calculation_date", nullable = false)
    private ZonedDateTime calculationDate;
    
    /**
     * Total value of all portfolio assets including:
     * - Market value of all positions
     * - Cash balance
     */
    @Column(name = "total_assets", precision = 19, scale = 4, nullable = false)
    private BigDecimal totalAssets;
    
    /**
     * Total value of all portfolio liabilities
     * Initially zero, will include management fee accrual in Story 5.2
     */
    @Column(name = "total_liabilities", precision = 19, scale = 4, nullable = false)
    private BigDecimal totalLiabilities;
    
    /**
     * Net Asset Value = Total Assets - Total Liabilities
     * Core NAV figure used for portfolio valuation and fee calculations
     */
    @Column(name = "net_asset_value", precision = 19, scale = 4, nullable = false)
    private BigDecimal netAssetValue;
    
    /**
     * NAV Per Share = Net Asset Value / Shares Outstanding
     * Used for performance tracking and share dealing
     */
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
     */
    public NavCalculation(String portfolioId) {
        this.id = UUID.randomUUID();
        this.portfolioId = portfolioId;
        this.calculationDate = ZonedDateTime.now();
    }

    public UUID getId() {
        return id;
    }

    public UUID getPortfolioId() {
        return portfolioId;
    }

    public void setPortfolioId(UUID portfolioId) {
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

    public BigDecimal getNavPerShare() {
        return navPerShare;
    }

    public void setNavPerShare(BigDecimal navPerShare) {
        this.navPerShare = navPerShare;
    }
}