package it.pagopa.pn.deliverypushvalidator.exceptions;

import it.pagopa.pn.deliverypushvalidator.exception.PnDeliveryPushValidatorExceptionCodes;
import it.pagopa.pn.deliverypushvalidator.exception.PnValidationMessageNotFoundException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class PnValidationMessageNotFoundExceptionTest {

    @Test
    void constructorSetsValidationErrorCodeDetailAndElement() {
        String detail = "Message with id: abc not found";
        String element = "recipients[0].messageId";

        PnValidationMessageNotFoundException exception = new PnValidationMessageNotFoundException(detail, element);

        Assertions.assertNotNull(exception.getProblem());
        Assertions.assertEquals(
                PnDeliveryPushValidatorExceptionCodes.NotificationRefusedErrorCodeInt.MESSAGE_NOT_FOUND.getValue(),
                exception.getProblem().getErrors().getFirst().getCode()
        );
        Assertions.assertEquals(detail, exception.getProblem().getErrors().getFirst().getDetail());
        Assertions.assertEquals(element, exception.getProblem().getErrors().getFirst().getElement());
    }
}

