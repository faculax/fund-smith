package com.vibe.fundsmith.repository;

import com.vibe.fundsmith.model.Trade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Pageable;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TradeRepository extends JpaRepository<Trade, Long> {
    /**
     * Find a trade by its business key (tradeId)
     * @param tradeId The UUID of the trade
     * @return The trade if found
     */
    Optional<Trade> findByTradeId(UUID tradeId);
    
    /**
     * Find trades that need settlement (have reached their settle date but don't have settlement markers)
     * @param settleDate The settlement date to check
     * @return List of trades due for settlement
     */
    @org.springframework.data.jpa.repository.Query(
        "SELECT t FROM Trade t WHERE t.settleDate = :settleDate AND t.tradeId NOT IN " +
        "(SELECT sm.tradeId FROM SettlementMarker sm)"
    )
    List<Trade> findBySettleDateAndNotSettled(LocalDate settleDate);
    
    List<Trade> findByTradeDateBetweenOrderByCreatedAtDescIdDesc(
        LocalDate fromDate, 
        LocalDate toDate, 
        Pageable pageable
    );
    
    List<Trade> findByIsinAndTradeDateBetweenOrderByCreatedAtDescIdDesc(
        String isin,
        LocalDate fromDate, 
        LocalDate toDate, 
        Pageable pageable
    );
    
    List<Trade> findByOrderByCreatedAtDescIdDesc(Pageable pageable);
    
    List<Trade> findByIsinOrderByCreatedAtDescIdDesc(String isin, Pageable pageable);
}