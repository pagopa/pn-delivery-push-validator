package it.pagopa.pn.deliverypushvalidator.config;

import it.pagopa.pn.commons.abstractions.ParameterConsumer;
import it.pagopa.pn.commons.exceptions.PnInternalException;
import it.pagopa.pn.deliverypushvalidator.dto.campaign.*;
import it.pagopa.pn.deliverypushvalidator.dto.ext.datavault.RecipientTypeInt;
import it.pagopa.pn.deliverypushvalidator.exception.PnCampaignNotFoundException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@ExtendWith(MockitoExtension.class)
class MVPCampaignsParameterConsumerTest {

    private static final String SENDER_A = "5b994d4a-0fa8-47ac-9c7b-354f1d44a1ce";
    private static final String SENDER_B = "138f5f86-954b-4c65-a556-85b7f5f3958a";

    private ParameterConsumer parameterConsumer;
    private MVPCampaignsParameterConsumer campaignsParameterConsumer;

    @BeforeEach
    void setup() {
        parameterConsumer = Mockito.mock(ParameterConsumer.class);
        campaignsParameterConsumer = new MVPCampaignsParameterConsumer(parameterConsumer);
    }



    @Test
    void initialize_unexpectedInternalExceptionIsPropagated() {
        PnInternalException exception = new PnInternalException("boom", "GENERIC_ERROR");

        Mockito.when(parameterConsumer.getParameterValue(Mockito.anyString(), Mockito.eq(Campaign[].class)))
                .thenThrow(exception);

        Assertions.assertThrows(PnInternalException.class, () -> campaignsParameterConsumer.initialize());
    }

    @Test
    void getCampaignByCampaignIdAndSenderId_success() {
        Campaign campaign = validCampaign("c1", SENDER_A, CampaignStatus.IN_PROGRESS);

        Mockito.when(parameterConsumer.getParameterValue(Mockito.anyString(), Mockito.eq(Campaign[].class)))
                .thenReturn(Optional.of(new Campaign[]{campaign}));
        campaignsParameterConsumer.initialize();

        Campaign result = campaignsParameterConsumer.getCampaignByCampaignIdAndSenderId("c1", SENDER_A);

        Assertions.assertNotNull(result);
        Assertions.assertEquals("c1", result.getCampaignId());
        Assertions.assertEquals(SENDER_A, result.getSenderId());
    }

    @Test
    void getCampaignByCampaignIdAndSenderId_notFoundByCampaignId() {
        Campaign[] campaigns = new Campaign[]{
                validCampaign("c1", SENDER_A, CampaignStatus.IN_PROGRESS)
        };

        Mockito.when(parameterConsumer.getParameterValue(Mockito.anyString(), Mockito.eq(Campaign[].class)))
                .thenReturn(Optional.of(campaigns));
        campaignsParameterConsumer.initialize();

        Assertions.assertThrows(PnCampaignNotFoundException.class,
                () -> campaignsParameterConsumer.getCampaignByCampaignIdAndSenderId("missing", SENDER_A));
    }

    @Test
    void getCampaignByCampaignIdAndSenderId_notFoundBySenderId() {
        Campaign[] campaigns = new Campaign[]{
                validCampaign("c1", SENDER_B, CampaignStatus.IN_PROGRESS)
        };

        Mockito.when(parameterConsumer.getParameterValue(Mockito.anyString(), Mockito.eq(Campaign[].class)))
                .thenReturn(Optional.of(campaigns));
        campaignsParameterConsumer.initialize();

        Assertions.assertThrows(PnCampaignNotFoundException.class,
                () -> campaignsParameterConsumer.getCampaignByCampaignIdAndSenderId("c1", SENDER_A));
    }

    @Test
    void getCampaignByCampaignIdAndSenderId_parameterNotFound() {
        Mockito.when(parameterConsumer.getParameterValue(Mockito.anyString(), Mockito.eq(Campaign[].class)))
                .thenReturn(Optional.empty());
        campaignsParameterConsumer.initialize();

        Assertions.assertThrows(PnCampaignNotFoundException.class,
                () -> campaignsParameterConsumer.getCampaignByCampaignIdAndSenderId("c1", SENDER_A));
    }

    @Test
    void getCampaignByCampaignIdAndSenderId_multipleCampaigns() {
        Campaign[] campaigns = new Campaign[]{
                validCampaign("c1", SENDER_A, CampaignStatus.IN_PROGRESS),
                validCampaign("c2", SENDER_A, CampaignStatus.IN_PROGRESS),
                validCampaign("c3", SENDER_A, CampaignStatus.IN_PROGRESS)
        };

        Mockito.when(parameterConsumer.getParameterValue(Mockito.anyString(), Mockito.eq(Campaign[].class)))
                .thenReturn(Optional.of(campaigns));
        campaignsParameterConsumer.initialize();

        Campaign result = campaignsParameterConsumer.getCampaignByCampaignIdAndSenderId("c2", SENDER_A);

        Assertions.assertNotNull(result);
        Assertions.assertEquals("c2", result.getCampaignId());
    }

    @Test
    void initialize_invalidCampaignStatus() {
        Mockito.when(parameterConsumer.getParameterValue(Mockito.anyString(), Mockito.eq(Campaign[].class)))
                .thenReturn(Optional.of(new Campaign[]{validCampaign("c1", SENDER_A, null)}));

        campaignsParameterConsumer.initialize();

        Assertions.assertThrows(PnCampaignNotFoundException.class,
                () -> campaignsParameterConsumer.getCampaignByCampaignIdAndSenderId("c1", SENDER_A));
    }

    private Campaign validCampaign(String campaignId, String senderId, CampaignStatus status) {
        return Campaign.builder()
                .campaignId(campaignId)
                .senderId(senderId)
                .title("Campaign " + campaignId)
                .descriptionScope("Description " + campaignId)
                .status(status)
                .startDate(Instant.now())
                .endDate(Instant.now())
                .serviceId("service-" + campaignId)
                .sensitiveContent(false)
                .stopOnViewed(false)
                .workflow(List.of(validWorkflowStep()))
                .build();
    }

    private WorkflowEntity validWorkflowStep() {
        return WorkflowEntity.builder()
                .channel(Channel.IO)
                .recipientType(Collections.singleton(RecipientTypeInt.PF))
                .timeout(Duration.ofDays(1))
                .desiredFeedback(Set.of(DesiredFeedback.READ))
                .includeAttachment(false)
                .build();
    }
}
