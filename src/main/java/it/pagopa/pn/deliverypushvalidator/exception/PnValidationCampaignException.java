package it.pagopa.pn.deliverypushvalidator.exception;

import it.pagopa.pn.commons.exceptions.PnValidationException;
import it.pagopa.pn.commons.exceptions.dto.ProblemError;

import java.util.List;

public class PnValidationCampaignException extends PnValidationException {

    public PnValidationCampaignException(String detail) {
        super("Validation failed, campaign not valid",
                List.of(ProblemError.builder()
                        .code(PnDeliveryPushValidatorExceptionCodes.NotificationRefusedErrorCodeInt.CAMPAIGN_NOT_VALID.getValue())
                        .detail(detail)
                        .build()), null);
    }
}

