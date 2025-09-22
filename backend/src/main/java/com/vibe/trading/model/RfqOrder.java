package com.vibe.trading.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "rfq_orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RfqOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "rfq_id", unique = true, nullable = false)
    private String rfqId;

    @Column(name = "rfq_date", nullable = false)
    private LocalDate rfqDate;

    @Column(name = "currency_pair", nullable = false)
    private String currencyPair;

    @Enumerated(EnumType.STRING)
    @Column(name = "direction", nullable = false)
    private TradeDirection direction;

    @Column(name = "notional_amount", nullable = false, precision = 19, scale = 4)
    @DecimalMin(value = "0.0001", message = "Notional amount must be greater than 0")
    private BigDecimal notionalAmount;

    @Column(name = "value_date", nullable = false)
    private LocalDate valueDate;

    @Column(name = "settlement_date")
    private LocalDate settlementDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "rfq_type", nullable = false)
    private RfqType rfqType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private RfqStatus status;

    @Column(name = "client_name", nullable = false)
    private String clientName;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (status == null) {
            status = RfqStatus.RFQ_SENT;
        }
        if (settlementDate == null && rfqType == RfqType.FX_SPOT) {
            settlementDate = LocalDate.now().plusDays(2); // T+2
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum TradeDirection {
        BUY, SELL
    }

    public enum RfqType {
        FX_SPOT, FX_FORWARD
    }

    public enum RfqStatus {
        RFQ_SENT, QUOTED, ACCEPTED, EXECUTED, VERIFIED, SETTLED, CANCELLED
    }
} 