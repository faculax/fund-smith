package com.vibe.trading.repository;

import com.vibe.trading.model.Trade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface TradeRepository extends JpaRepository<Trade, Long> {

    Optional<Trade> findByTradeId(String tradeId);

    List<Trade> findByTradeDate(LocalDate tradeDate);

    List<Trade> findByTradeType(Trade.TradeType tradeType);

    List<Trade> findByStatus(Trade.TradeStatus status);

    List<Trade> findByCreatedBy(String createdBy);

    List<Trade> findByCounterparty(String counterparty);

    List<Trade> findByCurrencyPair(String currencyPair);

    @Query("SELECT t FROM Trade t WHERE t.tradeDate BETWEEN :startDate AND :endDate")
    List<Trade> findByTradeDateBetween(@Param("startDate") LocalDate startDate, 
                                      @Param("endDate") LocalDate endDate);

    @Query("SELECT t FROM Trade t WHERE t.currencyPair = :currencyPair AND t.tradeDate = :tradeDate")
    List<Trade> findByCurrencyPairAndTradeDate(@Param("currencyPair") String currencyPair, 
                                              @Param("tradeDate") LocalDate tradeDate);

    @Query("SELECT t FROM Trade t WHERE t.tradeDate BETWEEN :startDate AND :endDate ORDER BY t.createdAt DESC")
    List<Trade> findByTradeDateBetweenOrderByCreatedAtDesc(@Param("startDate") LocalDate startDate, 
                                                          @Param("endDate") LocalDate endDate);

    @Query("SELECT COUNT(t) FROM Trade t WHERE t.status = :status")
    Long countByStatus(@Param("status") Trade.TradeStatus status);

    @Query("SELECT COUNT(t) FROM Trade t WHERE t.tradeType = :tradeType")
    Long countByTradeType(@Param("tradeType") Trade.TradeType tradeType);

    @Query("SELECT SUM(t.notionalAmount) FROM Trade t WHERE t.status = :status")
    Double sumNotionalAmountByStatus(@Param("status") Trade.TradeStatus status);

    // User-based filtering methods for Story 34.5
    List<Trade> findByBookedByUserId(String userId);

    List<Trade> findByBookedByUsername(String username);

    @Query("SELECT COUNT(t) FROM Trade t WHERE t.bookedByUserId = :userId")
    Long countByBookedByUserId(@Param("userId") String userId);

    boolean existsByTradeId(String tradeId);
} 