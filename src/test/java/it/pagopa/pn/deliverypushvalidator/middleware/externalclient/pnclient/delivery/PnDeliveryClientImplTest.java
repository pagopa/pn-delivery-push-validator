package it.pagopa.pn.deliverypushvalidator.middleware.externalclient.pnclient.delivery;

import it.pagopa.pn.deliverypushvalidator.generated.openapi.msclient.delivery.api.InternalOnlyApi;
import it.pagopa.pn.deliverypushvalidator.generated.openapi.msclient.delivery.model.InformalSentNotificationV1;
import it.pagopa.pn.deliverypushvalidator.generated.openapi.msclient.delivery.model.SentNotificationV26;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;

class PnDeliveryClientImplTest {

    @Mock
    private InternalOnlyApi pnDeliveryApi;

    private PnDeliveryClientImpl client;

    @BeforeEach
    void setup() {
        client = new PnDeliveryClientImpl(pnDeliveryApi);
    }

    @Test
    @ExtendWith(SpringExtension.class)
    void getSentNotification() {
        SentNotificationV26 notification = new SentNotificationV26();
        notification.setIun("001");
        
        Mockito.when(pnDeliveryApi.getSentNotificationPrivateWithHttpInfo("001")).thenReturn(ResponseEntity.ok(notification));

        SentNotificationV26 res = client.getSentNotification("001");

        Assertions.assertEquals("001", res.getIun());

    }

    @Test
    @ExtendWith(SpringExtension.class)
    void getSentInformalNotification() {
        InformalSentNotificationV1 notification = new InformalSentNotificationV1();
        notification.setIun("002");

        Mockito.when(pnDeliveryApi.getSentInformalNotificationPrivateV1WithHttpInfo("002", false)).thenReturn(ResponseEntity.ok(notification));

        InformalSentNotificationV1 res = client.getSentInformalNotification("002");

        Assertions.assertEquals("002", res.getIun());
    }
}