package com.vibe.fundsmith.dto;

/**
 * DTO representing the result of a cash balance reset operation
 */
public class CashResetResultDto {
    private boolean success;
    private String message;
    private String newBalance;
    private String currency;
    
    public CashResetResultDto() {}
    
    public CashResetResultDto(boolean success, String message, String newBalance, String currency) {
        this.success = success;
        this.message = message;
        this.newBalance = newBalance;
        this.currency = currency;
    }
    
    // Getters and setters
    public boolean isSuccess() {
        return success;
    }
    
    public void setSuccess(boolean success) {
        this.success = success;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public String getNewBalance() {
        return newBalance;
    }
    
    public void setNewBalance(String newBalance) {
        this.newBalance = newBalance;
    }
    
    public String getCurrency() {
        return currency;
    }
    
    public void setCurrency(String currency) {
        this.currency = currency;
    }
}