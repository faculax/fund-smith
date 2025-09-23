package com.vibe.fundsmith.repository;

import com.vibe.fundsmith.model.ProcessedTrade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ProcessedTradeRepository extends JpaRepository<ProcessedTrade, UUID> {
    
    /**
     * Check if a trade has already been processed
     * @param tradeId The trade ID to check
     * @return True if the trade has been processed
     */
    boolean existsByTradeId(UUID tradeId);
}