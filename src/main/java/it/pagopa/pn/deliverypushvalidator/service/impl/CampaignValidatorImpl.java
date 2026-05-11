package it.pagopa.pn.deliverypushvalidator.service.impl;

import it.pagopa.pn.commons.exceptions.dto.ProblemError;
import it.pagopa.pn.deliverypushvalidator.dto.campaign.Campaign;
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
        validateCampaignIsOpen(campaign, campaignId);

        log.debug("Campaign validated successfully [campaignId={}]", campaignId);
        return campaign;
    }

    private Campaign fetchCampaign(String campaignId, String senderId) {
        try {
            return campaignService.getCampaignByCampaignIdAndSenderId(campaignId, senderId);
        } catch (PnCampaignNotFoundException ex) {
            handleValidationException(
                    NotificationRefusedErrorCodeInt.CAMPAIGN_NOT_FOUND,
                    String.format("No campaign with id %s for sender %s", campaignId, senderId)
            );
            throw ex;
        }
    }

    private void validateCampaignIsOpen(Campaign campaign, String campaignId) {
        if (campaign.isClosed()) {
            handleValidationException(
                    NotificationRefusedErrorCodeInt.CAMPAIGN_CLOSED,
                    String.format("Campaign %s is closed", campaignId)
            );
        }
    }

    private void handleValidationException(NotificationRefusedErrorCodeInt errorCode, String detail) {
        ProblemError problemError = ProblemError.builder()
                .code(errorCode.getValue())
                .element(CAMPAIGN_ELEMENT)
                .detail(detail)
                .build();
        throw new PnCampaignValidationException(errorCode.getValue(), List.of(problemError));
    }
}