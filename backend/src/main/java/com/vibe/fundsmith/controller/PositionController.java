package com.vibe.fundsmith.controller;

import com.vibe.fundsmith.dto.PositionDto;
import com.vibe.fundsmith.service.PositionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/positions")
public class PositionController {
    
    private final PositionService positionService;
    
    @Autowired
    public PositionController(PositionService positionService) {
        this.positionService = positionService;
    }
    
    /**
     * Get all positions
     * 
     * @return List of all positions sorted by ISIN
     */
    @GetMapping
    public List<PositionDto> getAllPositions() {
        return positionService.getAllPositions();
    }
}