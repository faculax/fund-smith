package com.vibe.fundsmith.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vibe.fundsmith.dto.PositionDto;
import com.vibe.fundsmith.event.PositionUpdatedEvent;
import com.vibe.fundsmith.model.Position;
import com.vibe.fundsmith.model.ProcessedTrade;
import com.vibe.fundsmith.model.TradeSide;
import com.vibe.fundsmith.repository.PositionRepository;
import com.vibe.fundsmith.repository.ProcessedTradeRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PositionService {
    private static final Logger log = LoggerFactory.getLogger(PositionService.class);

    private final PositionRepository positionRepository;
    private final ProcessedTradeRepository processedTradeRepository;
    private final ObjectMapper objectMapper;

    @Autowired
    public PositionService(PositionRepository positionRepository,
            ProcessedTradeRepository processedTradeRepository,
            ObjectMapper objectMapper) {
        this.positionRepository = positionRepository;
        this.processedTradeRepository = processedTradeRepository;
        this.objectMapper = objectMapper;
    }

    /**
     * Get all positions
     * This is used by NAV calculation and position queries
     * 
     * @return List of all positions sorted by ISIN
     */
    public List<Position> getPositions() {
        return positionRepository.findAllByOrderByIsinAsc();
    }

    /**
     * Update a position for a given trade if it hasn't been processed already.
     * 
     * @param tradeId  Unique identifier of the trade
     * @param isin     ISIN of the instrument
     * @param quantity Quantity of the trade
     * @param side     Side of the trade (BUY/SELL)
     * @return true if position was updated, false if trade was already processed
     * @throws RuntimeException if the update fails
     */
    @Transactional
    public boolean updatePosition(UUID tradeId, String isin, BigDecimal quantity, TradeSide side) {
        // Check for idempotency - if trade already processed, skip
        if (processedTradeRepository.existsByTradeId(tradeId)) {
            log.info("Trade {} already processed - skipping position update", tradeId);
            return false;
        }

        // Calculate delta based on side
        BigDecimal delta = (side == TradeSide.BUY) ? quantity : quantity.negate();

        // Get current position (if exists) before updating
        Optional<Position> currentPositionOpt = positionRepository.findById(isin);
        BigDecimal currentQuantity = currentPositionOpt.map(Position::getQuantity)
                .orElse(BigDecimal.ZERO);

        // Check if resulting quantity would be negative when SELL is not fully
        // supported
        BigDecimal newQuantity = currentQuantity.add(delta);
        if (newQuantity.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalStateException("Position would become negative: " + isin +
                    ", current: " + currentQuantity + ", delta: " + delta);
        }

        // Perform atomic update or insert
        int updated = positionRepository.updateQuantity(isin, delta);
        // If no rows were affected, insert new position
        if (updated == 0) {
            positionRepository.insertPosition(isin, delta);
        }

        // Register trade as processed for idempotency
        processedTradeRepository.save(new ProcessedTrade(tradeId, isin, delta));

        // Fetch the updated position
        Position updatedPosition = positionRepository.findById(isin)
                .orElseThrow(() -> new RuntimeException("Position not found after update: " + isin));

        // Emit position updated event (log for now)
        emitPositionUpdatedEvent(isin, delta, updatedPosition.getQuantity(), updatedPosition.getUpdatedAt());

        log.info("Updated position for ISIN {}: delta={}, new quantity={}",
                isin, delta, updatedPosition.getQuantity());

        return true;
    }

    /**
     * Get all positions
     * 
     * @return List of all positions sorted by ISIN
     */
    public List<PositionDto> getAllPositions() {
        return positionRepository.findAllByOrderByIsinAsc()
                .stream()
                .map(p -> new PositionDto(p.getIsin(), p.getQuantity(), p.getUpdatedAt()))
                .collect(Collectors.toList());
    }

    /**
     * Emit position updated event
     */
    private void emitPositionUpdatedEvent(String isin, BigDecimal delta,
            BigDecimal newQuantity, ZonedDateTime updatedAt) {
        PositionUpdatedEvent event = new PositionUpdatedEvent(isin, delta, newQuantity, updatedAt);
        try {
            // For now, just log the event as JSON
            log.info("EVENT: {}", objectMapper.writeValueAsString(event));
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize position updated event", e);
        }
        // In the future, publish to message bus or event stream
    }
}