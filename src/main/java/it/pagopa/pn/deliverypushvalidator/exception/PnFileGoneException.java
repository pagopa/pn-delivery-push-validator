package it.pagopa.pn.deliverypushvalidator.exception;

import it.pagopa.pn.commons.exceptions.PnInternalException;
import static it.pagopa.pn.deliverypushvalidator.exception.PnDeliveryPushValidatorExceptionCodes.ERROR_CODE_DELIVERYPUSH_FILE_GONE;

public class PnFileGoneException extends PnInternalException {

    public PnFileGoneException(String message, Throwable cause) {
        super(message, ERROR_CODE_DELIVERYPUSH_FILE_GONE, cause);
    }
}
