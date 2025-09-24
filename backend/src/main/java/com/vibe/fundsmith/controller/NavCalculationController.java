package com.vibe.fundsmith.controller;

import com.vibe.fundsmith.dto.NavCalculationDto;
import com.vibe.fundsmith.model.NavCalculation;
import com.vibe.fundsmith.repository.NavCalculationRepository;
import com.vibe.fundsmith.service.NavCalculationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * REST controller for NAV endpoints (Story 5.4).
 * - POST /api/nav/calculate -> triggers calculation and returns saved snapshot
 * (includes id)
 * - GET /api/nav/latest -> returns latest snapshot
 * - GET /api/nav/history -> returns newest-first limited history
 * - GET /api/nav/history/range -> returns historical snapshots between start
 * and end (asc)
 */
@RestController
@RequestMapping("/api/nav")
public class NavCalculationController {
    private static final Logger log = LoggerFactory.getLogger(NavCalculationController.class);

    private final NavCalculationService navCalculationService;
    private final NavCalculationRepository navCalculationRepository;

    public NavCalculationController(NavCalculationService navCalculationService,
            NavCalculationRepository navCalculationRepository) {
        this.navCalculationService = navCalculationService;
        this.navCalculationRepository = navCalculationRepository;
    }

    @PostMapping("/calculate")
    public ResponseEntity<NavCalculationDto> calculateNav(
            @RequestParam(defaultValue = "DEFAULT") String portfolioId) {
        try {
            NavCalculation snapshot = navCalculationService.calculateNav(portfolioId);
            return ResponseEntity.ok(NavCalculationDto.fromEntity(snapshot));
        } catch (Exception e) {
            log.error("Error calculating NAV: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/latest")
    public ResponseEntity<NavCalculationDto> getLatest(
            @RequestParam(defaultValue = "DEFAULT") String portfolioId) {
        try {
            return navCalculationRepository.findTopByPortfolioIdOrderByCalculationDateDesc(portfolioId)
                    .map(NavCalculationDto::fromEntity)
                    .map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.notFound().build());
        } catch (Exception e) {
            log.error("Error fetching latest NAV: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/history")
    public ResponseEntity<List<NavCalculationDto>> getHistory(
            @RequestParam(defaultValue = "DEFAULT") String portfolioId,
            @RequestParam(required = false, defaultValue = "30") int limit) {
        try {
            int safeLimit = Math.min(Math.max(limit, 1), 1000);
            Pageable pageable = PageRequest.of(0, safeLimit, Sort.by(Sort.Direction.DESC, "calculationDate"));

            List<NavCalculationDto> list = navCalculationRepository.findByPortfolioId(portfolioId, pageable)
                    .map(NavCalculationDto::fromEntity)
                    .stream()
                    .collect(Collectors.toList());

            return ResponseEntity.ok(list);
        } catch (Exception e) {
            log.error("Error fetching NAV history: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/history/range")
    public ResponseEntity<List<NavCalculationDto>> getHistoryRange(
            @RequestParam(defaultValue = "DEFAULT") String portfolioId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) ZonedDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) ZonedDateTime endDate) {
        try {
            List<NavCalculationDto> list = navCalculationService
                    .getNavHistory(portfolioId, startDate, endDate)
                    .stream()
                    .map(NavCalculationDto::fromEntity)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(list);
        } catch (Exception e) {
            log.error("Error fetching NAV history range: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Delete all NAV snapshots (for system reset)
     *
     * @return Response with count of deleted snapshots
     */
    @DeleteMapping
    public ResponseEntity<Map<String, Object>> deleteAllSnapshots() {
        try {
            long deletedCount = navCalculationRepository.count();
            navCalculationRepository.deleteAll();
            log.info("Deleted {} NAV snapshots", deletedCount);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "All NAV snapshots have been deleted",
                    "deletedCount", deletedCount));
        } catch (Exception e) {
            log.error("Error deleting NAV snapshots: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
}