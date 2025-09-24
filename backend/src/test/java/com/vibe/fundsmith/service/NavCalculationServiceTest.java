package com.vibe.fundsmith.service;

import com.vibe.fundsmith.config.DemoConfig;
import com.vibe.fundsmith.model.NavCalculation;
import com.vibe.fundsmith.model.Position;
import com.vibe.fundsmith.repository.CashLedgerRepository;
import com.vibe.fundsmith.repository.NavCalculationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Basic unit test for NavCalculationService verifying:
 * - positions + cash + fee accrual calculation
 * - snapshot is persisted via repository
 */
class NavCalculationServiceTest {

    @Mock
    private NavCalculationRepository navCalculationRepository;

    @Mock
    private PositionService positionService;

    @Mock
    private CashLedgerRepository cashLedgerRepository;

    @Mock
    private DemoConfig demoConfig;

    @InjectMocks
    private NavCalculationService navCalculationService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        // default shares outstanding and fee rate injected via constructor in actual app,
        // recreate service with defaults for test
        navCalculationService = new NavCalculationService(
                navCalculationRepository,
                positionService,
                cashLedgerRepository,
                demoConfig,
                1_000_000L,
                new BigDecimal("0.005")
        );
    }

    @Test
    void testCalculateNav_WithPositionsAndCash_PersistsSnapshotAndCalculatesFee() {
        String portfolioId = "DEFAULT";

        // Create a mocked position with ISIN and quantity
        Position pos = mock(Position.class);
        when(pos.getIsin()).thenReturn("US0378331005");
        when(pos.getQuantity()).thenReturn(new BigDecimal("100")); // 100 shares

        when(positionService.getPositions()).thenReturn(List.of(pos));

        // Demo price for the ISIN
        when(demoConfig.getBasePrices()).thenReturn(Map.of("US0378331005", new BigDecimal("175.50")));

        // Cash balance from ledger
        when(cashLedgerRepository.getCurrentBalance(portfolioId)).thenReturn(new BigDecimal("1000.00"));

        // Capture saved entity
        ArgumentCaptor<NavCalculation> captor = ArgumentCaptor.forClass(NavCalculation.class);
        when(navCalculationRepository.save(captor.capture())).thenAnswer(invocation -> invocation.getArgument(0));

        NavCalculation saved = navCalculationService.calculateNav(portfolioId);

        // Verify save was called once
        verify(navCalculationRepository, times(1)).save(any());

        // Calculate expected values:
        // positions value = 100 * 175.50 = 17,550.00
        BigDecimal expectedPositions = new BigDecimal("17550.00");
        BigDecimal expectedGross = expectedPositions.add(new BigDecimal("1000.00")); // 18,550.00
        // daily fee = gross * 0.005 / 365
        BigDecimal expectedDailyFee = expectedGross.multiply(new BigDecimal("0.005"))
                .divide(new BigDecimal("365"), 4, BigDecimal.ROUND_HALF_UP);
        BigDecimal expectedNet = expectedGross.subtract(expectedDailyFee);
        BigDecimal expectedNavPerShare = expectedNet.divide(new BigDecimal("1000000"), 4, BigDecimal.ROUND_HALF_UP);

        NavCalculation captured = captor.getValue();

        assertNotNull(saved.getId());
        assertEquals(expectedGross.setScale(4), captured.getTotalAssets().setScale(4));
        assertEquals(expectedDailyFee.setScale(4), captured.getTotalLiabilities().setScale(4));
        assertEquals(expectedNet.setScale(4), captured.getNetAssetValue().setScale(4));
        assertEquals(expectedNavPerShare.setScale(4), captured.getNavPerShare().setScale(4));
    }
}