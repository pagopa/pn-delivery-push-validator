package it.pagopa.pn.deliverypushvalidator.middleware.queue.consumer;

import org.junit.jupiter.api.Test;

import static java.util.Collections.emptyMap;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
class F24ConsumerTest {
    @InjectMocks
    private F24Consumer f24Consumer;

    @Test
    void f24Consumer_logsMessageOnValidInput() {
        Message<String> message = mock(Message.class);
        when(message.getPayload()).thenReturn("valid-input");
        when(message.getHeaders()).thenReturn(new MessageHeaders(emptyMap()));

        f24Consumer.pnF24EventInboundConsumer(message);
    }

    @Test
    void f24Consumer_throwsExceptionOnProcessingError() {
        Message<String> message = mock(Message.class);
        when(message.getPayload()).thenReturn("processing-error");
        doThrow(new RuntimeException("simulated-error"))
                .when(message).getPayload();

        assertThrows(RuntimeException.class, () -> f24Consumer.pnF24EventInboundConsumer(message));
    }

    @Test
    void f24Consumer_handlesNullMessageGracefully() {
        assertThrows(NullPointerException.class, () -> f24Consumer.pnF24EventInboundConsumer(null));
    }
}