package it.pagopa.pn.deliverypushvalidator.middleware.queue.consumer;

import it.pagopa.pn.api.dto.events.PnDeliveryNewNotificationEvent;
import it.pagopa.pn.deliverypushvalidator.middleware.queue.consumer.handler.utils.HandleEventUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InformalValidationConsumerTest {

    private InformalValidationConsumer consumer;

    @BeforeEach
    void setUp() {
        consumer = new InformalValidationConsumer();
    }


    @Test
    void testInformalValidationInputsEventConsumer_ok() {
        // Arrange
        PnDeliveryNewNotificationEvent.Payload payload = mock(PnDeliveryNewNotificationEvent.Payload.class);
        when(payload.getIun()).thenReturn("TEST_IUN");

        Message<PnDeliveryNewNotificationEvent.Payload> message =
                MessageBuilder.withPayload(payload).build();

        try (MockedStatic<HandleEventUtils> utilsMock = mockStatic(HandleEventUtils.class);
             MockedStatic<it.pagopa.pn.deliverypushvalidator.middleware.queue.utils.ChannelUtils> channelMock =
                     mockStatic(it.pagopa.pn.deliverypushvalidator.middleware.queue.utils.ChannelUtils.class)) {

            assertDoesNotThrow(() ->
                    consumer.informalValidationInputsEventConsumer(message)
            );

            utilsMock.verify(() -> HandleEventUtils.addIunToMdc("TEST_IUN"));
        }
    }

    @Test
    void testInformalValidationInputsEventConsumer_exception() {
        // Arrange
        PnDeliveryNewNotificationEvent.Payload payload = mock(PnDeliveryNewNotificationEvent.Payload.class);
        when(payload.getIun()).thenThrow(new RuntimeException("boom"));

        Message<PnDeliveryNewNotificationEvent.Payload> message =
                MessageBuilder.withPayload(payload).build();

        try (MockedStatic<HandleEventUtils> utilsMock = mockStatic(HandleEventUtils.class);
             MockedStatic<it.pagopa.pn.deliverypushvalidator.middleware.queue.utils.ChannelUtils> channelMock =
                     mockStatic(it.pagopa.pn.deliverypushvalidator.middleware.queue.utils.ChannelUtils.class)) {

            assertThrows(RuntimeException.class, () ->
                    consumer.informalValidationInputsEventConsumer(message)
            );

            utilsMock.verify(() ->
                    HandleEventUtils.handleException(any(), any())
            );
        }
    }
}
