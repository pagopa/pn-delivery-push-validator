package it.pagopa.pn.deliverypushvalidator.service.mapper;

import it.pagopa.pn.commons.db.campaign.entity.CampaignChannel;
import it.pagopa.pn.commons.db.campaign.entity.CampaignEntity;
import it.pagopa.pn.commons.db.campaign.entity.CampaignStatus;
import it.pagopa.pn.commons.db.campaign.entity.DesiredFeedback;
import it.pagopa.pn.commons.db.campaign.entity.WorkflowEntity;
import it.pagopa.pn.deliverypushvalidator.dto.campaign.Campaign;
import it.pagopa.pn.deliverypushvalidator.dto.campaign.Channel;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class CampaignMapperTest {

    @Test
    void shouldMapCampaignEntityToInternalCampaignAndBack() {
        CampaignEntity campaignEntity = CampaignEntity.builder()
                .campaignId("camp-1")
                .senderId("550e8400-e29b-41d4-a716-446655440001")
                .title("Title")
                .descriptionScope("Description")
                .startDate(Instant.parse("2026-01-01T00:00:00Z"))
                .endDate(Instant.parse("2026-12-31T23:59:59Z"))
                .status(CampaignStatus.IN_PROGRESS)
                .senderContact("contact@example.it")
                .serviceId("service-1")
                .serviceName("Service")
                .taxonomyCode("taxonomy")
                .sensitiveContent(Boolean.TRUE)
                .stopOnViewed(Boolean.FALSE)
                .workflow(List.of(WorkflowEntity.builder()
                        .channel(CampaignChannel.IO)
                        .recipientType(Set.of(it.pagopa.pn.commons.utils.qr.models.RecipientTypeInt.PF))
                        .timeout(Duration.ofHours(1))
                        .desiredFeedback(Set.of(DesiredFeedback.READ))
                        .includeAttachment(Boolean.TRUE)
                        .build()))
                .build();

        Campaign internalCampaign = CampaignMapper.toInternalCampaign(campaignEntity);
        assertNotNull(internalCampaign);
        assertEquals("camp-1", internalCampaign.getCampaignId());
        assertEquals(Channel.IO, internalCampaign.getWorkflow().getFirst().getChannel());

        CampaignEntity roundTrip = CampaignMapper.toCampaignEntity(internalCampaign);
        assertEquals(campaignEntity, roundTrip);
    }
}
