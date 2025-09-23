package com.vibe.fundsmith.repository;

import com.vibe.fundsmith.model.CashEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface CashLedgerRepository extends JpaRepository<CashEntry, Long> {
    
    /**
     * Get the current cash balance for a portfolio
     * @param portfolioId The portfolio ID
     * @return The current cash balance
     */
    @Query("SELECT COALESCE(SUM(c.delta), 0) FROM CashEntry c WHERE c.portfolioId = :portfolioId")
    BigDecimal getCurrentBalance(@Param("portfolioId") String portfolioId);
    
    /**
     * Find all cash entries for a portfolio, sorted by creation date (newest first)
     * @param portfolioId The portfolio ID
     * @return List of cash entries
     */
    List<CashEntry> findByPortfolioIdOrderByCreatedAtDesc(String portfolioId);
}