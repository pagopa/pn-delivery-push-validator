package it.pagopa.pn.deliverypushvalidator.utils;

import it.pagopa.pn.commons.exceptions.PnInternalException;
import it.pagopa.pn.deliverypushvalidator.dto.ext.delivery.notification.CommunicationType;
import lombok.CustomLog;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import static it.pagopa.pn.deliverypushvalidator.exception.PnDeliveryPushValidatorExceptionCodes.ERROR_CODE_DELIVERYPUSH_INCOHERENT_COMMUNICATION_TYPE;

@Component
@CustomLog
public class CommunicationTypeChecker {
    private static final String LEGAL_IUN_NOTIFICATION_VERSION_CHAR = "1";
    private static final String INFORMAL_IUN_NOTIFICATION_VERSION_CHAR = "A";
    private static final String CHECK_ERROR_MESSAGE_TEMPLATE = "Incoherent communication type: expected %s version char (%s) but found %s as version char for iun: %s";

    public void checkAgainstIun(CommunicationType communicationType, @NotNull String iun) {
        if(communicationType == null) {
            failWithFatalError("Communication type is null for iun: " + iun);
        }

        String versionChar = iun.substring(iun.length() -1); // The version char is the last character of the IUN
        if (communicationType == CommunicationType.LEGAL && !LEGAL_IUN_NOTIFICATION_VERSION_CHAR.equals(versionChar)) {
            String msg = String.format(CHECK_ERROR_MESSAGE_TEMPLATE, "LEGAL", LEGAL_IUN_NOTIFICATION_VERSION_CHAR, versionChar, iun);
            failWithFatalError(msg);
        } else if (communicationType == CommunicationType.INFORMAL && !INFORMAL_IUN_NOTIFICATION_VERSION_CHAR.equals(versionChar)) {
            String msg = String.format(CHECK_ERROR_MESSAGE_TEMPLATE, "INFORMAL", INFORMAL_IUN_NOTIFICATION_VERSION_CHAR, versionChar, iun);
            failWithFatalError(msg);
        }
    }

    private void failWithFatalError(String message) {
        log.fatal(message);
        throw new PnInternalException(message, ERROR_CODE_DELIVERYPUSH_INCOHERENT_COMMUNICATION_TYPE);
    }
}
