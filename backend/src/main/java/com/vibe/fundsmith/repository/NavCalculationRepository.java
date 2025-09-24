package com.vibe.fundsmith.repository;

import com.vibe.fundsmith.model.NavCalculation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
     * @param portfolioId Portfolio's id (string scope key used across the app)
     * @return Optional containing the most recent NAV calculation, if any
     */
    Optional<NavCalculation> findTopByPortfolioIdOrderByCalculationDateDesc(String portfolioId);

    /**
     * Page through NAV calculations for a portfolio ordered by calculationDate
     * desc.
     * Using Page/ Pageable allows DB-level limit/ordering (efficient).
     *
     * @param portfolioId Portfolio id
     * @param pageable    Pagination + sort (caller should request desc on
     *                    calculationDate)
     * @return Page of NavCalculation entities
     */
    Page<NavCalculation> findByPortfolioId(String portfolioId, Pageable pageable);

    /**
     * Finds all NAV calculations for a portfolio within a date range
     *
     * @param portfolioId Portfolio's Id
     * @param startDate   Start of date range (inclusive)
     * @param endDate     End of date range (inclusive)
     * @return List of NAV calculations ordered by date
     */
    List<NavCalculation> findByPortfolioIdAndCalculationDateBetweenOrderByCalculationDateAsc(
            String portfolioId,
            ZonedDateTime startDate,
            ZonedDateTime endDate);
}