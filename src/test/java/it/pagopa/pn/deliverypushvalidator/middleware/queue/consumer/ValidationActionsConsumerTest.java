package it.pagopa.pn.deliverypushvalidator.middleware.queue.consumer;

import it.pagopa.pn.deliverypushvalidator.middleware.queue.consumer.router.EventRouter;
import it.pagopa.pn.deliverypushvalidator.middleware.queue.producer.abstractions.actionspool.Action;
import it.pagopa.pn.deliverypushvalidator.middleware.queue.producer.abstractions.actionspool.ActionType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(SpringExtension.class)
class ValidationActionsConsumerTest {
    @InjectMocks
    private ValidationActionsConsumer validationActionsConsumer;
    @Mock
    private EventRouter eventRouter;

    @Test
    void pnDeliveryPushValidationActionsInboundConsumer_routesMessageSuccessfully() {
        Action action = Action.builder().iun("test_IUN").recipientIndex(0).type(ActionType.NOTIFICATION_VALIDATION).build();
        Message<Action> message = Mockito.mock(Message.class);
        Mockito.when(message.getPayload()).thenReturn(action);
        Mockito.when(message.getHeaders()).thenReturn(new MessageHeaders(Map.of("test", "headerValue")));

        validationActionsConsumer.pnDeliveryPushValidationActionsInboundConsumer(message);

        EventRouter.RoutingConfig expectedConfig = EventRouter.RoutingConfig.builder()
                .eventType(ActionType.NOTIFICATION_VALIDATION.name())
                .build();
        Mockito.verify(eventRouter).route(message, expectedConfig);
    }

    @Test
    void pnDeliveryPushValidationActionsInboundConsumer_handlesExceptionGracefully() {
        Action action = Action.builder().iun("test_IUN").recipientIndex(0).build();
        Message<Action> message = Mockito.mock(Message.class);
        Mockito.when(message.getPayload()).thenReturn(action);
        Mockito.when(message.getHeaders()).thenReturn(new MessageHeaders(Map.of("test", "headerValue")));

        assertThrows(RuntimeException.class, () ->
                validationActionsConsumer.pnDeliveryPushValidationActionsInboundConsumer(message));

        Mockito.verify(eventRouter, Mockito.never()).route(Mockito.any(), Mockito.any());
    }


}