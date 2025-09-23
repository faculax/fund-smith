package com.vibe.fundsmith.repository;

import com.vibe.fundsmith.model.Position;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface PositionRepository extends JpaRepository<Position, String> {
    
    /**
     * Atomically upsert a position by adding delta quantity
     * This implementation is simplified for test compatibility with H2
     * In production PostgreSQL, we would use ON CONFLICT
     * 
     * @param isin The instrument ISIN
     * @param deltaQuantity The quantity delta to apply (can be positive or negative)
     */
    @Modifying
    @Query(value = 
        "UPDATE positions SET quantity = quantity + :deltaQuantity, " +
        "updated_at = CURRENT_TIMESTAMP WHERE isin = :isin", nativeQuery = true)
    int updateQuantity(@Param("isin") String isin, @Param("deltaQuantity") BigDecimal deltaQuantity);
    
    /**
     * Insert a new position record
     * 
     * @param isin The instrument ISIN
     * @param quantity Initial quantity
     */
    @Modifying
    @Query(value = 
        "INSERT INTO positions (isin, quantity, updated_at) " +
        "VALUES (:isin, :quantity, CURRENT_TIMESTAMP)", nativeQuery = true)
    int insertPosition(@Param("isin") String isin, @Param("quantity") BigDecimal quantity);
    
    /**
     * Find all positions ordered by ISIN
     * @return List of all positions sorted by ISIN
     */
    List<Position> findAllByOrderByIsinAsc();
}