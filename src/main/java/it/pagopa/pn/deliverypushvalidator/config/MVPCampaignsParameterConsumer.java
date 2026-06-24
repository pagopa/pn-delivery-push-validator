package it.pagopa.pn.deliverypushvalidator.config;

import it.pagopa.pn.commons.abstractions.ParameterConsumer;
import it.pagopa.pn.commons.exceptions.PnInternalException;
import it.pagopa.pn.deliverypushvalidator.dto.campaign.Campaign;
import it.pagopa.pn.deliverypushvalidator.exception.PnCampaignNotFoundException;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import software.amazon.awssdk.services.ssm.model.ParameterNotFoundException;


import java.util.*;

@Slf4j
@Configuration
public class MVPCampaignsParameterConsumer {

    private static final String PARAMETER_STORE_MVP_CAMPAIGNS = "MVPCampaigns";

    private final ParameterConsumer parameterConsumer;
    private List<Campaign> campaigns = Collections.emptyList();

    public MVPCampaignsParameterConsumer(ParameterConsumer parameterConsumer) {
        this.parameterConsumer = parameterConsumer;
    }

    @PostConstruct
    protected void initialize() {
        Optional<Campaign[]> maybeCampaigns = loadCampaigns();
        if (maybeCampaigns.isEmpty()) {
            log.info("No campaign configuration found on parameter store");
            return;
        }
        List<Campaign> loaded = new ArrayList<>();
        for (Campaign campaign : maybeCampaigns.get()) {
            if (isValid(campaign)) {
                loaded.add(campaign);
            } else {
                log.warn("Invalid campaign configuration found: {}", campaign);
            }
        }
        campaigns = Collections.unmodifiableList(loaded);
        log.info("Loaded {} campaigns in memory", campaigns.size());
    }

    private Optional<Campaign[]> loadCampaigns() {
        try {
            return parameterConsumer.getParameterValue(
                    PARAMETER_STORE_MVP_CAMPAIGNS,
                    Campaign[].class
            );
        } catch (PnInternalException ex) {
            if (hasParameterNotFoundCause(ex)) {
                log.info("Campaign configuration parameter {} not found on parameter store",
                        PARAMETER_STORE_MVP_CAMPAIGNS);
                return Optional.empty();
            }
            throw ex;
        }
    }

    public List<Campaign> getCampaignsBySenderId(String senderId) {
        return campaigns.stream()
                .filter(campaign -> Objects.equals(senderId, campaign.getSenderId()))
                .toList();
    }

    public Campaign getCampaignByCampaignIdAndSenderId(String campaignId, String senderId) {
        log.debug("Start getCampaignByCampaignIdAndSenderId for campaignId={} and senderId={}",
                campaignId, senderId);
        return campaigns.stream()
                .filter(c -> Objects.equals(campaignId, c.getCampaignId())
                        && Objects.equals(senderId, c.getSenderId()))
                .findFirst()
                .orElseThrow(() -> new PnCampaignNotFoundException(
                        String.format("Campaign with campaignId=%s and senderId=%s not found", campaignId, senderId)
                ));
    }

    private boolean isValid(Campaign campaign) {
        return !Objects.isNull(campaign)
                && StringUtils.hasText(campaign.getCampaignId())
                && StringUtils.hasText(campaign.getSenderId());
    }

    private boolean hasParameterNotFoundCause(Throwable throwable) {
        Throwable current = throwable;
        while (current != null) {
            if (current instanceof ParameterNotFoundException) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }
}