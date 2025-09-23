package com.vibe.fundsmith.exception;

/**
 * Custom exception for NAV calculation related errors.
 * Used to indicate business logic failures in NAV calculations such as:
 * - Missing or invalid portfolio data
 * - Invalid shares outstanding
 * - Missing market prices
 */
public class NavCalculationException extends RuntimeException {
    
    /**
     * Creates a new NAV calculation exception with a message
     *
     * @param message Detailed error message
     */
    public NavCalculationException(String message) {
        super(message);
    }
    
    /**
     * Creates a new NAV calculation exception with a message and cause
     *
     * @param message Detailed error message
     * @param cause The underlying cause of the error
     */
    public NavCalculationException(String message, Throwable cause) {
        super(message, cause);
    }
}