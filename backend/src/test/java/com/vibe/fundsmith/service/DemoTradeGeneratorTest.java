package com.vibe.fundsmith.service;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.vibe.fundsmith.config.DemoConfig;

@ExtendWith(MockitoExtension.class)
public class DemoTradeGeneratorTest {

    @Mock
    private TradeService tradeService;
    
    @Mock
    private DemoConfig config;
    
    @InjectMocks
    private DemoTradeGenerator generator;
    
    @Test
    public void testEnableBackdatedTradeMode() {
        // Default should be REGULAR mode
        assertEquals(DemoTradeGenerator.GenerationMode.REGULAR, generator.getGenerationMode());
        
        // Enable backdated mode
        generator.enableBackdatedTradeMode();
        
        // Check if mode changed
        assertEquals(DemoTradeGenerator.GenerationMode.BACKDATED, generator.getGenerationMode());
        
        // Switch back to regular mode
        generator.enableRegularTradeMode();
        
        // Check if mode changed back
        assertEquals(DemoTradeGenerator.GenerationMode.REGULAR, generator.getGenerationMode());
    }
}
