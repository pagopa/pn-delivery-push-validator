package it.pagopa.pn.deliverypushvalidator.validation;

import it.pagopa.pn.deliverypushvalidator.config.CampaignParameterConsumer;
import it.pagopa.pn.deliverypushvalidator.dto.ext.delivery.notification.NotificationInt;
import it.pagopa.pn.deliverypushvalidator.dto.ext.delivery.notification.NotificationSenderInt;
import it.pagopa.pn.deliverypushvalidator.exception.PnValidationCampaignException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class CampaignValidatorTest {

    @Mock
    private CampaignParameterConsumer campaignParameterConsumer;

    private CampaignValidator campaignValidator;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        campaignValidator = new CampaignValidator(campaignParameterConsumer);
    }

    private NotificationInt buildNotification(String campaignId) {
        return NotificationInt.builder()
                .iun("IUN-TEST-001")
                .campaignId(campaignId)
                .sender(NotificationSenderInt.builder().paId("PA-001").paTaxId("TAXID").build())
                .build();
    }

    private CampaignData buildCampaign(Boolean closed, Instant start, Instant end) {
        return CampaignData.builder()
                .campaignId("CAMP-001")
                .senderId("PA-001")
                .title("Test Campaign")
                .closed(closed)
                .startDate(start)
                .endDate(end)
                .build();
    }

    @Test
    void validateAndGetCampaign_success() {
        NotificationInt notification = buildNotification("CAMP-001");
        CampaignData campaign = buildCampaign(false,
                Instant.now().minus(1, ChronoUnit.DAYS),
                Instant.now().plus(30, ChronoUnit.DAYS));
        Mockito.when(campaignParameterConsumer.getCampaign("CAMP-001")).thenReturn(Optional.of(campaign));

        CampaignData result = campaignValidator.validateAndGetCampaign(notification);
        assertNotNull(result);
        assertEquals("CAMP-001", result.getCampaignId());
    }

    @Test
    void validateAndGetCampaign_missingCampaignId() {
        NotificationInt notification = buildNotification(null);
        assertThrows(PnValidationCampaignException.class,
                () -> campaignValidator.validateAndGetCampaign(notification));
    }

    @Test
    void validateAndGetCampaign_campaignNotFound() {
        NotificationInt notification = buildNotification("CAMP-MISSING");
        Mockito.when(campaignParameterConsumer.getCampaign("CAMP-MISSING")).thenReturn(Optional.empty());
        assertThrows(PnValidationCampaignException.class,
                () -> campaignValidator.validateAndGetCampaign(notification));
    }

    @Test
    void validateAndGetCampaign_campaignClosed() {
        NotificationInt notification = buildNotification("CAMP-001");
        CampaignData campaign = buildCampaign(true,
                Instant.now().minus(1, ChronoUnit.DAYS),
                Instant.now().plus(30, ChronoUnit.DAYS));
        Mockito.when(campaignParameterConsumer.getCampaign("CAMP-001")).thenReturn(Optional.of(campaign));
        assertThrows(PnValidationCampaignException.class,
                () -> campaignValidator.validateAndGetCampaign(notification));
    }

    @Test
    void validateAndGetCampaign_campaignNotStarted() {
        NotificationInt notification = buildNotification("CAMP-001");
        CampaignData campaign = buildCampaign(false,
                Instant.now().plus(10, ChronoUnit.DAYS),
                Instant.now().plus(30, ChronoUnit.DAYS));
        Mockito.when(campaignParameterConsumer.getCampaign("CAMP-001")).thenReturn(Optional.of(campaign));
        assertThrows(PnValidationCampaignException.class,
                () -> campaignValidator.validateAndGetCampaign(notification));
    }

    @Test
    void validateAndGetCampaign_campaignExpired() {
        NotificationInt notification = buildNotification("CAMP-001");
        CampaignData campaign = buildCampaign(false,
                Instant.now().minus(30, ChronoUnit.DAYS),
                Instant.now().minus(1, ChronoUnit.DAYS));
        Mockito.when(campaignParameterConsumer.getCampaign("CAMP-001")).thenReturn(Optional.of(campaign));
        assertThrows(PnValidationCampaignException.class,
                () -> campaignValidator.validateAndGetCampaign(notification));
    }
}
