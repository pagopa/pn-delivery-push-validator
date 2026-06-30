package it.pagopa.pn.deliverypushvalidator.exception;

import it.pagopa.pn.commons.exceptions.PnValidationException;
import it.pagopa.pn.commons.exceptions.dto.ProblemError;

import java.util.List;

public class PnValidationMessageNotFoundException extends PnValidationException {

    public PnValidationMessageNotFoundException(String detail, String element) {
        super("Validation failed, message not found",
                List.of(ProblemError.builder()
                        .code(PnDeliveryPushValidatorExceptionCodes.NotificationRefusedErrorCodeInt.MESSAGE_NOT_FOUND.getValue())
                        .detail(detail)
                        .element(element)
                        .build()), null);
    }
}

