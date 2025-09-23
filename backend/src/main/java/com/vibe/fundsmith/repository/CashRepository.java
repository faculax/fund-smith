package com.vibe.fundsmith.repository;

import com.vibe.fundsmith.model.CashEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CashRepository extends JpaRepository<CashEntry, Long> {
    
    /**
     * Find cash entries for a specific portfolio
     * 
     * @param portfolioId The portfolio ID
     * @return List of cash entries ordered by most recent first
     */
    List<CashEntry> findByPortfolioIdOrderByCreatedAtDesc(String portfolioId);
}