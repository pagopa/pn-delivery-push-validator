package it.pagopa.pn.deliverypushvalidator.middleware.queue.consumer;

import it.pagopa.pn.deliverypushvalidator.generated.openapi.msclient.addressmanager.model.NormalizeItemsResult;
import it.pagopa.pn.deliverypushvalidator.middleware.responsehandler.AddressManagerResponseHandler;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
class AddressManagerConsumerTestIT {

    @Mock
    private AddressManagerResponseHandler handler;

    @InjectMocks
    private AddressManagerConsumer consumer;

    @Test
    void consumeMessageOK() {
        NormalizeItemsResult result = new NormalizeItemsResult();

        Message<NormalizeItemsResult> message = MessageBuilder.withPayload(result)
                .setHeader("test", "headerValue")
                .build();

        consumer.pnAddressManagerEventInboundConsumer(message);

        Mockito.verify(handler, Mockito.times(1)).handleResponseReceived(result);
    }

    @Test
    void consumeMessageKO() {
        NormalizeItemsResult result = new NormalizeItemsResult();

        Message<NormalizeItemsResult> message = MessageBuilder.withPayload(result)
                .setHeader("test", "headerValue")
                .build();

        Mockito.doThrow(new RuntimeException("Test Exception")).when(handler).handleResponseReceived(Mockito.any());

        Assertions.assertThrows(RuntimeException.class, () -> consumer.pnAddressManagerEventInboundConsumer(message));

        Mockito.verify(handler, Mockito.times(1)).handleResponseReceived(result);
    }
}
