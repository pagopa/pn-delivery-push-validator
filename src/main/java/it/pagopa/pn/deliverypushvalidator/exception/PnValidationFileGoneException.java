package it.pagopa.pn.deliverypushvalidator.exception;

import it.pagopa.pn.commons.exceptions.PnValidationException;
import it.pagopa.pn.commons.exceptions.dto.ProblemError;

import java.util.List;

public class PnValidationFileGoneException extends PnValidationException {
    public PnValidationFileGoneException(String detail, Throwable ex) {
        super( detail ,
            List.of(
                ProblemError.builder()
                    .code(PnDeliveryPushValidatorExceptionCodes.NotificationRefusedErrorCodeInt.FILE_GONE.getValue())
                    .detail(detail)
                    .build()
            ),
            ex
        );
    }
}
