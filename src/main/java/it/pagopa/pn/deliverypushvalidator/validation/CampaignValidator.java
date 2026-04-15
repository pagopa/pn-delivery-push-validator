package it.pagopa.pn.deliverypushvalidator.validation;

import it.pagopa.pn.deliverypushvalidator.config.CampaignParameterConsumer;
import it.pagopa.pn.deliverypushvalidator.dto.ext.delivery.notification.NotificationInt;
import it.pagopa.pn.deliverypushvalidator.exception.PnValidationCampaignException;
import lombok.AllArgsConstructor;
import lombok.CustomLog;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@AllArgsConstructor
@CustomLog
public class CampaignValidator {

    private final CampaignParameterConsumer campaignParameterConsumer;

    /**
     * Validates the campaign associated with the notification and returns its data.
     *
     * @param notification the informal notification
     * @return CampaignData if validation succeeds
     * @throws PnValidationCampaignException if validation fails (causes notification refusal)
     */
    public CampaignData validateAndGetCampaign(NotificationInt notification) {
        log.debug("Start campaign validation - iun={}", notification.getIun());

        String campaignId = notification.getCampaignId();
        if (StringUtils.isBlank(campaignId)) {
            throw new PnValidationCampaignException(
                    "Campaign id is required for INFORMAL notifications - iun=" + notification.getIun());
        }

        CampaignData campaign = campaignParameterConsumer.getCampaign(campaignId)
                .orElseThrow(() -> new PnValidationCampaignException(
                        String.format("Campaign not found for campaignId=%s - iun=%s", campaignId, notification.getIun())));

        if (Boolean.TRUE.equals(campaign.getClosed())) {
            throw new PnValidationCampaignException(
                    String.format("Campaign is closed for campaignId=%s - iun=%s", campaignId, notification.getIun()));
        }

        Instant now = Instant.now();
        if (campaign.getStartDate() != null && now.isBefore(campaign.getStartDate())) {
            throw new PnValidationCampaignException(
                    String.format("Campaign has not started yet for campaignId=%s startDate=%s - iun=%s",
                            campaignId, campaign.getStartDate(), notification.getIun()));
        }

        if (campaign.getEndDate() != null && now.isAfter(campaign.getEndDate())) {
            throw new PnValidationCampaignException(
                    String.format("Campaign has expired for campaignId=%s endDate=%s - iun=%s",
                            campaignId, campaign.getEndDate(), notification.getIun()));
        }

        log.info("Campaign validation passed for campaignId={} - iun={}", campaignId, notification.getIun());
        return campaign;
    }
}

