package it.pagopa.pn.deliverypushvalidator.service;

import it.pagopa.pn.deliverypushvalidator.dto.ext.delivery.notification.NotificationInt;

public interface SaveLegalFactsService {
    String sendCreationRequestForNotificationReceivedLegalFact(NotificationInt notification);
}
