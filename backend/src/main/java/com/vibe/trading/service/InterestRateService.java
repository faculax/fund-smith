package com.vibe.trading.service;

import com.vibe.trading.model.dto.InterestRateResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import jakarta.annotation.PostConstruct;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class InterestRateService {

    @Value("${api.ninjas.key:demo}")
    private String apiKey;

    @Value("${api.ninjas.base-url:https://api.api-ninjas.com/v1}")
    private String baseUrl;

    private final RestTemplate restTemplate = new RestTemplate();
    private final Map<String, BigDecimal> cachedRates = new ConcurrentHashMap<>();
    private LocalDateTime lastFetchTime = LocalDateTime.now();

    // Currency to API parameter mapping
    private static final Map<String, String> CURRENCY_TO_RATE_PARAM = Map.of(
        "USD", "usd_fed_funds_rate",
        "EUR", "euribor_3_months", 
        "JPY", "jpy_tibor_3_months",
        "GBP", "gbp_libor_3_months",
        "AUD", "aud_cash_rate_target"
    );

    // Mock interest rates for development (fallback when API is not available)
    private static final Map<String, BigDecimal> MOCK_INTEREST_RATES = Map.of(
        "USD", BigDecimal.valueOf(0.0525), // 5.25%
        "EUR", BigDecimal.valueOf(0.0450), // 4.50%
        "JPY", BigDecimal.valueOf(0.0010), // 0.10%
        "GBP", BigDecimal.valueOf(0.0520), // 5.20%
        "AUD", BigDecimal.valueOf(0.0425)  // 4.25%
    );

    // Tenor to year fraction mapping
    private static final Map<String, BigDecimal> TENOR_TO_YEAR_FRACTION = Map.of(
        "1W", BigDecimal.valueOf(7.0 / 360.0),
        "1M", BigDecimal.valueOf(1.0 / 12.0),
        "3M", BigDecimal.valueOf(0.25),
        "6M", BigDecimal.valueOf(0.5),
        "1Y", BigDecimal.valueOf(1.0)
    );

    @PostConstruct
    public void init() {
        log.info("Initializing InterestRateService with mock rates on startup.");
        cachedRates.putAll(MOCK_INTEREST_RATES);
        lastFetchTime = LocalDateTime.now();
        log.info("Mock rates initialized. Total mock rates: {}", cachedRates.size());
    }

    @Scheduled(fixedRate = 3600000) // 1 hour
    public void fetchInterestRates() {
        try {
            log.info("Fetching interest rates from API");
            
            // Check if we have a valid API key
            if ("demo".equals(apiKey)) {
                log.warn("Using demo API key, falling back to mock interest rates");
                useMockRates();
                return;
            }
            
            for (Map.Entry<String, String> entry : CURRENCY_TO_RATE_PARAM.entrySet()) {
                String currency = entry.getKey();
                String rateParam = entry.getValue();
                
                String url = baseUrl + "/interestrate?country=" + rateParam;
                InterestRateResponse response = restTemplate.getForObject(url, InterestRateResponse.class);
                
                if (response != null && response.getCentral_bank_rates() != null && !response.getCentral_bank_rates().isEmpty()) {
                    BigDecimal rate = response.getCentral_bank_rates().get(0).getRate_pct();
                    // Convert percentage to decimal (e.g., 5.25% -> 0.0525)
                    BigDecimal decimalRate = rate.divide(BigDecimal.valueOf(100), 6, RoundingMode.HALF_UP);
                    cachedRates.put(currency, decimalRate);
                    log.debug("Updated interest rate for {}: {}% (decimal: {})", currency, rate, decimalRate);
                }
            }
            
            lastFetchTime = LocalDateTime.now();
            log.info("Successfully updated interest rates for {} currencies", cachedRates.size());
            
        } catch (Exception e) {
            log.error("Error fetching interest rates: {}", e.getMessage());
            log.info("Falling back to mock interest rates for development");
            useMockRates();
        }
    }

    private void useMockRates() {
        cachedRates.clear();
        cachedRates.putAll(MOCK_INTEREST_RATES);
        lastFetchTime = LocalDateTime.now();
        log.info("Using mock interest rates for development");
    }

    public BigDecimal getInterestRate(String currency) {
        return cachedRates.getOrDefault(currency, BigDecimal.ZERO);
    }

    public BigDecimal getYearFraction(String tenor) {
        return TENOR_TO_YEAR_FRACTION.getOrDefault(tenor, BigDecimal.ZERO);
    }

    public BigDecimal calculateForwardRate(BigDecimal spotRate, String baseCurrency, String quoteCurrency, String tenor) {
        try {
            BigDecimal rBase = getInterestRate(baseCurrency);
            BigDecimal rQuote = getInterestRate(quoteCurrency);
            BigDecimal T = getYearFraction(tenor);

            if (T.compareTo(BigDecimal.ZERO) == 0) {
                log.warn("Invalid tenor: {}", tenor);
                return spotRate;
            }

            // Forward Rate = Spot × (1 + rBase × T) / (1 + rQuote × T)
            BigDecimal numerator = BigDecimal.ONE.add(rBase.multiply(T));
            BigDecimal denominator = BigDecimal.ONE.add(rQuote.multiply(T));
            BigDecimal forwardRate = spotRate.multiply(numerator).divide(denominator, 6, RoundingMode.HALF_UP);

            log.debug("Forward calculation: Spot={}, Base={} ({}%), Quote={} ({}%), Tenor={}, Forward={}", 
                spotRate, baseCurrency, rBase.multiply(BigDecimal.valueOf(100)), 
                quoteCurrency, rQuote.multiply(BigDecimal.valueOf(100)), tenor, forwardRate);

            return forwardRate;
        } catch (Exception e) {
            log.error("Error calculating forward rate: {}", e.getMessage());
            return spotRate;
        }
    }

    public BigDecimal calculateForwardPoints(BigDecimal spotRate, BigDecimal forwardRate) {
        return forwardRate.subtract(spotRate);
    }

    public LocalDateTime getLastFetchTime() {
        return lastFetchTime;
    }

    public Map<String, BigDecimal> getCachedRates() {
        return new ConcurrentHashMap<>(cachedRates);
    }
} 