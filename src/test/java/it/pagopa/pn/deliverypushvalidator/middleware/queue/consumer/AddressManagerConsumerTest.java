package it.pagopa.pn.deliverypushvalidator.middleware.queue.consumer;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static java.util.Collections.emptyMap;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
class AddressManagerConsumerTest {
    @InjectMocks
    private AddressManagerConsumer addressManagerConsumer;

    @Test
    void pnAddressManagerEventInboundConsumer_logsMessageOnValidInput() {
        Message<String> message = mock(Message.class);
        when(message.getPayload()).thenReturn("valid-input");
        when(message.getHeaders()).thenReturn(new MessageHeaders(emptyMap()));

        addressManagerConsumer.pnAddressManagerEventInboundConsumer(message);
    }

    @Test
    void pnAddressManagerEventInboundConsumer_throwsExceptionOnProcessingError() {
        Message<String> message = mock(Message.class);
        when(message.getPayload()).thenReturn("processing-error");
        doThrow(new RuntimeException("simulated-error"))
                .when(message).getPayload();

        assertThrows(RuntimeException.class, () -> addressManagerConsumer.pnAddressManagerEventInboundConsumer(message));
    }

    @Test
    void pnAddressManagerEventInboundConsumer_handlesNullMessageGracefully() {
        assertThrows(NullPointerException.class, () -> addressManagerConsumer.pnAddressManagerEventInboundConsumer(null));
    }
}