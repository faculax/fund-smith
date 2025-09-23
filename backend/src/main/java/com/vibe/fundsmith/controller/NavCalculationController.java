package com.vibe.fundsmith.controller;

import com.vibe.fundsmith.dto.NavCalculationDto;
import com.vibe.fundsmith.exception.NavCalculationException;
import com.vibe.fundsmith.service.NavCalculationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * REST controller for NAV calculation endpoints.
 * Provides HTTP endpoints for:
 * - Triggering NAV calculations
 * - Retrieving latest NAV
 * - Accessing historical NAV data
 */
@RestController
@RequestMapping("/api/nav")
public class NavCalculationController {
    private static final Logger log = LoggerFactory.getLogger(NavCalculationController.class);

    private final NavCalculationService navCalculationService;

    public NavCalculationController(NavCalculationService navCalculationService) {
        this.navCalculationService = navCalculationService;
    }

    /**
     * Triggers a new NAV calculation for a portfolio
     * 
     * @param portfolioId Portfolio UUID as path variable
     * @return ResponseEntity containing the NAV calculation result
     */
    @PostMapping("/calculate")
    public ResponseEntity<NavCalculationDto> calculateNav(
            @RequestParam(defaultValue = "DEFAULT") String portfolioId) {
        try {

            return ResponseEntity.ok(
                    NavCalculationDto.fromEntity(
                            navCalculationService.calculateNav(portfolioId)));
        } catch (NavCalculationException e) {
            log.error("NAV calculation failed: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Unexpected error during NAV calculation: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Retrieves the latest NAV calculation for a portfolio
     * 
     * @param portfolioId Portfolio UUID as path variable
     * @return ResponseEntity containing the latest NAV calculation
     */
    @GetMapping("/latest")
    public ResponseEntity<NavCalculationDto> getLatestNav(
            @RequestParam(defaultValue = "DEFAULT") String portfolioId) {
        try {
            return ResponseEntity.ok(
                    NavCalculationDto.fromEntity(
                            navCalculationService.getLatestNav(portfolioId)));
        } catch (NavCalculationException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Unexpected error retrieving latest NAV for portfolio {}: {}", portfolioId, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Retrieves historical NAV calculations within a date range
     * 
     * @param portfolioId Portfolio UUID as path variable
     * @param startDate   Start of date range
     * @param endDate     End of date range
     * @return ResponseEntity containing list of NAV calculations
     */
    @GetMapping("/history")
    public ResponseEntity<List<NavCalculationDto>> getNavHistory(
            @PathVariable String portfolioId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) ZonedDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) ZonedDateTime endDate) {
        try {
            log.info("Retrieving NAV history for portfolio {} between {} and {}",
                    portfolioId, startDate, endDate);

            List<NavCalculationDto> history = navCalculationService
                    .getNavHistory(portfolioId, startDate, endDate)
                    .stream()
                    .map(NavCalculationDto::fromEntity)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(history);
        } catch (Exception e) {
            log.error("Error retrieving NAV history for portfolio {}: {}", portfolioId, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
}