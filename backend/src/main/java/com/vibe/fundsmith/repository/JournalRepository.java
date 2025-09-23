package com.vibe.fundsmith.repository;

import com.vibe.fundsmith.model.Journal;
import com.vibe.fundsmith.model.JournalType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface JournalRepository extends JpaRepository<Journal, UUID> {
    
    /**
     * Find all journals related to a specific trade
     * 
     * @param tradeId The trade ID
     * @return List of journals for the trade
     */
    List<Journal> findByTradeIdOrderByCreatedAtDesc(UUID tradeId);
    
    /**
     * Find a specific type of journal for a trade
     * 
     * @param tradeId The trade ID
     * @param journalType The type of journal
     * @return Optional journal if found
     */
    Optional<Journal> findByTradeIdAndJournalType(UUID tradeId, JournalType journalType);
    
    /**
     * Check if a journal already exists for a trade and type
     * 
     * @param tradeId The trade ID
     * @param journalType The type of journal
     * @return true if a journal exists
     */
    boolean existsByTradeIdAndJournalType(UUID tradeId, JournalType journalType);
    
    /**
     * Find the most recent journals
     * 
     * @param limit Maximum number of journals to return
     * @return List of journals ordered by creation date (newest first)
     */
    List<Journal> findTop10ByOrderByCreatedAtDesc();
}