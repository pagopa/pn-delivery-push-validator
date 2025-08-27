package it.pagopa.pn.deliverypushvalidator.exceptions;

import it.pagopa.pn.commons.exceptions.dto.ProblemError;
import it.pagopa.pn.deliverypushvalidator.exception.PnDeliveryPushValidatorExceptionCodes;
import it.pagopa.pn.deliverypushvalidator.exception.PnLookupAddressValidationFailedException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class PnLookupAddressValidationFailedExceptionTest {

    @Test
    void testExceptionMessageAndDetails() {
        ProblemError error = new ProblemError();
        error.setCode(PnDeliveryPushValidatorExceptionCodes.NotificationRefusedErrorCodeInt.ADDRESS_NOT_FOUND.getValue());
        error.setDetail("Validazione fallita. Problemi nella ricerca sui registri pubblici");
        List<ProblemError> errors = List.of(error);
        PnLookupAddressValidationFailedException exception = new PnLookupAddressValidationFailedException(errors);

        assertNotNull(exception);
        assertNotNull(exception.getProblem());
        Assertions.assertEquals("ADDRESS_NOT_FOUND", exception.getProblem().getErrors().getFirst().getCode());
        Assertions.assertEquals("Validazione fallita. Problemi nella ricerca sui registri pubblici", exception.getProblem().getDetail());
    }

}
