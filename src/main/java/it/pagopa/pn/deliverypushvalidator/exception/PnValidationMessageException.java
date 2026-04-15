package it.pagopa.pn.deliverypushvalidator.exception;

import it.pagopa.pn.commons.exceptions.PnValidationException;
import it.pagopa.pn.commons.exceptions.dto.ProblemError;

import java.util.List;

public class PnValidationMessageException extends PnValidationException {

    public PnValidationMessageException(String detail) {
        super("Validation failed, message not valid",
                List.of(ProblemError.builder()
                        .code(PnDeliveryPushValidatorExceptionCodes.NotificationRefusedErrorCodeInt.MESSAGE_NOT_VALID.getValue())
                        .detail(detail)
                        .build()), null);
    }
}

