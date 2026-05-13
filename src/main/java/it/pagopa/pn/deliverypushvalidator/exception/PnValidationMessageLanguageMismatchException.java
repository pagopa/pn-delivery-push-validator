package it.pagopa.pn.deliverypushvalidator.exception;

import it.pagopa.pn.commons.exceptions.PnValidationException;
import it.pagopa.pn.commons.exceptions.dto.ProblemError;

import java.util.List;

public class PnValidationMessageLanguageMismatchException extends PnValidationException {

    public PnValidationMessageLanguageMismatchException(String detail, String element) {
        super("Validation failed, message language mismatch",
                List.of(ProblemError.builder()
                        .code(PnDeliveryPushValidatorExceptionCodes.NotificationRefusedErrorCodeInt.MESSAGE_LANGUAGE_MISMATCH.getValue())
                        .detail(detail)
                        .element(element)
                        .build()), null);
    }
}

