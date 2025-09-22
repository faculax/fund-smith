package com.vibe.fundsmith.service;

import com.vibe.fundsmith.config.DemoConfig;
import com.vibe.fundsmith.dto.TradeRequest;
import com.vibe.fundsmith.model.Trade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Random;

@Service
@ConditionalOnProperty(name = "demo.enabled", havingValue = "true")
public class DemoTradeGenerator {
    private static final Logger log = LoggerFactory.getLogger(DemoTradeGenerator.class);
    private final TradeService tradeService;
    private final DemoConfig config;
    private final Random random;
    private boolean running;

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
            Trade booked = tradeService.bookTrade(trade);
            log.info("Generated synthetic trade: id={}, isin={}, quantity={}, price={}",
                booked.getId(), booked.getIsin(), booked.getQuantity(), booked.getPrice());
        } catch (Exception e) {
            log.error("Failed to generate synthetic trade", e);
        }
    }

    private TradeRequest generateRandomTrade() {
        String isin = pickRandomIsin();
        BigDecimal basePrice = config.getBasePrices().get(isin);
        BigDecimal jitteredPrice = applyRandomJitter(basePrice);
        long quantity = generateRandomQuantity();

        TradeRequest trade = new TradeRequest();
        trade.setIsin(isin);
        trade.setQuantity(quantity);
        trade.setPrice(jitteredPrice);
        trade.setTradeDate(LocalDate.now());
        return trade;
    }

    private String pickRandomIsin() {
        int index = random.nextInt(config.getIsins().size());
        return config.getIsins().get(index);
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
}