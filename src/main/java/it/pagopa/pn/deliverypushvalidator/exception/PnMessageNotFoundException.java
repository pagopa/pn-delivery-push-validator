package it.pagopa.pn.deliverypushvalidator.exception;

import static it.pagopa.pn.deliverypushvalidator.exception.PnDeliveryPushValidatorExceptionCodes.ERROR_CODE_DELIVERYPUSH_MESSAGE_NOT_FOUND;

public class PnMessageNotFoundException extends PnNotFoundException {

    public PnMessageNotFoundException(String description) {
        super("Message not found", description, ERROR_CODE_DELIVERYPUSH_MESSAGE_NOT_FOUND);
    }

    public PnMessageNotFoundException(String description, Throwable cause) {
        super("Message not found", description, ERROR_CODE_DELIVERYPUSH_MESSAGE_NOT_FOUND, cause);
    }
}

