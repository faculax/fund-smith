package com.vibe.fundsmith.dto;

import com.vibe.fundsmith.model.NavCalculation;
import java.math.BigDecimal;
import java.util.UUID;

/**
 * Data Transfer Object for NAV calculation API responses.
 * Provides a clean, serializable representation of NAV calculations
 * without exposing internal implementation details.
 *
 * Key features:
 * - Handles date formatting consistently
 * - Maintains precision for monetary values
 * - Includes only necessary fields for API consumers
 * - Provides static factory method for entity conversion
 */
public class NavCalculationDto {
    private UUID id;
    private UUID portfolioId;
    private String calculationDate;  // ISO-8601 formatted
    private BigDecimal totalAssets;
    private BigDecimal totalLiabilities;
    private BigDecimal netAssetValue;
    private BigDecimal navPerShare;

    /**
     * Default constructor for JSON serialization
     */
    public NavCalculationDto() {}

    /**
     * Converts a NavCalculation entity to its DTO representation.
     * Handles date formatting and null safety.
     *
     * @param nav The NAV calculation entity to convert
     * @return NavCalculationDto representing the entity
     */
    public static NavCalculationDto fromEntity(NavCalculation nav) {
        NavCalculationDto dto = new NavCalculationDto();
        dto.setId(nav.getId());
        dto.setPortfolioId(nav.getPortfolioId());
        dto.setCalculationDate(nav.getCalculationDate().toString());
        dto.setTotalAssets(nav.getTotalAssets());
        dto.setTotalLiabilities(nav.getTotalLiabilities());
        dto.setNetAssetValue(nav.getNetAssetValue());
        dto.setNavPerShare(nav.getNavPerShare());
        return dto;
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getPortfolioId() {
        return portfolioId;
    }

    public void setPortfolioId(UUID portfolioId) {
        this.portfolioId = portfolioId;
    }

    public String getCalculationDate() {
        return calculationDate;
    }

    public void setCalculationDate(String calculationDate) {
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