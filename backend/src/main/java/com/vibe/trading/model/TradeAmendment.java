package com.vibe.trading.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "trade_amendments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TradeAmendment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "trade_id", nullable = false)
    private String tradeId;

    @Column(name = "amendment_version", nullable = false)
    private Integer amendmentVersion;

    @Column(name = "amended_by", nullable = false)
    private String amendedBy;

    @Column(name = "amended_at", nullable = false)
    private LocalDateTime amendedAt;

    @Column(name = "amendment_reason")
    private String amendmentReason;

    // Original values before amendment
    @Column(name = "original_notional_amount", precision = 19, scale = 4)
    private BigDecimal originalNotionalAmount;

    @Column(name = "original_rate", precision = 19, scale = 6)
    private BigDecimal originalRate;

    @Column(name = "original_value_date")
    private LocalDate originalValueDate;

    @Column(name = "original_lei")
    private String originalLei;

    @Column(name = "original_uti")
    private String originalUti;

    @Column(name = "original_emir_mifid_classification")
    @Enumerated(EnumType.STRING)
    private Trade.EmirMifidClassification originalEmirMifidClassification;

    @Column(name = "original_reporting_party")
    private String originalReportingParty;

    // New values after amendment
    @Column(name = "new_notional_amount", precision = 19, scale = 4)
    private BigDecimal newNotionalAmount;

    @Column(name = "new_rate", precision = 19, scale = 6)
    private BigDecimal newRate;

    @Column(name = "new_value_date")
    private LocalDate newValueDate;

    @Column(name = "new_lei")
    private String newLei;

    @Column(name = "new_uti")
    private String newUti;

    @Column(name = "new_emir_mifid_classification")
    @Enumerated(EnumType.STRING)
    private Trade.EmirMifidClassification newEmirMifidClassification;

    @Column(name = "new_reporting_party")
    private String newReportingParty;

    @PrePersist
    protected void onCreate() {
        this.amendedAt = LocalDateTime.now();
    }
}
