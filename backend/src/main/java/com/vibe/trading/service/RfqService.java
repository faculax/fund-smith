package com.vibe.trading.service;

import com.vibe.trading.model.RfqOrder;
import com.vibe.trading.model.dto.RfqRequest;
import com.vibe.trading.model.dto.RfqResponse;
import com.vibe.trading.repository.RfqOrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RfqService {

    private final RfqOrderRepository rfqOrderRepository;

    @Transactional
    public RfqResponse createRfqOrder(RfqRequest request) {
        log.info("Creating RFQ order: {}", request);

        // Validate value date based on RFQ type
        validateValueDate(request);

        // Generate unique RFQ ID
        String rfqId = generateRfqId();

        // Create RFQ order entity
        RfqOrder rfqOrder = RfqOrder.builder()
                .rfqId(rfqId)
                .rfqDate(LocalDate.now())
                .currencyPair(request.getCurrencyPair())
                .direction(request.getDirection())
                .notionalAmount(request.getNotionalAmount())
                .valueDate(request.getValueDate())
                .rfqType(request.getRfqType())
                .status(RfqOrder.RfqStatus.RFQ_SENT)
                .clientName(request.getClientName())
                .build();

        RfqOrder savedRfqOrder = rfqOrderRepository.save(rfqOrder);
        log.info("RFQ order created successfully: {}", savedRfqOrder.getRfqId());

        return mapToResponse(savedRfqOrder);
    }

    @Transactional(readOnly = true)
    public List<RfqResponse> getAllRfqOrders() {
        return rfqOrderRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Optional<RfqResponse> getRfqOrderById(Long id) {
        return rfqOrderRepository.findById(id)
                .map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public Optional<RfqResponse> getRfqOrderByRfqId(String rfqId) {
        return rfqOrderRepository.findByRfqId(rfqId)
                .map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public List<RfqResponse> getRfqOrdersByStatus(RfqOrder.RfqStatus status) {
        return rfqOrderRepository.findByStatus(status).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<RfqResponse> getRfqOrdersByClient(String clientName) {
        return rfqOrderRepository.findByClientName(clientName).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public RfqResponse updateRfqStatus(String rfqId, RfqOrder.RfqStatus status) {
        Optional<RfqOrder> rfqOpt = rfqOrderRepository.findByRfqId(rfqId);
        if (rfqOpt.isEmpty()) {
            throw new RuntimeException("RFQ order not found: " + rfqId);
        }

        RfqOrder rfqOrder = rfqOpt.get();
        rfqOrder.setStatus(status);
        RfqOrder updatedRfqOrder = rfqOrderRepository.save(rfqOrder);
        
        log.info("RFQ status updated: {} -> {}", rfqId, status);
        return mapToResponse(updatedRfqOrder);
    }

    @Transactional
    public RfqResponse approveRfqOrder(String rfqId) {
        return updateRfqStatus(rfqId, RfqOrder.RfqStatus.ACCEPTED);
    }

    @Transactional
    public RfqResponse verifyRfqOrder(String rfqId) {
        return updateRfqStatus(rfqId, RfqOrder.RfqStatus.VERIFIED);
    }

    @Transactional
    public RfqResponse executeRfqOrder(String rfqId) {
        return updateRfqStatus(rfqId, RfqOrder.RfqStatus.EXECUTED);
    }

    // Scheduled task to automatically settle RFQ orders on settlement date
    @Scheduled(cron = "0 0 9 * * ?") // Run at 9 AM every day
    @Transactional
    public void settleRfqOrders() {
        LocalDate today = LocalDate.now();
        log.info("Checking for RFQ orders to settle on: {}", today);

        List<RfqOrder> ordersToSettle = rfqOrderRepository.findByStatusAndSettlementDate(
                RfqOrder.RfqStatus.VERIFIED, today);

        for (RfqOrder order : ordersToSettle) {
            order.setStatus(RfqOrder.RfqStatus.SETTLED);
            rfqOrderRepository.save(order);
            log.info("RFQ order settled: {}", order.getRfqId());
        }
    }

    private void validateValueDate(RfqRequest request) {
        LocalDate spotDate = LocalDate.now().plusDays(2); // T+2 for spot
        
        if (request.getRfqType() == RfqOrder.RfqType.FX_SPOT) {
            if (request.getValueDate() != null && !request.getValueDate().equals(spotDate)) {
                throw new IllegalArgumentException("FX Spot RFQ orders must have value date T+2");
            }
        } else if (request.getRfqType() == RfqOrder.RfqType.FX_FORWARD) {
            if (request.getValueDate() == null) {
                throw new IllegalArgumentException("FX Forward RFQ orders must specify a value date");
            }
            if (request.getValueDate().isBefore(spotDate.plusDays(1))) {
                throw new IllegalArgumentException("FX Forward RFQ orders must have value date after T+2");
            }
        }
    }

    private String generateRfqId() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String uuid = UUID.randomUUID().toString().substring(0, 8);
        return "RFQ" + timestamp + uuid;
    }

    private RfqResponse mapToResponse(RfqOrder rfqOrder) {
        return RfqResponse.builder()
                .id(rfqOrder.getId())
                .rfqId(rfqOrder.getRfqId())
                .rfqDate(rfqOrder.getRfqDate())
                .currencyPair(rfqOrder.getCurrencyPair())
                .direction(rfqOrder.getDirection())
                .notionalAmount(rfqOrder.getNotionalAmount())
                .valueDate(rfqOrder.getValueDate())
                .settlementDate(rfqOrder.getSettlementDate())
                .rfqType(rfqOrder.getRfqType())
                .status(rfqOrder.getStatus())
                .clientName(rfqOrder.getClientName())
                .createdAt(rfqOrder.getCreatedAt())
                .updatedAt(rfqOrder.getUpdatedAt())
                .build();
    }
} 