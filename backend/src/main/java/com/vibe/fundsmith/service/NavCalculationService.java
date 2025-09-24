package com.vibe.fundsmith.service;

import com.vibe.fundsmith.config.DemoConfig;
import com.vibe.fundsmith.exception.NavCalculationException;
import com.vibe.fundsmith.model.NavCalculation;
import com.vibe.fundsmith.model.Position;
import com.vibe.fundsmith.repository.CashLedgerRepository;
import com.vibe.fundsmith.repository.NavCalculationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * Service responsible for NAV calculation and persistence.
 *
 * Implementation notes / rationale:
 * - Reuses demo prices defined in application.yml via DemoConfig (no new price stub).
 * - Uses cash_ledger aggregate for cash balance (CashLedgerRepository#getCurrentBalance).
 * - Shares outstanding and fee rate are externalized in application.yml for configurability.
 * - Fee accrual is calculated as a daily accrual: (gross * feeRate) / 365 and stored as a liability.
 * - The method is transactional to ensure snapshot persistence is atomic with the read operations.
 */
@Service
public class NavCalculationService {
    private static final Logger log = LoggerFactory.getLogger(NavCalculationService.class);

    private final NavCalculationRepository navCalculationRepository;
    private final PositionService positionService;
    private final CashLedgerRepository cashLedgerRepository;
    private final DemoConfig demoConfig;
    private final Long defaultSharesOutstanding;
    private final BigDecimal feeRate; // annual fee rate, e.g. 0.005 for 0.5%

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
     * Calculate positions value using demo prices.
     * Returns BigDecimal.ZERO when no positions exist (explicit empty portfolio handling).
     */
    private BigDecimal calculatePositionsValue() {
        List<Position> positions = positionService.getPositions(); // PositionService must expose getPositions()
        if (positions == null || positions.isEmpty()) {
            log.debug("No positions present; positions value = 0");
            return BigDecimal.ZERO;
        }

        return positions.stream()
                .map(pos -> {
                    BigDecimal price = demoConfig.getBasePrices()
                            .getOrDefault(pos.getIsin(), BigDecimal.ZERO);
                    return price.multiply(pos.getQuantity());
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Calculate NAV for given portfolio scope key and persist snapshot.
     *
     * transactional ensures snapshot is only saved if the calculation completes successfully.
     */
    @Transactional
    public NavCalculation calculateNav(String portfolioId) {
        log.info("Starting NAV calculation for portfolio {}", portfolioId);
        try {
            BigDecimal positionsValue = calculatePositionsValue();
            log.debug("Positions value computed: {}", positionsValue);

            BigDecimal cashBalance = cashLedgerRepository.getCurrentBalance(portfolioId);
            log.debug("Cash balance fetched: {}", cashBalance);

            BigDecimal grossAssetValue = positionsValue.add(cashBalance);
            // daily fee accrual
            BigDecimal dailyFeeAccrual = grossAssetValue
                    .multiply(feeRate)
                    .divide(BigDecimal.valueOf(365), 4, RoundingMode.HALF_UP);

            BigDecimal netAssetValue = grossAssetValue.subtract(dailyFeeAccrual);

            BigDecimal navPerShare = netAssetValue.divide(
                    BigDecimal.valueOf(defaultSharesOutstanding),
                    4,
                    RoundingMode.HALF_UP
            );

            // Create and save NAV calculation snapshot
            NavCalculation nav = new NavCalculation(portfolioId, defaultSharesOutstanding);
            nav.setTotalAssets(grossAssetValue);
            nav.setTotalLiabilities(dailyFeeAccrual); // fee accrual as liability
            nav.setNetAssetValue(netAssetValue);
            nav.setNavPerShare(navPerShare);

            // Ensure shares outstanding recorded at calculation time (acceptance criteria)
            nav.setSharesOutstanding(defaultSharesOutstanding);

            NavCalculation saved = navCalculationRepository.save(nav);

            log.info("NAV snapshot saved (id={}): gross={}, fee={}, net={}, nav/share={}",
                    saved.getId(), grossAssetValue, dailyFeeAccrual, netAssetValue, navPerShare);

            return saved;
        } catch (Exception e) {
            log.error("Failed to calculate NAV for portfolio {}: {}", portfolioId, e.getMessage());
            throw new NavCalculationException("Failed to calculate NAV", e);
        }
    }

    public NavCalculation getLatestNav(String portfolioId) {
        return navCalculationRepository.findTopByPortfolioIdOrderByCalculationDateDesc(portfolioId)
                .orElseThrow(() -> new NavCalculationException("No NAV snapshots found for portfolio: " + portfolioId));
    }

    public List<NavCalculation> getNavHistory(String portfolioId, java.time.ZonedDateTime start, java.time.ZonedDateTime end) {
        return navCalculationRepository.findByPortfolioIdAndCalculationDateBetweenOrderByCalculationDateAsc(portfolioId, start, end);
    }
}