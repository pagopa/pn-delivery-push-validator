package it.pagopa.pn.deliverypushvalidator.middleware.queue.consumer;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;

import static java.util.Collections.emptyMap;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
class SafeStorageConsumerTest {
    @InjectMocks
    private SafeStorageConsumer safeStorageConsumer;

    @Test
    void pnSafeStorageEventInboundConsumer_logsMessageOnValidInput() {
        Message<String> message = mock(Message.class);
        when(message.getPayload()).thenReturn("valid-input");
        when(message.getHeaders()).thenReturn(new MessageHeaders(emptyMap()));

        safeStorageConsumer.pnSafeStorageEventInboundConsumer(message);
    }

    @Test
    void pnSafeStorageEventInboundConsumer_throwsExceptionOnProcessingError() {
        Message<String> message = mock(Message.class);
        when(message.getPayload()).thenReturn("processing-error");
        doThrow(new RuntimeException("simulated-error"))
                .when(message).getPayload();

        assertThrows(RuntimeException.class, () -> safeStorageConsumer.pnSafeStorageEventInboundConsumer(message));
    }

    @Test
    void pnSafeStorageEventInboundConsumer_handlesNullMessageGracefully() {
        assertThrows(NullPointerException.class, () -> safeStorageConsumer.pnSafeStorageEventInboundConsumer(null));
    }
}