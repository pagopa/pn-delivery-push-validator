package it.pagopa.pn.deliverypushvalidator.action.it.mockbean;

import it.pagopa.pn.deliverypushvalidator.generated.openapi.msclient.delivery.model.NotificationFeePolicy;
import it.pagopa.pn.deliverypushvalidator.generated.openapi.msclient.deliverypush.model.NotificationProcessCostResponse;
import it.pagopa.pn.deliverypushvalidator.middleware.externalclient.pnclient.deliverypush.PnDeliveryPushClientReactive;
import reactor.core.publisher.Mono;

public class PnDeliveryPushClientMock implements PnDeliveryPushClientReactive {
    @Override
    public Mono<NotificationProcessCostResponse> getNotificationProcessCost(String iun, Integer recIndex, NotificationFeePolicy notificationFeePolicy, Boolean applyCost, Integer paFee, Integer vat) {
        return null;
    }
}
