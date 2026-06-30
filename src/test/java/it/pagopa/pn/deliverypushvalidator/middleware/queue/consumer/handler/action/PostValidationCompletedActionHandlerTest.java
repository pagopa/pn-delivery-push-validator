package it.pagopa.pn.deliverypushvalidator.middleware.queue.consumer.handler.action;

import it.pagopa.pn.deliverypushvalidator.action.startworkflow.PostValidationCompletedHandler;
import it.pagopa.pn.deliverypushvalidator.action.utils.TimelineUtils;
import it.pagopa.pn.deliverypushvalidator.middleware.queue.consumer.router.SupportedEventType;
import it.pagopa.pn.deliverypushvalidator.middleware.queue.producer.abstractions.actionspool.Action;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.MessageHeaders;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class PostValidationCompletedActionHandlerTest {

    @Mock
    private TimelineUtils timelineUtils;

    @Mock
    private PostValidationCompletedHandler postValidationCompletedHandler;

    @Mock
    private MessageHeaders headers;

    @InjectMocks
    private PostValidationCompletedActionHandler handler;

    @Test
    void getSupportedEventTypeReturnsCorrectType() {
        assertEquals(SupportedEventType.POST_VALIDATION_COMPLETED, handler.getSupportedEventType());
    }

    @Test
    void handleCallsAcceptNotificationWhenNotificationIsNotCancelled() {
        Action action = Action.builder().iun("IUN_123").build();
        Mockito.when(timelineUtils.checkIsNotificationCancellationRequested("IUN_123")).thenReturn(false);

        handler.handle(action, headers);

        Mockito.verify(postValidationCompletedHandler).acceptNotification("IUN_123");
    }

    @Test
    void handleSkipsAcceptNotificationWhenNotificationIsCancelled() {
        Action action = Action.builder().iun("IUN_123").build();
        Mockito.when(timelineUtils.checkIsNotificationCancellationRequested("IUN_123")).thenReturn(true);

        handler.handle(action, headers);

        Mockito.verify(postValidationCompletedHandler, Mockito.never()).acceptNotification(Mockito.anyString());
    }
}

