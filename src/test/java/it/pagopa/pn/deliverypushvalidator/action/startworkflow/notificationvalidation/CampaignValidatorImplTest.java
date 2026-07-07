package it.pagopa.pn.deliverypushvalidator.action.startworkflow.notificationvalidation;

import it.pagopa.pn.deliverypushvalidator.dto.campaign.Campaign;
import it.pagopa.pn.deliverypushvalidator.dto.campaign.CampaignStatus;
import it.pagopa.pn.deliverypushvalidator.dto.ext.delivery.notification.NotificationInt;
import it.pagopa.pn.deliverypushvalidator.dto.ext.delivery.notification.NotificationSenderInt;
import it.pagopa.pn.deliverypushvalidator.exception.PnCampaignNotFoundException;
import it.pagopa.pn.deliverypushvalidator.exception.PnCampaignValidationException;
import it.pagopa.pn.deliverypushvalidator.service.CampaignService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CampaignValidatorImplTest {

    private CampaignService campaignService;
    private CampaignValidatorImpl campaignValidator;

    @BeforeEach
    void setUp() {
        campaignService = mock(CampaignService.class);
        campaignValidator = new CampaignValidatorImpl(campaignService);
    }

    @Test
    void validateAndGetCampaign_shouldReturnCampaign_whenCampaignExistsAndIsInProgress() {
        NotificationInt notification = buildNotification("camp1", "sender1");
        Campaign campaign = mock(Campaign.class);
        when(campaign.getStatus()).thenReturn(CampaignStatus.IN_PROGRESS);
        when(campaignService.getCampaignByCampaignIdAndSenderId("camp1", "sender1")).thenReturn(campaign);

        Campaign result = campaignValidator.validateAndGetCampaign(notification);

        assertEquals(campaign, result);
    }

    @Test
    void validateAndGetCampaign_shouldThrowException_whenCampaignNotFound() {
        NotificationInt notification = buildNotification("camp2", "sender2");
        when(campaignService.getCampaignByCampaignIdAndSenderId("camp2", "sender2"))
                .thenThrow(new PnCampaignNotFoundException("not found", ""));

        PnCampaignValidationException ex = assertThrows(
                PnCampaignValidationException.class,
                () -> campaignValidator.validateAndGetCampaign(notification)
        );
        assertEquals("Campaign with id camp2 not found", ex.getProblem().getDetail());
    }

    @Test
    void validateAndGetCampaign_shouldThrowException_whenCampaignIsNotInProgress() {
        NotificationInt notification = buildNotification("camp3", "sender3");
        Campaign campaign = mock(Campaign.class);
        when(campaign.getStatus()).thenReturn(CampaignStatus.CANCELED);
        when(campaignService.getCampaignByCampaignIdAndSenderId("camp3", "sender3")).thenReturn(campaign);

        PnCampaignValidationException ex = assertThrows(
                PnCampaignValidationException.class,
                () -> campaignValidator.validateAndGetCampaign(notification)
        );
        assertEquals("Campaign camp3 has CANCELED status", ex.getProblem().getDetail());
    }

    private NotificationInt buildNotification(String campaignId, String senderId) {
        NotificationInt notification = mock(NotificationInt.class);
        when(notification.getCampaignId()).thenReturn(campaignId);
        NotificationSenderInt sender = mock(NotificationSenderInt.class);
        when(sender.getPaId()).thenReturn(senderId);
        when(notification.getSender()).thenReturn(sender);
        return notification;
    }
}