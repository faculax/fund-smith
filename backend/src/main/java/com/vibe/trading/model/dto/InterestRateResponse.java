package com.vibe.trading.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class InterestRateResponse {
    
    @JsonProperty("central_bank_rates")
    private List<CentralBankRate> central_bank_rates;
    
    @Data
    public static class CentralBankRate {
        @JsonProperty("country")
        private String country;
        
        @JsonProperty("rate_pct")
        private BigDecimal rate_pct;
        
        @JsonProperty("last_updated")
        private String last_updated;
    }
} 