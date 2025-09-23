package com.vibe.fundsmith.dto;

/**
 * DTO representing cash balance for API responses
 */
public class CashBalanceDto {
    private String balance;
    private String currency;
    
    public CashBalanceDto() {}
    
    public CashBalanceDto(String balance, String currency) {
        this.balance = balance;
        this.currency = currency;
    }
    
    // Getters and setters
    public String getBalance() {
        return balance;
    }
    
    public void setBalance(String balance) {
        this.balance = balance;
    }
    
    public String getCurrency() {
        return currency;
    }
    
    public void setCurrency(String currency) {
        this.currency = currency;
    }
}