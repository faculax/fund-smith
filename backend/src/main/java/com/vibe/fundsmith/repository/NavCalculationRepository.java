package com.vibe.fundsmith.repository;

import com.vibe.fundsmith.model.NavCalculation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for NAV calculation persistence operations.
 * Provides methods for:
 * - Saving NAV calculations
 * - Retrieving latest NAV
 * - Querying historical NAV data
 */
@Repository
public interface NavCalculationRepository extends JpaRepository<NavCalculation, UUID> {
    
    /**
     * Finds the most recent NAV calculation for a portfolio
     *
     * @param portfolioId Portfolio's UUID
     * @return Optional containing the most recent NAV calculation, if any
     */
    Optional<NavCalculation> findTopByPortfolioIdOrderByCalculationDateDesc(String portfolioId);
    
    /**
     * Finds all NAV calculations for a portfolio within a date range
     *
     * @param portfolioId Portfolio's Id
     * @param startDate Start of date range (inclusive)
     * @param endDate End of date range (inclusive)
     * @return List of NAV calculations ordered by date
     */
    List<NavCalculation> findByPortfolioIdAndCalculationDateBetweenOrderByCalculationDateAsc(
        String portfolioId,
        ZonedDateTime startDate,
        ZonedDateTime endDate
    );
}