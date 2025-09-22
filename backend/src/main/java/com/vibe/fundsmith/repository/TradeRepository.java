package com.vibe.fundsmith.repository;

import com.vibe.fundsmith.model.Trade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Pageable;
import java.time.LocalDate;
import java.util.List;

public interface TradeRepository extends JpaRepository<Trade, Long> {
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