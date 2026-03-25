package it.pagopa.pn.deliverypushvalidator.middleware.externalclient.pnclient.notificationcostservice;

import it.pagopa.pn.deliverypushvalidator.generated.openapi.msclient.notificationcostservice.api.NotificationCostRecipientApi;
import it.pagopa.pn.deliverypushvalidator.generated.openapi.msclient.notificationcostservice.model.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.eq;

class PnNotificationCostServiceClientImplTest {
    private NotificationCostRecipientApi notificationCostRecipientApi;

    private NotificationCostServiceClientImpl client;

    @BeforeEach
    void setup() {
        this.notificationCostRecipientApi = Mockito.mock(NotificationCostRecipientApi.class);
        client = new NotificationCostServiceClientImpl(notificationCostRecipientApi);
    }

    @Test
    void initializeNotificationCost() {
        // Given
        String iun = "testIun";
        String expectedResponse = "cost-uuid-123";
        NewNotificationCostRequest request = buildMockNewNotificationCostRequest();

        Mockito.when(notificationCostRecipientApi.initializeNotificationCost(iun, request))
                .thenReturn(Mono.just(expectedResponse));

        // When
        Mono<String> response = client.initializeNotificationCost(iun, request);

        // Then
        StepVerifier.create(response)
                .assertNext(result -> {
                    Assertions.assertNotNull(result);
                    Assertions.assertEquals(expectedResponse, result);
                })
                .verifyComplete();

        Mockito.verify(notificationCostRecipientApi).initializeNotificationCost(eq(iun), eq(request));
    }

    private NewNotificationCostRequest buildMockNewNotificationCostRequest() {
        PaymentData paymentData = new PaymentData()
                .iuv("302000100000019421")
                .applyCost(true);

        RecipientCostData recipientCostData = new RecipientCostData()
                .recIndex(0)
                .recipientInternalId("PF-4fc75df3-0913-407e-bdaa-e50329708b7d")
                .senderInternalId("PA-abc123")
                .payments(Collections.singletonList(paymentData))
                .baseCost(150)
                .paFee(50)
                .notificationFeePolicy(NotificationFeePolicy.DELIVERY_MODE)
                .pagoPaIntMode(PagoPaIntMode.SYNC)
                .vat(22);

        return new NewNotificationCostRequest()
                .costRecipients(Collections.singletonList(recipientCostData));
    }
}
