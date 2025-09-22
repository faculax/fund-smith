package com.vibe.trading.repository;

import com.vibe.trading.model.RfqOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface RfqOrderRepository extends JpaRepository<RfqOrder, Long> {

    Optional<RfqOrder> findByRfqId(String rfqId);

    List<RfqOrder> findByRfqDate(LocalDate rfqDate);

    List<RfqOrder> findByRfqType(RfqOrder.RfqType rfqType);

    List<RfqOrder> findByStatus(RfqOrder.RfqStatus status);

    List<RfqOrder> findByClientName(String clientName);

    @Query("SELECT r FROM RfqOrder r WHERE r.rfqDate BETWEEN :startDate AND :endDate")
    List<RfqOrder> findByRfqDateBetween(@Param("startDate") LocalDate startDate, 
                                       @Param("endDate") LocalDate endDate);

    @Query("SELECT r FROM RfqOrder r WHERE r.settlementDate = :settlementDate")
    List<RfqOrder> findBySettlementDate(@Param("settlementDate") LocalDate settlementDate);

    @Query("SELECT r FROM RfqOrder r WHERE r.status = :status AND r.settlementDate = :settlementDate")
    List<RfqOrder> findByStatusAndSettlementDate(@Param("status") RfqOrder.RfqStatus status, 
                                                @Param("settlementDate") LocalDate settlementDate);

    boolean existsByRfqId(String rfqId);
} 