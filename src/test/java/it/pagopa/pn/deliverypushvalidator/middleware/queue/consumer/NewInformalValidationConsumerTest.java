package it.pagopa.pn.deliverypushvalidator.middleware.queue.consumer;

import it.pagopa.pn.api.dto.events.PnDeliveryNewNotificationEvent;
import it.pagopa.pn.deliverypushvalidator.action.startworkflow.StartWorkflowHandler;
import it.pagopa.pn.deliverypushvalidator.dto.timeline.CommunicationType;
import it.pagopa.pn.deliverypushvalidator.middleware.queue.consumer.handler.utils.HandleEventUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
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

        try (MockedStatic<HandleEventUtils> utilsMock = mockStatic(HandleEventUtils.class)) {
            assertDoesNotThrow(() ->
                    consumer.informalValidationInputsEventConsumer(message)
            );

            utilsMock.verify(() -> HandleEventUtils.addIunToMdc("TEST_IUN"));
            verify(startWorkflowHandler).startWorkflow("TEST_IUN", CommunicationType.INFORMAL);
        }
    }

    @Test
    void testInformalValidationInputsEventConsumer_exceptionOnStartWorkflow() {
        // Arrange
        PnDeliveryNewNotificationEvent.Payload payload = mock(PnDeliveryNewNotificationEvent.Payload.class);
        when(payload.getIun()).thenReturn("TEST_IUN");

        Message<PnDeliveryNewNotificationEvent.Payload> message =
                MessageBuilder.withPayload(payload).build();

        doThrow(new RuntimeException("workflow error"))
                .when(startWorkflowHandler).startWorkflow("TEST_IUN", CommunicationType.INFORMAL);

        try (MockedStatic<HandleEventUtils> utilsMock = mockStatic(HandleEventUtils.class)) {
            assertThrows(RuntimeException.class, () ->
                    consumer.informalValidationInputsEventConsumer(message)
            );

            utilsMock.verify(() -> HandleEventUtils.handleException(any(), any()));
        }
    }

    @Test
    void testInformalValidationInputsEventConsumer_exceptionOnGetIun() {
        // Arrange
        PnDeliveryNewNotificationEvent.Payload payload = mock(PnDeliveryNewNotificationEvent.Payload.class);
        when(payload.getIun()).thenThrow(new RuntimeException("boom"));

        Message<PnDeliveryNewNotificationEvent.Payload> message =
                MessageBuilder.withPayload(payload).build();

        try (MockedStatic<HandleEventUtils> utilsMock = mockStatic(HandleEventUtils.class)) {
            assertThrows(RuntimeException.class, () ->
                    consumer.informalValidationInputsEventConsumer(message)
            );

            utilsMock.verify(() -> HandleEventUtils.handleException(any(), any()));
            verify(startWorkflowHandler, never()).startWorkflow(any(), any());
        }
    }
}