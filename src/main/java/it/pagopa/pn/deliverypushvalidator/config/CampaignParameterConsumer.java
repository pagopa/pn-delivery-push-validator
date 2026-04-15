package it.pagopa.pn.deliverypushvalidator.config;

import it.pagopa.pn.commons.abstractions.ParameterConsumer;
import it.pagopa.pn.deliverypushvalidator.validation.CampaignData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Slf4j
@Component
public class CampaignParameterConsumer {

    private final ParameterConsumer parameterConsumer;

    private static final String PARAMETER_STORE_CAMPAIGN_PREFIX = "Campaign_";

    public CampaignParameterConsumer(ParameterConsumer parameterConsumer) {
        this.parameterConsumer = parameterConsumer;
    }

    /**
     * Retrieves campaign data from Parameter Store by campaignId.
     *
     * @param campaignId the campaign identifier
     * @return Optional containing campaign data if found
     */
    public Optional<CampaignData> getCampaign(String campaignId) {
        log.debug("Start getCampaign for campaignId={}", campaignId);
        String parameterName = PARAMETER_STORE_CAMPAIGN_PREFIX + campaignId;
        return parameterConsumer.getParameterValue(parameterName, CampaignData.class);
    }
}

