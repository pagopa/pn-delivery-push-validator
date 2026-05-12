package it.pagopa.pn.deliverypushvalidator.exception;

import it.pagopa.pn.commons.exceptions.PnValidationException;
import it.pagopa.pn.commons.exceptions.dto.ProblemError;

import java.util.List;

public class PnValidationSenderIdNotValidException extends PnValidationException {

    public PnValidationSenderIdNotValidException(String detail, String element) {
        super(detail,
                List.of(ProblemError.builder()
                        .code(PnDeliveryPushValidatorExceptionCodes.ERROR_CODE_DELIVERYPUSH_SENDER_ID_NOT_VALID)
                        .detail(detail)
                        .element(element)
                        .build()), null);
    }
}
