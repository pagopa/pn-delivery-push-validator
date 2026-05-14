package it.pagopa.pn.deliverypushvalidator.exceptions;

import it.pagopa.pn.deliverypushvalidator.exception.PnDeliveryPushValidatorExceptionCodes;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;


class PnDeliveryPushValidationExceptionCodesTest {

    @Test
    void checkAll() {
        Assertions.assertAll(
                () -> Assertions.assertEquals("PN_DELIVERYPUSH_NOTFOUND", PnDeliveryPushValidatorExceptionCodes.ERROR_CODE_DELIVERYPUSH_NOTFOUND),
                () -> Assertions.assertEquals("PN_DELIVERYPUSH_GETFILEERROR", PnDeliveryPushValidatorExceptionCodes.ERROR_CODE_DELIVERYPUSH_GETFILEERROR),
                () -> Assertions.assertEquals("PN_DELIVERYPUSH_MESSAGE_NOT_FOUND", PnDeliveryPushValidatorExceptionCodes.ERROR_CODE_DELIVERYPUSH_MESSAGE_NOT_FOUND),
                () -> Assertions.assertEquals(
                        PnDeliveryPushValidatorExceptionCodes.NotificationRefusedErrorCodeInt.MESSAGE_NOT_FOUND,
                        PnDeliveryPushValidatorExceptionCodes.NotificationRefusedErrorCodeInt.fromValue("MESSAGE_NOT_FOUND")
                ),
                () -> Assertions.assertEquals(
                        PnDeliveryPushValidatorExceptionCodes.NotificationRefusedErrorCodeInt.MESSAGE_LANGUAGE_MISMATCH,
                        PnDeliveryPushValidatorExceptionCodes.NotificationRefusedErrorCodeInt.fromValue("MESSAGE_LANGUAGE_MISMATCH")
                )
        );
    }

    @Test
    void fromValueShouldReturnNullForInvalidValues() {
        Assertions.assertAll(
                () -> Assertions.assertNull(PnDeliveryPushValidatorExceptionCodes.NotificationRefusedErrorCodeInt.fromValue(null)),
                () -> Assertions.assertNull(PnDeliveryPushValidatorExceptionCodes.NotificationRefusedErrorCodeInt.fromValue("")),
                () -> Assertions.assertNull(PnDeliveryPushValidatorExceptionCodes.NotificationRefusedErrorCodeInt.fromValue("NOT_EXISTENT_ERROR_CODE"))
        );
    }

}