package it.pagopa.pn.deliverypushvalidator.config;

import it.pagopa.pn.commons.abstractions.ParameterConsumer;
import it.pagopa.pn.commons.exceptions.PnInternalException;
import it.pagopa.pn.deliverypushvalidator.dto.campaign.Campaign;
import it.pagopa.pn.deliverypushvalidator.exception.PnCampaignNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import software.amazon.awssdk.services.ssm.model.ParameterNotFoundException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MVPCampaignsParameterConsumerTest {

    private static final String CAMPAIGN_ID = "campaign-id";
    private static final String SENDER_ID = "sender-id";

    private ParameterConsumer parameterConsumer;
    private MVPCampaignsParameterConsumer mvpCampaignsParameterConsumer;

    @BeforeEach
    void setup() {
        parameterConsumer = Mockito.mock(ParameterConsumer.class);
        mvpCampaignsParameterConsumer = new MVPCampaignsParameterConsumer(parameterConsumer);
    }

    // -------------------------------------------------------------------------
    // getCampaignsBySenderId
    // -------------------------------------------------------------------------

    @Test
    void getCampaignsBySenderId_filtersCampaigns() {
        Campaign[] campaigns = new Campaign[]{
                buildCampaign("c1", "sender-a"),
                buildCampaign("c2", "sender-b"),
                buildCampaign("c3", "sender-a")
        };
        when(parameterConsumer.getParameterValue(anyString(), eq(Campaign[].class)))
                .thenReturn(Optional.of(campaigns));

        mvpCampaignsParameterConsumer.initialize();
        List<Campaign> result = mvpCampaignsParameterConsumer.getCampaignsBySenderId("sender-a");

        assertEquals(2, result.size());
        assertEquals("c1", result.get(0).getCampaignId());
        assertEquals("c3", result.get(1).getCampaignId());
    }

    @Test
    void getCampaignsBySenderId_noResults() {
        when(parameterConsumer.getParameterValue(anyString(), eq(Campaign[].class)))
                .thenReturn(Optional.of(new Campaign[]{buildCampaign("c1", "sender-b")}));

        mvpCampaignsParameterConsumer.initialize();
        List<Campaign> result = mvpCampaignsParameterConsumer.getCampaignsBySenderId("sender-a");

        assertTrue(result.isEmpty());
    }

    @Test
    void getCampaignsBySenderId_parameterNotFound() {
        when(parameterConsumer.getParameterValue(anyString(), eq(Campaign[].class)))
                .thenReturn(Optional.empty());

        mvpCampaignsParameterConsumer.initialize();
        List<Campaign> result = mvpCampaignsParameterConsumer.getCampaignsBySenderId("sender-a");

        assertTrue(result.isEmpty());
    }

    // -------------------------------------------------------------------------
    // getCampaignByCampaignIdAndSenderId
    // -------------------------------------------------------------------------

    @Test
    void getCampaignByCampaignIdAndSenderId_success() {
        Campaign expected = buildCampaign(CAMPAIGN_ID, SENDER_ID);
        Campaign other = buildCampaign("other-campaign", "other-sender");
        when(parameterConsumer.getParameterValue(anyString(), eq(Campaign[].class)))
                .thenReturn(Optional.of(new Campaign[]{other, expected}));

        mvpCampaignsParameterConsumer.initialize();
        Campaign result = mvpCampaignsParameterConsumer.getCampaignByCampaignIdAndSenderId(CAMPAIGN_ID, SENDER_ID);

        assertSame(expected, result);
    }

    @Test
    void getCampaignByCampaignIdAndSenderId_notFoundByCampaignId() {
        when(parameterConsumer.getParameterValue(anyString(), eq(Campaign[].class)))
                .thenReturn(Optional.of(new Campaign[]{buildCampaign("c1", SENDER_ID)}));

        mvpCampaignsParameterConsumer.initialize();

        PnCampaignNotFoundException ex = assertThrows(PnCampaignNotFoundException.class,
                () -> mvpCampaignsParameterConsumer.getCampaignByCampaignIdAndSenderId("missing", SENDER_ID));
        assertEquals(
                "Campaign not found",
                ex.getMessage()
        );
    }

    @Test
    void getCampaignByCampaignIdAndSenderId_notFoundBySenderId() {
        when(parameterConsumer.getParameterValue(anyString(), eq(Campaign[].class)))
                .thenReturn(Optional.of(new Campaign[]{buildCampaign(CAMPAIGN_ID, "sender-b")}));

        mvpCampaignsParameterConsumer.initialize();

        PnCampaignNotFoundException ex = assertThrows(PnCampaignNotFoundException.class,
                () -> mvpCampaignsParameterConsumer.getCampaignByCampaignIdAndSenderId(CAMPAIGN_ID, SENDER_ID));
        assertEquals(
                "Campaign not found",
                ex.getMessage()
        );
    }

    @Test
    void getCampaignByCampaignIdAndSenderId_parameterNotFound() {
        when(parameterConsumer.getParameterValue(anyString(), eq(Campaign[].class)))
                .thenReturn(Optional.empty());

        mvpCampaignsParameterConsumer.initialize();

        PnCampaignNotFoundException ex = assertThrows(PnCampaignNotFoundException.class,
                () -> mvpCampaignsParameterConsumer.getCampaignByCampaignIdAndSenderId(CAMPAIGN_ID, SENDER_ID));
        assertEquals(
                "Campaign not found",
                ex.getMessage()
        );
    }

    @Test
    void getCampaignByCampaignIdAndSenderId_multipleCampaigns() {
        Campaign[] campaigns = new Campaign[]{
                buildCampaign("c1", SENDER_ID),
                buildCampaign("c2", SENDER_ID),
                buildCampaign("c3", SENDER_ID)
        };
        when(parameterConsumer.getParameterValue(anyString(), eq(Campaign[].class)))
                .thenReturn(Optional.of(campaigns));

        mvpCampaignsParameterConsumer.initialize();
        Campaign result = mvpCampaignsParameterConsumer.getCampaignByCampaignIdAndSenderId("c2", SENDER_ID);

        assertNotNull(result);
        assertEquals("c2", result.getCampaignId());
    }

    // -------------------------------------------------------------------------
    // initialize — comportamenti speciali
    // -------------------------------------------------------------------------

    @Test
    void initialize_parameterNotFoundExceptionDoesNotBreakStartup() {
        PnInternalException exception = new PnInternalException(
                "Internal Server Error",
                "GENERIC_ERROR",
                ParameterNotFoundException.builder().message("Parameter MVPCampaigns not found.").build()
        );
        when(parameterConsumer.getParameterValue(anyString(), eq(Campaign[].class)))
                .thenThrow(exception);

        assertDoesNotThrow(() -> mvpCampaignsParameterConsumer.initialize());
        assertTrue(mvpCampaignsParameterConsumer.getCampaignsBySenderId(SENDER_ID).isEmpty());
    }

    @Test
    void initialize_unexpectedInternalExceptionIsPropagated() {
        PnInternalException exception = new PnInternalException("boom", "GENERIC_ERROR");
        when(parameterConsumer.getParameterValue(anyString(), eq(Campaign[].class)))
                .thenThrow(exception);

        assertThrows(PnInternalException.class, () -> mvpCampaignsParameterConsumer.initialize());
    }

    @Test
    void initialize_skipsInvalidCampaigns() {
        Campaign[] campaigns = new Campaign[]{
                buildCampaign("c1", "sender-a"),
                buildCampaign(null, "sender-a"),
                buildCampaign("c3", null),
                null
        };
        when(parameterConsumer.getParameterValue(anyString(), eq(Campaign[].class)))
                .thenReturn(Optional.of(campaigns));

        mvpCampaignsParameterConsumer.initialize();
        List<Campaign> result = mvpCampaignsParameterConsumer.getCampaignsBySenderId("sender-a");

        assertEquals(1, result.size());
        assertEquals("c1", result.getFirst().getCampaignId());
    }

    // -------------------------------------------------------------------------
    // cache — il parameter store viene letto una sola volta
    // -------------------------------------------------------------------------

    @Test
    void parameterStoreIsReadOnlyAtInitialization() {
        when(parameterConsumer.getParameterValue(anyString(), eq(Campaign[].class)))
                .thenReturn(Optional.of(new Campaign[]{buildCampaign(CAMPAIGN_ID, SENDER_ID)}));

        mvpCampaignsParameterConsumer.initialize();

        mvpCampaignsParameterConsumer.getCampaignsBySenderId(SENDER_ID);
        mvpCampaignsParameterConsumer.getCampaignsBySenderId("other-sender");
        mvpCampaignsParameterConsumer.getCampaignByCampaignIdAndSenderId(CAMPAIGN_ID, SENDER_ID);

        verify(parameterConsumer, times(1))
                .getParameterValue(anyString(), eq(Campaign[].class));
    }

    // -------------------------------------------------------------------------
    // helper
    // -------------------------------------------------------------------------

    private Campaign buildCampaign(String campaignId, String senderId) {
        Campaign campaign = new Campaign();
        campaign.setCampaignId(campaignId);
        campaign.setSenderId(senderId);
        return campaign;
    }
}