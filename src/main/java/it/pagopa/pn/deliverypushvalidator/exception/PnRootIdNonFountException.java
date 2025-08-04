package it.pagopa.pn.deliverypushvalidator.exception;
import static it.pagopa.pn.deliverypushvalidator.exception.PnDeliveryPushValidatorExceptionCodes.ERROR_CODE_DELIVERYPUSH_ROOTIDNOTFOUND;

public class PnRootIdNonFountException extends PnNotFoundException {

    public PnRootIdNonFountException(String description) {
        super("RootId not found", description, ERROR_CODE_DELIVERYPUSH_ROOTIDNOTFOUND);
    }

}
