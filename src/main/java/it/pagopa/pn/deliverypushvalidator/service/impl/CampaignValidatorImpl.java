package it.pagopa.pn.deliverypushvalidator.service.impl;

import it.pagopa.pn.commons.exceptions.dto.ProblemError;
import it.pagopa.pn.deliverypushvalidator.dto.campaign.Campaign;
import it.pagopa.pn.deliverypushvalidator.dto.campaign.CampaignStatus;
import it.pagopa.pn.deliverypushvalidator.dto.ext.delivery.notification.NotificationInt;
import it.pagopa.pn.deliverypushvalidator.exception.PnCampaignNotFoundException;
import it.pagopa.pn.deliverypushvalidator.exception.PnCampaignValidationException;
import it.pagopa.pn.deliverypushvalidator.exception.PnDeliveryPushValidatorExceptionCodes.NotificationRefusedErrorCodeInt;
import it.pagopa.pn.deliverypushvalidator.service.CampaignService;
import it.pagopa.pn.deliverypushvalidator.service.CampaignValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class CampaignValidatorImpl implements CampaignValidator {

    private static final String CAMPAIGN_ELEMENT = "campaignId";
    private final CampaignService campaignService;

    @Override
    public Campaign validateAndGetCampaign(NotificationInt notification) {
        String campaignId = notification.getCampaignId();
        String senderId = notification.getSender().getPaId();

        log.debug("Validating campaign [campaignId={}] for sender [senderId={}]", campaignId, senderId);

        Campaign campaign = fetchCampaign(campaignId, senderId);
        validateCampaignStatus(campaign, campaignId);

        log.debug("Campaign validated successfully [campaignId={}]", campaignId);
        return campaign;
    }

    private Campaign fetchCampaign(String campaignId, String senderId) {
        try {
            return campaignService.getCampaignByCampaignIdAndSenderId(campaignId, senderId);
        } catch (PnCampaignNotFoundException ex) {
            throw handleValidationException(
                    NotificationRefusedErrorCodeInt.CAMPAIGN_NOT_FOUND,
                    String.format("Campaign with id %s not found", campaignId)
            );
        }
    }

    private void validateCampaignStatus(Campaign campaign, String campaignId) {
        if (!CampaignStatus.IN_PROGRESS.equals(campaign.getStatus())) {
            throw handleValidationException(
                    NotificationRefusedErrorCodeInt.CAMPAIGN_INVALID,
                    String.format("Campaign %s has %s status", campaignId, campaign.getStatus())
            );
        }
    }

    private PnCampaignValidationException handleValidationException(NotificationRefusedErrorCodeInt errorCode, String detail) {
        ProblemError problemError = ProblemError.builder()
                .code(errorCode.getValue())
                .element(CAMPAIGN_ELEMENT)
                .detail(detail)
                .build();
        return new PnCampaignValidationException(detail, List.of(problemError));
    }
}