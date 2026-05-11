package it.pagopa.pn.deliverypushvalidator.service;

import it.pagopa.pn.deliverypushvalidator.dto.ext.delivery.notification.NotificationInt;

public interface NotificationService {
    NotificationInt getNotificationByIun(String iun);

    NotificationInt getInformalNotificationByIun(String iun);
}
