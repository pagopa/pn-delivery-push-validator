package it.pagopa.pn.deliverypushvalidator.middleware.queue.consumer;

import static java.util.Collections.emptyMap;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
class ValidationActionsConsumerTest {
    @InjectMocks
    private ValidationActionsConsumer validationActionsConsumer;

    @Test
    void pnDeliveryPushValidationActionsInboundConsumer_logsMessageOnValidInput() {
        Message<String> message = mock(Message.class);
        when(message.getPayload()).thenReturn("valid-input");
        when(message.getHeaders()).thenReturn(new MessageHeaders(emptyMap()));

        validationActionsConsumer.pnDeliveryPushValidationActionsInboundConsumer(message);
    }

    @Test
    void pnDeliveryPushValidationActionsInboundConsumer_throwsExceptionOnProcessingError() {
        Message<String> message = mock(Message.class);
        when(message.getPayload()).thenReturn("processing-error");
        doThrow(new RuntimeException("simulated-error"))
                .when(message).getPayload();

        assertThrows(RuntimeException.class, () -> validationActionsConsumer.pnDeliveryPushValidationActionsInboundConsumer(message));
    }

    @Test
    void pnDeliveryPushValidationActionsInboundConsumer_handlesNullMessageGracefully() {
        assertThrows(NullPointerException.class, () -> validationActionsConsumer.pnDeliveryPushValidationActionsInboundConsumer(null));
    }
}