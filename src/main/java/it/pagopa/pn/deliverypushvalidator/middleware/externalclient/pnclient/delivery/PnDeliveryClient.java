package it.pagopa.pn.deliverypushvalidator.middleware.externalclient.pnclient.delivery;

import it.pagopa.pn.commons.log.PnLogger;
import it.pagopa.pn.deliverypushvalidator.generated.openapi.msclient.delivery.model.InformalSentNotificationV1;
import it.pagopa.pn.deliverypushvalidator.generated.openapi.msclient.delivery.model.SentNotificationV25;

public interface PnDeliveryClient {
    String CLIENT_NAME = PnLogger.EXTERNAL_SERVICES.PN_DELIVERY;

    String GET_NOTIFICATION = "GET NOTIFICATION";
    String GET_INFORMAL_NOTIFICATION = "GET INFORMAL NOTIFICATION";

    SentNotificationV25 getSentNotification(String iun);

    InformalSentNotificationV1 getSentInformalNotification(String iun);
}
