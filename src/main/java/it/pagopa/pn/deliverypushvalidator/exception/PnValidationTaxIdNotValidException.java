package it.pagopa.pn.deliverypushvalidator.exception;

import it.pagopa.pn.commons.exceptions.PnValidationException;
import it.pagopa.pn.commons.exceptions.dto.ProblemError;

import java.util.List;

public class PnValidationTaxIdNotValidException extends PnValidationException {

    public PnValidationTaxIdNotValidException(String detail) {
        super( detail ,
                List.of(ProblemError.builder()
                .code(PnDeliveryPushValidatorExceptionCodes.NotificationRefusedErrorCodeInt.TAXID_NOT_VALID.getValue())
                .detail(detail)
                .build())
        );
    }

}
