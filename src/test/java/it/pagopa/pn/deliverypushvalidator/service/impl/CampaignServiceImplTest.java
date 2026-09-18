package it.pagopa.pn.deliverypushvalidator.service.impl;

import it.pagopa.pn.commons.db.campaign.CampaignServiceCachedProvider;
import it.pagopa.pn.commons.db.campaign.entity.CampaignChannel;
import it.pagopa.pn.commons.db.campaign.entity.CampaignEntity;
import it.pagopa.pn.commons.db.campaign.entity.CampaignStatus;
import it.pagopa.pn.commons.db.campaign.entity.DesiredFeedback;
import it.pagopa.pn.commons.db.campaign.entity.WorkflowEntity;
import it.pagopa.pn.deliverypushvalidator.dto.campaign.Campaign;
import it.pagopa.pn.deliverypushvalidator.dto.campaign.Channel;
import it.pagopa.pn.deliverypushvalidator.exception.PnCampaignNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CampaignServiceImplTest {

    private static final String CAMPAIGN_ID = "campaign-id";
    private static final String SENDER_ID = "sender-id";

    @Mock
    private CampaignServiceCachedProvider campaignServiceCachedProvider;

    @InjectMocks
    private CampaignServiceImpl campaignService;

    @Test
    void getCampaignByCampaignIdAndSenderIdReturnsMappedCampaign() {
        CampaignEntity campaignEntity = buildCampaignEntity();
        when(campaignServiceCachedProvider.getByCampaignIdAndSenderId(CAMPAIGN_ID, SENDER_ID))
                .thenReturn(campaignEntity);

        Campaign result = campaignService.getCampaignByCampaignIdAndSenderId(CAMPAIGN_ID, SENDER_ID);

        assertEquals(CAMPAIGN_ID, result.getCampaignId());
        assertEquals(SENDER_ID, result.getSenderId());
        assertEquals("Campaign title", result.getTitle());
        assertEquals(it.pagopa.pn.deliverypushvalidator.dto.campaign.CampaignStatus.IN_PROGRESS, result.getStatus());
        assertEquals(Channel.IO, result.getWorkflow().getFirst().getChannel());
        assertEquals(it.pagopa.pn.deliverypushvalidator.dto.campaign.DesiredFeedback.READ,
                result.getWorkflow().getFirst().getDesiredFeedback().iterator().next());
        verify(campaignServiceCachedProvider).getByCampaignIdAndSenderId(CAMPAIGN_ID, SENDER_ID);
    }

    @Test
    void getCampaignByCampaignIdAndSenderIdPropagatesNotFoundException() {
        when(campaignServiceCachedProvider.getByCampaignIdAndSenderId(CAMPAIGN_ID, SENDER_ID))
                .thenThrow(new PnCampaignNotFoundException(
                        "Campaign with campaignId=campaign-id and senderId=sender-id not found",
                        "Campaign with campaignId=campaign-id and senderId=sender-id not found"));

        PnCampaignNotFoundException exception = assertThrows(PnCampaignNotFoundException.class,
                () -> campaignService.getCampaignByCampaignIdAndSenderId(CAMPAIGN_ID, SENDER_ID));

        assertEquals("Campaign with campaignId=campaign-id and senderId=sender-id not found", exception.getMessage());
        verify(campaignServiceCachedProvider).getByCampaignIdAndSenderId(CAMPAIGN_ID, SENDER_ID);
    }

    private CampaignEntity buildCampaignEntity() {
        WorkflowEntity workflowEntity = WorkflowEntity.builder()
                .channel(CampaignChannel.IO)
                .recipientType(Set.of(it.pagopa.pn.commons.utils.qr.models.RecipientTypeInt.PF))
                .timeout(Duration.ofHours(2))
                .desiredFeedback(Set.of(DesiredFeedback.READ))
                .includeAttachment(Boolean.TRUE)
                .build();

        return CampaignEntity.builder()
                .campaignId(CAMPAIGN_ID)
                .senderId(SENDER_ID)
                .title("Campaign title")
                .descriptionScope("Description")
                .startDate(Instant.parse("2026-01-01T00:00:00Z"))
                .endDate(Instant.parse("2026-12-31T23:59:59Z"))
                .status(CampaignStatus.IN_PROGRESS)
                .senderContact("contact@example.it")
                .serviceId("service-1")
                .serviceName("Service")
                .taxonomyCode("taxonomy")
                .sensitiveContent(Boolean.FALSE)
                .stopOnViewed(Boolean.TRUE)
                .workflow(List.of(workflowEntity))
                .build();
    }
}
