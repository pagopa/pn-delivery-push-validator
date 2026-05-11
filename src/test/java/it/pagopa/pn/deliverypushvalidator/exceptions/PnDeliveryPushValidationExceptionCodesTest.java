package it.pagopa.pn.deliverypushvalidator.exceptions;

import it.pagopa.pn.deliverypushvalidator.exception.PnDeliveryPushValidatorExceptionCodes;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;


class PnDeliveryPushValidationExceptionCodesTest {

    private PnDeliveryPushValidatorExceptionCodes code;

    @Test
    void checkAll() {
        Assertions.assertAll(
                () -> Assertions.assertEquals("PN_DELIVERYPUSH_NOTFOUND", code.ERROR_CODE_DELIVERYPUSH_NOTFOUND),
                () -> Assertions.assertEquals("PN_DELIVERYPUSH_GETFILEERROR", code.ERROR_CODE_DELIVERYPUSH_GETFILEERROR),
                () -> Assertions.assertEquals("PN_DELIVERYPUSH_MESSAGE_NOT_FOUND", code.ERROR_CODE_DELIVERYPUSH_MESSAGE_NOT_FOUND)
        );
    }

}