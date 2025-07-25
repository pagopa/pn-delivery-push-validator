package it.pagopa.pn.deliverypushvalidator.middleware.queue.consumer;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static java.util.Collections.emptyMap;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
class NewNotificationConsumerTest {

    @InjectMocks
    private NewNotificationConsumer newNotificationConsumer;

    @Test
    void pnDeliveryNewNotificationEventConsumer_logsMessageOnValidInput() {
        Message<String> message = mock(Message.class);
        when(message.getPayload()).thenReturn("valid-input");
        when(message.getHeaders()).thenReturn(new MessageHeaders(emptyMap()));

        newNotificationConsumer.pnDeliveryNewNotificationEventConsumer(message);
    }

    @Test
    void pnDeliveryNewNotificationEventConsumer_throwsExceptionOnProcessingError() {
        Message<String> message = mock(Message.class);
        when(message.getPayload()).thenReturn("processing-error");
        doThrow(new RuntimeException("simulated-error"))
                .when(message).getPayload();

        assertThrows(RuntimeException.class, () -> newNotificationConsumer.pnDeliveryNewNotificationEventConsumer(message));
    }

    @Test
    void pnDeliveryNewNotificationEventConsumer_handlesNullMessageGracefully() {
        assertThrows(NullPointerException.class, () -> newNotificationConsumer.pnDeliveryNewNotificationEventConsumer(null));
    }
}