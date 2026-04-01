package it.pagopa.pn.deliverypushvalidator.action.it.mockbean;

import it.pagopa.pn.deliverypushvalidator.generated.openapi.msclient.notificationcostservice.model.NewNotificationCostRequest;
import it.pagopa.pn.deliverypushvalidator.middleware.externalclient.pnclient.notificationcostservice.NotificationCostServiceClient;
import reactor.core.publisher.Mono;

public class NotificationCostServiceClientMock implements NotificationCostServiceClient {

    @Override
    public Mono<String> initializeNotificationCost(String iun, NewNotificationCostRequest newNotificationCostRequest) {

        // Risposta di default: successo con l'iun stesso come risposta
        return Mono.just(iun);
    }
}
