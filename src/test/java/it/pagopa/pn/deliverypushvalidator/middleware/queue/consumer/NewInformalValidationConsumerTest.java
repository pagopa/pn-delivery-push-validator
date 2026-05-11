package it.pagopa.pn.deliverypushvalidator.middleware.queue.consumer;

import it.pagopa.pn.api.dto.events.PnDeliveryNewNotificationEvent;
import it.pagopa.pn.deliverypushvalidator.action.startworkflow.StartWorkflowHandler;
import it.pagopa.pn.deliverypushvalidator.dto.timeline.CommunicationType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NewInformalValidationConsumerTest {

    @Mock
    private StartWorkflowHandler startWorkflowHandler;

    @InjectMocks
    private NewInformalValidationConsumer consumer;


    @Test
    void testInformalValidationInputsEventConsumer_ok() {
        // Arrange
        PnDeliveryNewNotificationEvent.Payload payload = mock(PnDeliveryNewNotificationEvent.Payload.class);
        when(payload.getIun()).thenReturn("TEST_IUN");

        Message<PnDeliveryNewNotificationEvent.Payload> message =
                MessageBuilder.withPayload(payload).build();

        assertDoesNotThrow(() ->
                consumer.informalValidationInputsEventConsumer(message)
        );

        verify(startWorkflowHandler).startWorkflow("TEST_IUN", CommunicationType.INFORMAL);
    }

    @Test
    void testInformalValidationInputsEventConsumer_exceptionOnStartWorkflow() {
        PnDeliveryNewNotificationEvent.Payload payload = mock(PnDeliveryNewNotificationEvent.Payload.class);
        when(payload.getIun()).thenReturn("TEST_IUN");

        Message<PnDeliveryNewNotificationEvent.Payload> message =
                MessageBuilder.withPayload(payload).build();

        doThrow(new RuntimeException("workflow error"))
                .when(startWorkflowHandler).startWorkflow("TEST_IUN", CommunicationType.INFORMAL);

        assertThrows(RuntimeException.class, () ->
                consumer.informalValidationInputsEventConsumer(message)
        );

        verify(startWorkflowHandler).startWorkflow("TEST_IUN", CommunicationType.INFORMAL);
    }

    @Test
    void testInformalValidationInputsEventConsumer_exceptionOnGetIun() {
        PnDeliveryNewNotificationEvent.Payload payload = mock(PnDeliveryNewNotificationEvent.Payload.class);
        when(payload.getIun()).thenThrow(new RuntimeException("boom"));

        Message<PnDeliveryNewNotificationEvent.Payload> message =
                MessageBuilder.withPayload(payload).build();

        assertThrows(RuntimeException.class, () ->
                consumer.informalValidationInputsEventConsumer(message)
        );

        verify(startWorkflowHandler, never()).startWorkflow(any(), any());
    }
}