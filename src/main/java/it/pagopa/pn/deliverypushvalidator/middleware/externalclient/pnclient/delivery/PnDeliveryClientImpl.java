package it.pagopa.pn.deliverypushvalidator.middleware.externalclient.pnclient.delivery;


import it.pagopa.pn.deliverypushvalidator.generated.openapi.msclient.delivery.api.InternalOnlyApi;
import it.pagopa.pn.deliverypushvalidator.generated.openapi.msclient.delivery.model.InformalSentNotificationV1;
import it.pagopa.pn.deliverypushvalidator.generated.openapi.msclient.delivery.model.SentNotificationV25;
import lombok.CustomLog;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;


@CustomLog
@RequiredArgsConstructor
@Component
public class PnDeliveryClientImpl implements PnDeliveryClient{
    private final InternalOnlyApi pnDeliveryApi;

    @Override
    public SentNotificationV25 getSentNotification(String iun) {
        log.logInvokingExternalService(CLIENT_NAME, GET_NOTIFICATION);

        ResponseEntity<SentNotificationV25> res = pnDeliveryApi.getSentNotificationPrivateWithHttpInfo(iun);
        
        return res.getBody();
    }

    @Override
    public InformalSentNotificationV1 getSentInformalNotification(String iun) {
        log.logInvokingExternalService(CLIENT_NAME, GET_INFORMAL_NOTIFICATION);

        ResponseEntity<InformalSentNotificationV1> res = pnDeliveryApi.getSentInformalNotificationPrivateV1WithHttpInfo(iun);

        return res.getBody();
    }
}
