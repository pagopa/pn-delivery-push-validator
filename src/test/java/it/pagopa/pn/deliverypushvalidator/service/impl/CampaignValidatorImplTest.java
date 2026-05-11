package it.pagopa.pn.deliverypushvalidator.service.impl;

import it.pagopa.pn.deliverypushvalidator.dto.campaign.Campaign;
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
    void validateAndGetCampaign_shouldReturnCampaign_whenCampaignExistsAndIsOpen() {
        NotificationInt notification = buildNotification("camp1", "sender1");
        Campaign campaign = mock(Campaign.class);
        when(campaign.isClosed()).thenReturn(false);
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
        assertEquals("No campaign with id camp2 for sender sender2", ex.getProblem().getDetail());
    }

    @Test
    void validateAndGetCampaign_shouldThrowException_whenCampaignIsClosed() {
        NotificationInt notification = buildNotification("camp3", "sender3");
        Campaign campaign = mock(Campaign.class);
        when(campaign.isClosed()).thenReturn(true);
        when(campaignService.getCampaignByCampaignIdAndSenderId("camp3", "sender3")).thenReturn(campaign);

        PnCampaignValidationException ex = assertThrows(
                PnCampaignValidationException.class,
                () -> campaignValidator.validateAndGetCampaign(notification)
        );
        assertEquals("Campaign camp3 is closed", ex.getProblem().getDetail());
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