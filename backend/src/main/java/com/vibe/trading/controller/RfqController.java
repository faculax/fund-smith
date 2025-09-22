package com.vibe.trading.controller;

import com.vibe.trading.model.RfqOrder;
import com.vibe.trading.model.dto.RfqRequest;
import com.vibe.trading.model.dto.RfqResponse;
import com.vibe.trading.service.RfqService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rfq")
@RequiredArgsConstructor
@Slf4j
public class RfqController {

    private final RfqService rfqService;

    @PostMapping
    public ResponseEntity<RfqResponse> createRfqOrder(@Valid @RequestBody RfqRequest request) {
        log.info("Received RFQ creation request: {}", request);
        try {
            RfqResponse response = rfqService.createRfqOrder(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            log.error("Error creating RFQ order: {}", e.getMessage());
            throw e;
        }
    }

    @GetMapping
    public ResponseEntity<List<RfqResponse>> getAllRfqOrders() {
        List<RfqResponse> rfqOrders = rfqService.getAllRfqOrders();
        return ResponseEntity.ok(rfqOrders);
    }

    @GetMapping("/{id}")
    public ResponseEntity<RfqResponse> getRfqOrderById(@PathVariable Long id) {
        return rfqService.getRfqOrderById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/rfq-id/{rfqId}")
    public ResponseEntity<RfqResponse> getRfqOrderByRfqId(@PathVariable String rfqId) {
        return rfqService.getRfqOrderByRfqId(rfqId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<RfqResponse>> getRfqOrdersByStatus(@PathVariable RfqOrder.RfqStatus status) {
        List<RfqResponse> rfqOrders = rfqService.getRfqOrdersByStatus(status);
        return ResponseEntity.ok(rfqOrders);
    }

    @GetMapping("/client/{clientName}")
    public ResponseEntity<List<RfqResponse>> getRfqOrdersByClient(@PathVariable String clientName) {
        List<RfqResponse> rfqOrders = rfqService.getRfqOrdersByClient(clientName);
        return ResponseEntity.ok(rfqOrders);
    }

    @PutMapping("/{rfqId}/status")
    public ResponseEntity<RfqResponse> updateRfqStatus(
            @PathVariable String rfqId,
            @RequestParam RfqOrder.RfqStatus status) {
        try {
            RfqResponse response = rfqService.updateRfqStatus(rfqId, status);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error updating RFQ status: {}", e.getMessage());
            throw e;
        }
    }

    @PutMapping("/{rfqId}/approve")
    public ResponseEntity<RfqResponse> approveRfqOrder(@PathVariable String rfqId) {
        try {
            RfqResponse response = rfqService.approveRfqOrder(rfqId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error approving RFQ order: {}", e.getMessage());
            throw e;
        }
    }

    @PutMapping("/{rfqId}/verify")
    public ResponseEntity<RfqResponse> verifyRfqOrder(@PathVariable String rfqId) {
        try {
            RfqResponse response = rfqService.verifyRfqOrder(rfqId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error verifying RFQ order: {}", e.getMessage());
            throw e;
        }
    }

    @PutMapping("/{rfqId}/execute")
    public ResponseEntity<RfqResponse> executeRfqOrder(@PathVariable String rfqId) {
        try {
            RfqResponse response = rfqService.executeRfqOrder(rfqId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error executing RFQ order: {}", e.getMessage());
            throw e;
        }
    }
}
