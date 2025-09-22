package com.vibe.trading.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

@Entity
@Table(name = "trade_cancellations")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TradeCancellation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "trade_id", nullable = false)
    private String tradeId;

    @Column(name = "cancelled_by", nullable = false)
    private String cancelledBy;

    @Column(name = "cancelled_at", nullable = false)
    private LocalDateTime cancelledAt;

    @Column(name = "cancellation_reason", columnDefinition = "TEXT")
    private String cancellationReason;

    // Store original trade status before cancellation
    @Enumerated(EnumType.STRING)
    @Column(name = "original_status")
    private Trade.TradeStatus originalStatus;

    @PrePersist
    protected void onCreate() {
        this.cancelledAt = LocalDateTime.now();
    }
}
