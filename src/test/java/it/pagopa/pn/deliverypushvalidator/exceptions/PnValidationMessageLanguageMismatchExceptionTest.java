package it.pagopa.pn.deliverypushvalidator.exceptions;

import it.pagopa.pn.deliverypushvalidator.exception.PnDeliveryPushValidatorExceptionCodes;
import it.pagopa.pn.deliverypushvalidator.exception.PnValidationMessageLanguageMismatchException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class PnValidationMessageLanguageMismatchExceptionTest {

    @Test
    void constructorSetsValidationErrorCodeDetailAndElement() {
        String detail = "Message language 'fr' does not match additional languages";
        String element = "recipients[0].messageId";

        PnValidationMessageLanguageMismatchException exception =
                new PnValidationMessageLanguageMismatchException(detail, element);

        Assertions.assertNotNull(exception.getProblem());
        Assertions.assertEquals(
                PnDeliveryPushValidatorExceptionCodes.NotificationRefusedErrorCodeInt.MESSAGE_LANGUAGE_MISMATCH.getValue(),
                exception.getProblem().getErrors().getFirst().getCode()
        );
        Assertions.assertEquals(detail, exception.getProblem().getErrors().getFirst().getDetail());
        Assertions.assertEquals(element, exception.getProblem().getErrors().getFirst().getElement());
    }
}

