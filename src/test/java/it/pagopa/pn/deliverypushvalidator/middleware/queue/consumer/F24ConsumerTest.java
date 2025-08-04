package it.pagopa.pn.deliverypushvalidator.middleware.queue.consumer;

import it.pagopa.pn.api.dto.events.DetailedTypePayload;
import it.pagopa.pn.api.dto.events.PnF24PdfSetReadyEvent;
import it.pagopa.pn.deliverypushvalidator.middleware.responsehandler.F24ResponseHandler;
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


@ExtendWith(SpringExtension.class)
class F24ConsumerTest {

    @Mock
    private F24ResponseHandler handler;

    @InjectMocks
    private F24Consumer f24Consumer;


    @Test
    void pnF24EventInboundConsumerOk() {
        DetailedTypePayload event = new PnF24PdfSetReadyEvent.Detail();
        Message<DetailedTypePayload> message = MessageBuilder.withPayload(event)
                .setHeader("test", "headerValue")
                .build();
        f24Consumer.pnF24EventInboundConsumer(message);

        Mockito.verify(handler, Mockito.times(1)).handleEventF24(event);
    }

    @Test
    void handlePrepareResponseReceivedKO() {
        DetailedTypePayload event = new PnF24PdfSetReadyEvent.Detail();
        Message<DetailedTypePayload> message = MessageBuilder.withPayload(event)
                .setHeader("test", "headerValue")
                .build();


        Mockito.doThrow(new RuntimeException("Test Exception")).when(handler).handleEventF24(any());

        Assertions.assertThrows(RuntimeException.class, () -> f24Consumer.pnF24EventInboundConsumer(message));

        Mockito.verify(handler, Mockito.times(1)).handleEventF24(event);
    }
}
