package it.pagopa.pn.deliverypushvalidator.exception;

import it.pagopa.pn.commons.exceptions.PnValidationException;
import it.pagopa.pn.commons.exceptions.dto.ProblemError;

import java.util.List;

public class PnValidationPaymentException extends PnValidationException {

    public PnValidationPaymentException(String detail) {
        super("Validation failed, payment not valid",
                List.of(ProblemError.builder()
                .code(PnDeliveryPushValidatorExceptionCodes.NotificationRefusedErrorCodeInt.PAYMENT_NOT_VALID.getValue())
                .detail(detail)
                .build()), null );
    }
}
