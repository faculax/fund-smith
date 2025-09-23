package com.vibe.fundsmith.dto;

import com.vibe.fundsmith.model.Journal;
import com.vibe.fundsmith.model.JournalLine;
import com.vibe.fundsmith.model.JournalType;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * DTO for journal responses in the API
 */
public class JournalDto {
    private UUID journalId;
    private UUID tradeId;
    private String type;
    private String createdAt;
    private List<JournalLineDto> lines = new ArrayList<>();
    private BigDecimal totalDebits = BigDecimal.ZERO;
    private BigDecimal totalCredits = BigDecimal.ZERO;
    
    public JournalDto() {}
    
    /**
     * Create a DTO from a Journal entity
     */
    public static JournalDto fromEntity(Journal journal) {
        JournalDto dto = new JournalDto();
        dto.setJournalId(journal.getId());
        dto.setTradeId(journal.getTradeId());
        dto.setType(journal.getJournalType().toString());
        dto.setCreatedAt(journal.getCreatedAt().toString());
        
        BigDecimal totalDr = BigDecimal.ZERO;
        BigDecimal totalCr = BigDecimal.ZERO;
        
        List<JournalLineDto> lineDtos = new ArrayList<>();
        for (JournalLine line : journal.getLines()) {
            JournalLineDto lineDto = new JournalLineDto();
            lineDto.setAccount(line.getAccount());
            lineDto.setDr(line.getDebit());
            lineDto.setCr(line.getCredit());
            lineDtos.add(lineDto);
            
            totalDr = totalDr.add(line.getDebit());
            totalCr = totalCr.add(line.getCredit());
        }
        
        dto.setLines(lineDtos);
        dto.setTotalDebits(totalDr);
        dto.setTotalCredits(totalCr);
        
        return dto;
    }
    
    // Getters and setters
    public UUID getJournalId() {
        return journalId;
    }
    
    public void setJournalId(UUID journalId) {
        this.journalId = journalId;
    }
    
    public UUID getTradeId() {
        return tradeId;
    }
    
    public void setTradeId(UUID tradeId) {
        this.tradeId = tradeId;
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public String getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
    
    public List<JournalLineDto> getLines() {
        return lines;
    }
    
    public void setLines(List<JournalLineDto> lines) {
        this.lines = lines;
    }
    
    public BigDecimal getTotalDebits() {
        return totalDebits;
    }
    
    public void setTotalDebits(BigDecimal totalDebits) {
        this.totalDebits = totalDebits;
    }
    
    public BigDecimal getTotalCredits() {
        return totalCredits;
    }
    
    public void setTotalCredits(BigDecimal totalCredits) {
        this.totalCredits = totalCredits;
    }
    
    /**
     * DTO for journal line responses
     */
    public static class JournalLineDto {
        private String account;
        private BigDecimal dr;
        private BigDecimal cr;
        
        public JournalLineDto() {}
        
        // Getters and setters
        public String getAccount() {
            return account;
        }
        
        public void setAccount(String account) {
            this.account = account;
        }
        
        public BigDecimal getDr() {
            return dr;
        }
        
        public void setDr(BigDecimal dr) {
            this.dr = dr;
        }
        
        public BigDecimal getCr() {
            return cr;
        }
        
        public void setCr(BigDecimal cr) {
            this.cr = cr;
        }
    }
}