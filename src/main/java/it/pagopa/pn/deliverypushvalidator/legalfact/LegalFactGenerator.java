package it.pagopa.pn.deliverypushvalidator.legalfact;


import it.pagopa.pn.deliverypushvalidator.dto.ext.delivery.notification.NotificationInt;

import java.io.IOException;

public interface LegalFactGenerator {

    /**
     * Generates the legal fact for a received notification.
     *
     * @param notification the notification object containing details about the notification.
     * @return a byte array representing the pdf legal fact for the received notification.
     */
    byte[] generateNotificationReceivedLegalFact(NotificationInt notification) throws IOException;

}
