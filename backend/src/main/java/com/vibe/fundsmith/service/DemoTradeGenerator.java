package com.vibe.fundsmith.service;

import com.vibe.fundsmith.config.DemoConfig;
import com.vibe.fundsmith.dto.TradeRequest;
import com.vibe.fundsmith.dto.TradeResponse;
import com.vibe.fundsmith.model.TradeSide;
import com.vibe.fundsmith.model.TradeStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@Service
@ConditionalOnProperty(name = "demo.enabled", havingValue = "true")
public class DemoTradeGenerator {
    private static final Logger log = LoggerFactory.getLogger(DemoTradeGenerator.class);
    private static final List<String> CURRENCIES = Arrays.asList("USD", "EUR", "GBP");
    private static final List<String> PORTFOLIO_IDS = Arrays.asList("DEFAULT", "GLOBAL_EQUITY", "TECH_GROWTH");
    
    private final TradeService tradeService;
    private final DemoConfig config;
    private final Random random;
    private boolean running;
    private long tradeCount = 0;

    public DemoTradeGenerator(TradeService tradeService, DemoConfig config) {
        this.tradeService = tradeService;
        this.config = config;
        this.random = new Random();
        this.running = true;
    }

    @Scheduled(fixedRate = 5000)
    public void generateTrade() {
        if (!running) return;
        
        try {
            TradeRequest trade = generateRandomTrade();
            
            // Set this field to be captured by the TradeService
            trade.setTradeId(UUID.randomUUID());
            
            TradeResponse response = tradeService.bookTrade(trade);
            tradeCount++;
            
            log.info("Generated synthetic trade: tradeId={}, isin={}, quantity={}, price={}, side={}",
                response.getTradeId(), trade.getIsin(), trade.getQuantity(), trade.getPrice(), trade.getSide());
        } catch (Exception e) {
            log.error("Failed to generate synthetic trade", e);
        }
    }

    private TradeRequest generateRandomTrade() {
        // Pick random values for required and optional fields
        String isin = pickRandomIsin();
        BigDecimal basePrice = config.getBasePrices().get(isin);
        BigDecimal jitteredPrice = applyRandomJitter(basePrice);
        long quantity = generateRandomQuantity();
        
        // Default to BUY for now as only BUY is supported in TradeService
        TradeSide side = TradeSide.BUY;
        
        // Generate random values for optional fields
        String tradeCurrency = pickRandomCurrency();
        String portfolioId = pickRandomPortfolio();

        TradeRequest trade = new TradeRequest();
        trade.setTradeId(UUID.randomUUID()); // Generate a UUID for idempotency
        trade.setIsin(isin);
        trade.setQuantity(quantity);
        trade.setPrice(jitteredPrice);
        trade.setSide(side);
        trade.setTradeCurrency(tradeCurrency);
        trade.setPortfolioId(portfolioId);
        trade.setTradeDate(LocalDate.now());
        
        return trade;
    }

    private String pickRandomIsin() {
        int index = random.nextInt(config.getIsins().size());
        return config.getIsins().get(index);
    }
    
    private String pickRandomCurrency() {
        int index = random.nextInt(CURRENCIES.size());
        return CURRENCIES.get(index);
    }
    
    private String pickRandomPortfolio() {
        int index = random.nextInt(PORTFOLIO_IDS.size());
        return PORTFOLIO_IDS.get(index);
    }

    private BigDecimal applyRandomJitter(BigDecimal basePrice) {
        double jitterPct = (random.nextDouble() * 2 - 1) * config.getMaxPriceJitter();
        return basePrice.multiply(BigDecimal.ONE.add(BigDecimal.valueOf(jitterPct)))
            .setScale(2, RoundingMode.HALF_UP);
    }

    private long generateRandomQuantity() {
        long range = (config.getMaxQuantity() - config.getMinQuantity()) / config.getQuantityStep();
        return config.getMinQuantity() + random.nextInt((int)range + 1) * config.getQuantityStep();
    }

    // Admin endpoints support
    public void start() {
        running = true;
        log.info("Demo trade generator started");
    }

    public void stop() {
        running = false;
        log.info("Demo trade generator stopped");
    }
    
    // Metrics
    public long getTradeCount() {
        return tradeCount;
    }
}