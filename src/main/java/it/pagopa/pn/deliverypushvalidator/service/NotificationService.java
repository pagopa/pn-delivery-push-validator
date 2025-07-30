package it.pagopa.pn.deliverypushvalidator.service;


import it.pagopa.pn.deliverypushvalidator.dto.ext.delivery.notification.NotificationInt;

import java.util.Map;

public interface NotificationService {
    NotificationInt getNotificationByIun(String iun);

    Map<String, String> getRecipientsQuickAccessLinkToken(String iun);
}
