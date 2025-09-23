package com.vibe.fundsmith.repository;

import com.vibe.fundsmith.model.SettlementMarker;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface SettlementMarkerRepository extends JpaRepository<SettlementMarker, UUID> {
    
    /**
     * Check if a settlement marker exists for a trade
     * 
     * @param tradeId The trade ID
     * @return true if a marker exists
     */
    boolean existsByTradeId(UUID tradeId);
}