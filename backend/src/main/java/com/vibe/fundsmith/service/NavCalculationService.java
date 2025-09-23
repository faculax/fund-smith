package com.vibe.fundsmith.service;

import com.vibe.fundsmith.model.NavCalculation;
import com.vibe.fundsmith.model.Position;
import com.vibe.fundsmith.exception.NavCalculationException;
import com.vibe.fundsmith.repository.NavCalculationRepository;
import com.vibe.fundsmith.repository.CashLedgerRepository;
import com.vibe.fundsmith.config.DemoConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.ZonedDateTime;
import java.util.List;

/**
 * Service responsible for Net Asset Value (NAV) calculations.
 * Uses configuration-based approach for fund parameters (shares outstanding)
 * rather than Portfolio entity to maintain simpler data model for MVP.
 *
 * Key Features:
 * - On-demand NAV calculation (Story 5.1)
 * - Position valuation using price stub from demo config
 * - Cash balance from cash_ledger
 * - NAV per share using configured shares outstanding
 * - Historical NAV tracking
 */
@Service
public class NavCalculationService {
    private static final Logger log = LoggerFactory.getLogger(NavCalculationService.class);

    private final NavCalculationRepository navCalculationRepository;
    private final PositionService positionService;
    private final CashLedgerRepository cashLedgerRepository;
    private final DemoConfig demoConfig;
    private final Long defaultSharesOutstanding;
    private final BigDecimal feeRate; // Annual fee rate (e.g. 0.005 for 0.5%)

    public NavCalculationService(
            NavCalculationRepository navCalculationRepository,
            PositionService positionService,
            CashLedgerRepository cashLedgerRepository,
            DemoConfig demoConfig,
            @Value("${nav.default.shares-outstanding}") Long defaultSharesOutstanding,
            @Value("${nav.fee-rate:0.005}") BigDecimal feeRate) {
        this.navCalculationRepository = navCalculationRepository;
        this.positionService = positionService;
        this.cashLedgerRepository = cashLedgerRepository;
        this.demoConfig = demoConfig;
        this.defaultSharesOutstanding = defaultSharesOutstanding;
        this.feeRate = feeRate;
    }

    /**
     * Calculates NAV for the fund, including daily management fee accrual.
     * - Gross Asset Value = positions + cash
     * - Daily Fee = (Gross Asset Value * feeRate) / 365
     * - Net Asset Value = Gross Asset Value - Daily Fee
     * - NAV per share = Net Asset Value / shares outstanding
     */
    @Transactional
    public NavCalculation calculateNav(String portfolioId) {
        log.info("Starting NAV calculation for portfolio: {}", portfolioId);

        try {
            BigDecimal positionsValue = calculatePositionsValue();
            BigDecimal cashBalance = cashLedgerRepository.getCurrentBalance(portfolioId);
            BigDecimal grossAssetValue = positionsValue.add(cashBalance);

            // Calculate daily fee accrual
            BigDecimal dailyFeeAccrual = grossAssetValue
                    .multiply(feeRate)
                    .divide(BigDecimal.valueOf(365), 4, RoundingMode.HALF_UP);

            // Net asset value after fee
            BigDecimal netAssetValue = grossAssetValue.subtract(dailyFeeAccrual);

            // NAV per share
            BigDecimal navPerShare = netAssetValue.divide(
                    BigDecimal.valueOf(defaultSharesOutstanding),
                    4, RoundingMode.HALF_UP);

            // Create and save NAV calculation snapshot
            NavCalculation nav = new NavCalculation(portfolioId);
            nav.setTotalAssets(grossAssetValue);
            nav.setTotalLiabilities(dailyFeeAccrual); // Fee accrual as liability
            nav.setNetAssetValue(netAssetValue);
            nav.setNavPerShare(navPerShare);

            log.info("NAV calculation completed. Gross: {}, Fee: {}, Net: {}, NAV/share: {}",
                    grossAssetValue, dailyFeeAccrual, netAssetValue, navPerShare);

            return navCalculationRepository.save(nav);

        } catch (Exception e) {
            log.error("NAV calculation failed: {}", e.getMessage());
            throw new NavCalculationException("Failed to calculate NAV", e);
        }
    }

    /**
     * Calculates total value of all positions using demo prices.
     * Returns zero if no positions exist (handles empty portfolio case).
     *
     * @return Total value of all positions
     */
    private BigDecimal calculatePositionsValue() {
        List<Position> positions = positionService.getPositions();

        if (positions.isEmpty()) {
            log.info("No positions found, portfolio value is 0");
            return BigDecimal.ZERO;
        }

        return positions.stream()
            .map(position -> {
                // Get price from demo config (price stub)
                BigDecimal price = demoConfig.getBasePrices()
                    .getOrDefault(position.getIsin(), BigDecimal.ZERO);
                return price.multiply(position.getQuantity());
            })
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Calculates NAV per share with 4 decimal precision.
     *
     * @param nav Total net asset value
     * @param sharesOutstanding Number of shares
     * @return NAV per share rounded to 4 decimal places
     */
    private BigDecimal calculateNavPerShare(BigDecimal nav, Long sharesOutstanding) {
        return nav.divide(BigDecimal.valueOf(sharesOutstanding), 4, RoundingMode.HALF_UP);
    }

    /**
     * Retrieves the most recent NAV calculation for a portfolio.
     *
     * @param portfolioId Portfolio identifier
     * @return Latest NAV calculation
     * @throws NavCalculationException if no NAV calculations exist
     */
    public NavCalculation getLatestNav(String portfolioId) {
        return navCalculationRepository.findTopByPortfolioIdOrderByCalculationDateDesc(portfolioId)
            .orElseThrow(() -> new NavCalculationException("No NAV calculations found"));
    }

    /**
     * Retrieves historical NAV calculations within a date range.
     *
     * @param portfolioId Portfolio identifier
     * @param startDate Start of date range (inclusive)
     * @param endDate End of date range (inclusive)
     * @return List of NAV calculations ordered by date ascending
     */
    public List<NavCalculation> getNavHistory(String portfolioId,
                                            ZonedDateTime startDate,
                                            ZonedDateTime endDate) {
        return navCalculationRepository
            .findByPortfolioIdAndCalculationDateBetweenOrderByCalculationDateAsc(
                portfolioId, startDate, endDate);
    }
}