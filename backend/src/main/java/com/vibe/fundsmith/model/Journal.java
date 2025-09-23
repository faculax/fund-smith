package com.vibe.fundsmith.model;

import jakarta.persistence.*;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Represents an accounting journal entry in the ABOR.
 * Each journal consists of multiple journal lines that must balance (sum of debits = sum of credits).
 */
@Entity
@Table(name = "journals")
public class Journal {
    
    @Id
    @Column(name = "id")
    private UUID id;
    
    @Column(name = "trade_id", nullable = false)
    private UUID tradeId;
    
    @Column(name = "journal_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private JournalType journalType;
    
    @Column(name = "created_at", nullable = false)
    private ZonedDateTime createdAt;
    
    @OneToMany(mappedBy = "journal", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<JournalLine> lines = new ArrayList<>();
    
    // Default constructor for JPA
    protected Journal() {}
    
    public Journal(UUID tradeId, JournalType journalType) {
        this.id = UUID.randomUUID();
        this.tradeId = tradeId;
        this.journalType = journalType;
        this.createdAt = ZonedDateTime.now();
    }
    
    // Helper method to add a line to this journal
    public void addLine(String account, java.math.BigDecimal debit, java.math.BigDecimal credit) {
        JournalLine line = new JournalLine(this, account, debit, credit);
        this.lines.add(line);
    }
    
    // Getters and setters
    public UUID getId() {
        return id;
    }
    
    public UUID getTradeId() {
        return tradeId;
    }
    
    public void setTradeId(UUID tradeId) {
        this.tradeId = tradeId;
    }
    
    public JournalType getJournalType() {
        return journalType;
    }
    
    public void setJournalType(JournalType journalType) {
        this.journalType = journalType;
    }
    
    public ZonedDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(ZonedDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public List<JournalLine> getLines() {
        return lines;
    }
    
    public void setLines(List<JournalLine> lines) {
        this.lines = lines;
    }
    
    /**
     * Validates that this journal is balanced (sum of debits = sum of credits)
     * @return true if the journal is balanced, false otherwise
     */
    public boolean isBalanced() {
        java.math.BigDecimal totalDebit = java.math.BigDecimal.ZERO;
        java.math.BigDecimal totalCredit = java.math.BigDecimal.ZERO;
        
        for (JournalLine line : lines) {
            totalDebit = totalDebit.add(line.getDebit());
            totalCredit = totalCredit.add(line.getCredit());
        }
        
        return totalDebit.compareTo(totalCredit) == 0;
    }
}