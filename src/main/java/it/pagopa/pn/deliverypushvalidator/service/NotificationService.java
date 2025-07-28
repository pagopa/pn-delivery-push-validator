package it.pagopa.pn.deliverypushvalidator.service;


import it.pagopa.pn.deliverypushvalidator.dto.delivery.notification.NotificationInt;
import reactor.core.publisher.Mono;

import java.util.Map;

public interface NotificationService {
    NotificationInt getNotificationByIun(String iun);

    Map<String, String> getRecipientsQuickAccessLinkToken(String iun);
    
    Mono<NotificationInt> getNotificationByIunReactive(String iun);

    Mono<Void> removeAllNotificationCostsByIun(String iun);
}
