package com.vibe.trading.repository;

import com.vibe.trading.model.TradeAmendment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TradeAmendmentRepository extends JpaRepository<TradeAmendment, Long> {
    
    List<TradeAmendment> findByTradeIdOrderByAmendmentVersionDesc(String tradeId);
    
    @Query("SELECT MAX(ta.amendmentVersion) FROM TradeAmendment ta WHERE ta.tradeId = :tradeId")
    Integer findMaxAmendmentVersionByTradeId(@Param("tradeId") String tradeId);
    
    @Query("SELECT COUNT(ta) FROM TradeAmendment ta WHERE ta.tradeId = :tradeId")
    Long countAmendmentsByTradeId(@Param("tradeId") String tradeId);
}
