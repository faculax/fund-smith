package com.vibe.fundsmith.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

/**
 * Represents an individual line item in a journal entry (either a debit or credit).
 */
@Entity
@Table(name = "journal_lines")
public class JournalLine {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "journal_id", nullable = false)
    private Journal journal;
    
    @Column(name = "account", nullable = false)
    private String account;
    
    @Column(name = "dr", nullable = false, precision = 18, scale = 4)
    private BigDecimal debit;
    
    @Column(name = "cr", nullable = false, precision = 18, scale = 4)
    private BigDecimal credit;
    
    // Default constructor for JPA
    protected JournalLine() {}
    
    public JournalLine(Journal journal, String account, BigDecimal debit, BigDecimal credit) {
        this.journal = journal;
        this.account = account;
        this.debit = debit != null ? debit : BigDecimal.ZERO;
        this.credit = credit != null ? credit : BigDecimal.ZERO;
        
        // Enforce business rule: a line can't have both debit and credit
        if (this.debit.compareTo(BigDecimal.ZERO) > 0 && this.credit.compareTo(BigDecimal.ZERO) > 0) {
            throw new IllegalArgumentException("A journal line cannot have both debit and credit amounts");
        }
    }
    
    // Getters and setters
    public Long getId() {
        return id;
    }
    
    public Journal getJournal() {
        return journal;
    }
    
    public void setJournal(Journal journal) {
        this.journal = journal;
    }
    
    public String getAccount() {
        return account;
    }
    
    public void setAccount(String account) {
        this.account = account;
    }
    
    public BigDecimal getDebit() {
        return debit;
    }
    
    public void setDebit(BigDecimal debit) {
        this.debit = debit != null ? debit : BigDecimal.ZERO;
    }
    
    public BigDecimal getCredit() {
        return credit;
    }
    
    public void setCredit(BigDecimal credit) {
        this.credit = credit != null ? credit : BigDecimal.ZERO;
    }
}