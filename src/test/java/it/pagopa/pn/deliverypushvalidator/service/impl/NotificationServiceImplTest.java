package it.pagopa.pn.deliverypushvalidator.service.impl;

import it.pagopa.pn.commons.exceptions.PnHttpResponseException;
import it.pagopa.pn.commons.exceptions.PnInternalException;
import it.pagopa.pn.deliverypushvalidator.dto.ext.delivery.notification.NotificationInt;
import it.pagopa.pn.deliverypushvalidator.dto.ext.delivery.notification.NotificationSenderInt;
import it.pagopa.pn.deliverypushvalidator.dto.ext.delivery.notification.ServiceLevelTypeInt;
import it.pagopa.pn.deliverypushvalidator.generated.openapi.msclient.delivery.model.InformalSentNotificationV1;
import it.pagopa.pn.deliverypushvalidator.generated.openapi.msclient.delivery.model.NotificationFeePolicy;
import it.pagopa.pn.deliverypushvalidator.generated.openapi.msclient.delivery.model.SentNotificationV25;
import it.pagopa.pn.deliverypushvalidator.middleware.externalclient.pnclient.delivery.PnDeliveryClient;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Collections;

class NotificationServiceImplTest {

    @Mock
    private PnDeliveryClient pnDeliveryClient;

    private NotificationServiceImpl service;

    @BeforeEach
    void setup() {
        service = new NotificationServiceImpl(pnDeliveryClient);
    }

    @Test
    @ExtendWith(SpringExtension.class)
    void getNotificationByIun() {
        NotificationInt expected = buildNotificationInt();

        SentNotificationV25 sentNotification = buildSentNotification();
        Mockito.when(pnDeliveryClient.getSentNotification("001")).thenReturn(sentNotification);

        NotificationInt actual = service.getNotificationByIun("001");

        Assertions.assertEquals(expected, actual);
    }

    @Test
    @ExtendWith(SpringExtension.class)
    void getNotificationByIunNotFound() {

        Mockito.when(pnDeliveryClient.getSentNotification("001")).thenThrow(PnHttpResponseException.class);

        Assertions.assertThrows(PnHttpResponseException.class, () -> service.getNotificationByIun("001"));

    }

    @Test
    @ExtendWith(SpringExtension.class)
    void getInformalNotificationByIun() {
        InformalSentNotificationV1 sentInformalNotification = new InformalSentNotificationV1();
        sentInformalNotification.setIun("001");
        sentInformalNotification.setRecipients(Collections.emptyList());

        Mockito.when(pnDeliveryClient.getSentInformalNotification("001")).thenReturn(sentInformalNotification);

        NotificationInt actual = service.getInformalNotificationByIun("001");

        Assertions.assertEquals("001", actual.getIun());
        Assertions.assertEquals(Collections.emptyList(), actual.getRecipients());
    }

    @Test
    @ExtendWith(SpringExtension.class)
    void getInformalNotificationByIunNotFound() {
        Mockito.when(pnDeliveryClient.getSentInformalNotification("001")).thenThrow(PnHttpResponseException.class);

        Assertions.assertThrows(PnHttpResponseException.class, () -> service.getInformalNotificationByIun("001"));
    }

    @Test
    @ExtendWith(SpringExtension.class)
    void getNotificationByIunThrowsPnInternalExceptionWhenNull() {
        Mockito.when(pnDeliveryClient.getSentNotification("002")).thenReturn(null);

        Assertions.assertThrows(PnInternalException.class, () -> service.getNotificationByIun("002"));
    }

    @Test
    @ExtendWith(SpringExtension.class)
    void getInformalNotificationByIunThrowsPnInternalExceptionWhenNull() {
        Mockito.when(pnDeliveryClient.getSentInformalNotification("003")).thenReturn(null);

        Assertions.assertThrows(PnInternalException.class, () -> service.getInformalNotificationByIun("003"));
    }

    private SentNotificationV25 buildSentNotification() {
        SentNotificationV25 sentNotification = new SentNotificationV25();
        sentNotification.setIun("001");
        sentNotification.setPhysicalCommunicationType(SentNotificationV25.PhysicalCommunicationTypeEnum.REGISTERED_LETTER_890);
        sentNotification.setNotificationFeePolicy(it.pagopa.pn.deliverypushvalidator.generated.openapi.msclient.delivery.model.NotificationFeePolicy.DELIVERY_MODE);
        return sentNotification;
    }
    
    private NotificationInt buildNotificationInt() {
        return NotificationInt.builder()
                .iun("001")
                .recipients(Collections.emptyList())
                .documents(Collections.emptyList())
                .sender(NotificationSenderInt.builder().build())
                .notificationFeePolicy(NotificationFeePolicy.DELIVERY_MODE)
                .physicalCommunicationType(ServiceLevelTypeInt.REGISTERED_LETTER_890)
                .build();
    }
}