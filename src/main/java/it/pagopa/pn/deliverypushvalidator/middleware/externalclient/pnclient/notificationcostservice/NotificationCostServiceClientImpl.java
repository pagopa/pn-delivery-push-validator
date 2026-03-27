package it.pagopa.pn.deliverypushvalidator.middleware.externalclient.pnclient.notificationcostservice;

import it.pagopa.pn.deliverypushvalidator.generated.openapi.msclient.notificationcostservice.api.NotificationCostRecipientApi;
import it.pagopa.pn.deliverypushvalidator.generated.openapi.msclient.notificationcostservice.model.NewNotificationCostRequest;
import lombok.CustomLog;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@CustomLog
@RequiredArgsConstructor
public class NotificationCostServiceClientImpl implements NotificationCostServiceClient {
    private final NotificationCostRecipientApi notificationCostRecipientApi;

    @Override
    public Mono<String> initializeNotificationCost(String iun, NewNotificationCostRequest newNotificationCostRequest) {
        log.logInvokingExternalService(CLIENT_NAME, INITIALIZE_NOTIFICATION_COST);
        return notificationCostRecipientApi.initializeNotificationCost(iun, newNotificationCostRequest);
    }
}
