package it.pagopa.pn.deliverypushvalidator.exception;

import it.pagopa.pn.commons.exceptions.PnValidationException;
import it.pagopa.pn.commons.exceptions.dto.ProblemError;

import java.util.List;

/**
 *
 * quickWorkAroundForPN-9116
 */
public class PnValidationMoreThan20GramsException extends PnValidationException {
    public PnValidationMoreThan20GramsException(String detail) {
        super("Validazione fallita, mittente non autorizzato ad inviare avvisi cartacei con piú di 3 fogli (20 grammi)",
                List.of(ProblemError.builder()
                        .code(PnDeliveryPushValidatorExceptionCodes.NotificationRefusedErrorCodeInt.SENDER_DISABLED_MORE_THAN_20_GRAMS.getValue())
                        .detail(detail)
                        .build()), null );
    }
}
