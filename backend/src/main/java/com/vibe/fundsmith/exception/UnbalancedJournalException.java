package com.vibe.fundsmith.exception;

/**
 * Exception thrown when a journal is not balanced (debits != credits)
 */
public class UnbalancedJournalException extends RuntimeException {
    
    public static final String ERROR_CODE = "UNBALANCED_JOURNAL";
    
    public UnbalancedJournalException(String message) {
        super(message);
    }
    
    public UnbalancedJournalException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public String getErrorCode() {
        return ERROR_CODE;
    }
}