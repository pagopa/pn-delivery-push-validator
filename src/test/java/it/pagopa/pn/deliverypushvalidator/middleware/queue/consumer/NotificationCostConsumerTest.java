package it.pagopa.pn.deliverypushvalidator.middleware.queue.consumer;

import it.pagopa.pn.api.dto.events.notificationcost.utils.ValidationStatus;
import it.pagopa.pn.api.dto.events.notificationcost.validation.PnNotificationCostValidationEvent;
import it.pagopa.pn.api.dto.events.notificationcost.validation.PnNotificationCostValidationEventPayload;
import it.pagopa.pn.deliverypushvalidator.action.startworkflow.notificationvalidation.NotificationValidationActionHandler;
import it.pagopa.pn.deliverypushvalidator.dto.timeline.CommunicationType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

@ExtendWith(SpringExtension.class)
class NotificationCostConsumerTest {

    @Mock
    private NotificationValidationActionHandler handler;

    @InjectMocks
    private NotificationCostConsumer notificationCostConsumer;

    @Test
    void pnNotificationCostEventInboundConsumerOk() {
        // Given
        String iun = "TEST-IUN-001";
        CommunicationType communicationType = CommunicationType.LEGAL;
        PnNotificationCostValidationEventPayload payload = PnNotificationCostValidationEventPayload.builder()
                .iun(iun)
                .status(ValidationStatus.OK)
                .build();

        PnNotificationCostValidationEvent.Detail detail = new PnNotificationCostValidationEvent.Detail();
        detail.setPnNotificationCostValidationPayload(payload);

        Message<PnNotificationCostValidationEvent.Detail> message = MessageBuilder.withPayload(detail)
                .setHeader("test", "headerValue")
                .build();

        // When
        notificationCostConsumer.pnNotificationCostEventInboundConsumer(message);

        // Then
        Mockito.verify(handler, Mockito.times(1)).handleValidateNotificationCost(eq(iun), eq(payload), eq(communicationType));
    }

    @Test
    void pnNotificationCostEventInboundConsumerKO() {
        // Given
        String iun = "TEST-IUN-002";
        CommunicationType communicationType = CommunicationType.LEGAL;
        PnNotificationCostValidationEventPayload payload = PnNotificationCostValidationEventPayload.builder()
                .iun(iun)
                .status(ValidationStatus.KO)
                .build();

        PnNotificationCostValidationEvent.Detail detail = new PnNotificationCostValidationEvent.Detail();
        detail.setPnNotificationCostValidationPayload(payload);

        Message<PnNotificationCostValidationEvent.Detail> message = MessageBuilder.withPayload(detail)
                .setHeader("test", "headerValue")
                .build();

        Mockito.doThrow(new RuntimeException("Test Exception")).when(handler).handleValidateNotificationCost(any(), any(), eq(communicationType));

        // When/Then
        RuntimeException exception = Assertions.assertThrows(RuntimeException.class, () ->
                notificationCostConsumer.pnNotificationCostEventInboundConsumer(message));
        Assertions.assertEquals("Test Exception", exception.getMessage());

        Mockito.verify(handler, Mockito.times(1)).handleValidateNotificationCost(eq(iun), eq(payload), eq(communicationType));
    }

    @Test
    void pnNotificationCostEventInboundConsumerKOStatusHandled() {
        // Given
        String iun = "TEST-IUN-003";
        CommunicationType communicationType = CommunicationType.LEGAL;
        PnNotificationCostValidationEventPayload payload = PnNotificationCostValidationEventPayload.builder()
                .iun(iun)
                .status(ValidationStatus.KO)
                .build();

        PnNotificationCostValidationEvent.Detail detail = new PnNotificationCostValidationEvent.Detail();
        detail.setPnNotificationCostValidationPayload(payload);

        Message<PnNotificationCostValidationEvent.Detail> message = MessageBuilder.withPayload(detail)
                .setHeader("test", "headerValue")
                .build();

        // When
        notificationCostConsumer.pnNotificationCostEventInboundConsumer(message);

        // Then
        Mockito.verify(handler, Mockito.times(1)).handleValidateNotificationCost(eq(iun), eq(payload), eq(communicationType));
    }
}
