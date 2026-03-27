package it.pagopa.pn.deliverypushvalidator.middleware.externalclient.pnclient.notificationcostservice;

import it.pagopa.pn.commons.log.PnLogger;
import it.pagopa.pn.deliverypushvalidator.generated.openapi.msclient.notificationcostservice.model.NewNotificationCostRequest;
import reactor.core.publisher.Mono;

public interface NotificationCostServiceClient {
    String CLIENT_NAME = PnLogger.EXTERNAL_SERVICES.PN_NOTIFICATION_COST_SERVICE;
    String INITIALIZE_NOTIFICATION_COST = "INITIALIZE NOTIFICATION COST";

    Mono<String> initializeNotificationCost(String iun, NewNotificationCostRequest newNotificationCostRequest);
}
