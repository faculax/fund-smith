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
@Table(name = "trades")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Trade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "trade_id", unique = true, nullable = false)
    private String tradeId;

    @Column(name = "trade_date", nullable = false)
    private LocalDate tradeDate;

    @Column(name = "currency_pair", nullable = false)
    private String currencyPair;

    @Enumerated(EnumType.STRING)
    @Column(name = "direction", nullable = false)
    private TradeDirection direction;

    @Column(name = "notional_amount", nullable = false, precision = 19, scale = 4)
    @DecimalMin(value = "0.0001", message = "Notional amount must be greater than 0")
    private BigDecimal notionalAmount;

    @Column(name = "rate", nullable = false, precision = 19, scale = 6)
    @DecimalMin(value = "0.000001", message = "Rate must be greater than 0")
    private BigDecimal rate;

    @Column(name = "counterparty", nullable = false)
    private String counterparty;

    @Column(name = "value_date", nullable = false)
    private LocalDate valueDate;

    @Column(name = "execution_time", nullable = false)
    private LocalDateTime executionTime;

    @Column(name = "lei")
    private String lei;

    @Column(name = "uti")
    private String uti;

    @Enumerated(EnumType.STRING)
    @Column(name = "trade_type", nullable = false)
    private TradeType tradeType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private TradeStatus status;

    @Column(name = "created_by", nullable = false)
    private String createdBy;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // User audit tracking fields for Story 34.4
    @Column(name = "booked_by_user_id")
    private String bookedByUserId;

    @Column(name = "booked_by_username")
    private String bookedByUsername;

    // New regulatory fields
    @Enumerated(EnumType.STRING)
    @Column(name = "emir_mifid_classification")
    private EmirMifidClassification emirMifidClassification;

    @Column(name = "reporting_party")
    private String reportingParty;

    // Forward-specific fields
    @Column(name = "forward_value_date")
    private LocalDate forwardValueDate;

    @Column(name = "forward_points", precision = 19, scale = 6)
    private BigDecimal forwardPoints;

    @Column(name = "net_forward_rate", precision = 19, scale = 6)
    private BigDecimal netForwardRate;

    @Enumerated(EnumType.STRING)
    @Column(name = "pricing_source")
    private PricingSource pricingSource;

    @Column(name = "tenor")
    private String tenor;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (executionTime == null) {
            executionTime = LocalDateTime.now();
        }
        if (status == null) {
            status = TradeStatus.PENDING;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum TradeDirection {
        BUY, SELL
    }

    public enum TradeType {
        FX_SPOT, FX_FORWARD
    }

    public enum TradeStatus {
        PENDING, VERIFIED, CONFIRMED, CANCELLED, SETTLED
    }

    public enum EmirMifidClassification {
        FINANCIAL_COUNTERPARTY, NON_FINANCIAL_COUNTERPARTY, CENTRAL_COUNTERPARTY
    }

    public enum PricingSource {
        MANUAL, SYSTEM, STREAM
    }
} 