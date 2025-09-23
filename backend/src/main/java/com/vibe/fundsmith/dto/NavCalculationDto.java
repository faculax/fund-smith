package com.vibe.fundsmith.dto;

import com.vibe.fundsmith.model.NavCalculation;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Data Transfer Object for NAV snapshot API responses.
 *   - id: snapshot identifier (returned to caller after /calculate)
 *   - portfolioId: scope key used across the app (string)
 *   - calculationDate: ISO-8601 timestamp of the snapshot
 *   - grossValue: total assets before fee accrual
 *   - feeAccrual: daily management fee accrued (stored as liability)
 *   - netValue: grossValue - feeAccrual
 *   - sharesOutstanding: number of shares used for NAV per share calculation
 *   - navPerShare: netValue divided by sharesOutstanding (4 decimal places)
 *
 * - Keeps DTO minimal and serializable for API consumers.
 * - Uses static factory method fromEntity(nav) to centralize mapping from entity to DTO.
 * - Fields use names that align with product acceptance language (grossValue, feeAccrual, netValue).
 */
public class NavCalculationDto {
    // Unique snapshot identifier (returned to caller)
    private UUID id;

    // Portfolio/scope key used by the application (kept as String in the domain model)
    private String portfolioId;

    // ISO-8601 formatted calculation timestamp
    private String calculationDate;

    // Gross asset value (positions + cash) before fees
    private BigDecimal grossValue;

    // Fee accrual for the calculation period (stored as liability)
    private BigDecimal feeAccrual;

    // Net asset value after applying fee accrual
    private BigDecimal netValue;

    // Shares outstanding used for NAV per share calculation
    private Long sharesOutstanding;

    // NAV per share (netValue / sharesOutstanding), rounded to 4 decimal places
    private BigDecimal navPerShare;

    /**
     * Default constructor for JSON serialization frameworks.
     */
    public NavCalculationDto() {}

    /**
     * Convert a NavCalculation entity into this DTO.
     *
     * Rationale:
     * - Centralizes entity → DTO mapping so controllers can return a stable external contract.
     * - Normalizes field names to the product acceptance language.
     *
     * @param nav NavCalculation entity (snapshot) to convert
     * @return populated NavCalculationDto
     */
    public static NavCalculationDto fromEntity(NavCalculation nav) {
        NavCalculationDto dto = new NavCalculationDto();

        // Snapshot identifier
        dto.setId(nav.getId());

        // Portfolio/scope key (domain model uses String portfolioId)
        dto.setPortfolioId(nav.getPortfolioId());

        // Calculation timestamp as ISO-8601 string for API consumers
        if (nav.getCalculationDate() != null) {
            dto.setCalculationDate(nav.getCalculationDate().toString());
        } else {
            dto.setCalculationDate(null);
        }

        // Map domain monetary fields to DTO names expected by stories/clients
        dto.setGrossValue(nav.getTotalAssets());        // gross_value / totalAssets in entity
        dto.setFeeAccrual(nav.getTotalLiabilities());  // fee_accrual / totalLiabilities in entity
        dto.setNetValue(nav.getNetAssetValue());       // net_value / netAssetValue in entity

        // Shares outstanding captured at calculation time
        dto.setSharesOutstanding(nav.getSharesOutstanding());

        // NAV per share computed and stored on the entity
        dto.setNavPerShare(nav.getNavPerShare());

        return dto;
    }

    // Getters and setters for JSON serialization / framework usage

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getPortfolioId() {
        return portfolioId;
    }

    public void setPortfolioId(String portfolioId) {
        this.portfolioId = portfolioId;
    }

    public String getCalculationDate() {
        return calculationDate;
    }

    public void setCalculationDate(String calculationDate) {
        this.calculationDate = calculationDate;
    }

    public BigDecimal getGrossValue() {
        return grossValue;
    }

    public void setGrossValue(BigDecimal grossValue) {
        this.grossValue = grossValue;
    }

    public BigDecimal getFeeAccrual() {
        return feeAccrual;
    }

    public void setFeeAccrual(BigDecimal feeAccrual) {
        this.feeAccrual = feeAccrual;
    }

    public BigDecimal getNetValue() {
        return netValue;
    }

    public void setNetValue(BigDecimal netValue) {
        this.netValue = netValue;
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