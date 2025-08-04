package it.pagopa.pn.deliverypushvalidator.utils;

import it.pagopa.pn.commons.exceptions.PnInternalException;
import it.pagopa.pn.deliverypushvalidator.config.PnDeliveryPushValidatorConfigs;
import it.pagopa.pn.deliverypushvalidator.dto.cost.RefusalCostMode;
import it.pagopa.pn.deliverypushvalidator.service.NotificationProcessCostService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PnTechnicalRefusalCostModeTest {
    private PnDeliveryPushValidatorConfigs configs;
    private NotificationProcessCostService costService;

    @BeforeEach
    public void setup() {
        configs = mock(PnDeliveryPushValidatorConfigs.class);
        costService = mock(NotificationProcessCostService.class);
    }

    @Test
    void testValidConfiguration() {
        when(configs.getTechnicalRefusalCostMode()).thenReturn("RECIPIENT_BASED;50");

        PnTechnicalRefusalCostMode result = new PnTechnicalRefusalCostMode(configs, costService);

        assertEquals(RefusalCostMode.RECIPIENT_BASED, result.getMode());
        assertEquals(50, result.getCost());
    }

    @Test
    void testInvalidConfiguration() {
        when(configs.getTechnicalRefusalCostMode()).thenReturn("INVALID_CONFIG");

        PnInternalException exception = assertThrows(PnInternalException.class, () ->
                new PnTechnicalRefusalCostMode(configs, costService)
        );
        assertEquals("Invalid configuration for PN_DELIVERYPUSH_TECHNICAL_REFUSAL_COST_MODE", exception.getProblem().getDetail());
    }

    @Test
    void testMissingConfiguration() {
        when(configs.getTechnicalRefusalCostMode()).thenReturn(null);
        when(costService.getSendFee()).thenReturn(100);

        PnTechnicalRefusalCostMode result = new PnTechnicalRefusalCostMode(configs, costService);

        assertEquals(RefusalCostMode.RECIPIENT_BASED, result.getMode());
        assertEquals(100, result.getCost());
    }

    @Test
    void testConfigurationWithMissingCost() {
        when(configs.getTechnicalRefusalCostMode()).thenReturn("RECIPIENT_BASED;");

        assertThrows(PnInternalException.class, () ->
                new PnTechnicalRefusalCostMode(configs, costService)
        );
    }
}
