package com.vibe.trading.repository;

import com.vibe.trading.model.TradeCancellation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TradeCancellationRepository extends JpaRepository<TradeCancellation, Long> {
    
    Optional<TradeCancellation> findByTradeId(String tradeId);
    
    List<TradeCancellation> findByTradeIdOrderByCancelledAtDesc(String tradeId);
    
    @Query("SELECT tc FROM TradeCancellation tc WHERE tc.cancelledBy = :cancelledBy ORDER BY tc.cancelledAt DESC")
    List<TradeCancellation> findByCancelledByOrderByCancelledAtDesc(@Param("cancelledBy") String cancelledBy);
    
    @Query("SELECT COUNT(tc) FROM TradeCancellation tc WHERE tc.tradeId = :tradeId")
    Long countByTradeId(@Param("tradeId") String tradeId);
}
