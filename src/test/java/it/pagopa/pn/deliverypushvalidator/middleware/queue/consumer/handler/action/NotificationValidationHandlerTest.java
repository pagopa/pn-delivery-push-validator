package it.pagopa.pn.deliverypushvalidator.middleware.queue.consumer.handler.action;

import it.pagopa.pn.deliverypushvalidator.action.details.NotificationValidationActionDetails;
import it.pagopa.pn.deliverypushvalidator.action.startworkflow.notificationvalidation.NotificationValidationActionHandler;
import it.pagopa.pn.deliverypushvalidator.action.utils.TimelineUtils;
import it.pagopa.pn.deliverypushvalidator.dto.ext.delivery.notification.CommunicationType;
import it.pagopa.pn.deliverypushvalidator.middleware.queue.consumer.router.SupportedEventType;
import it.pagopa.pn.deliverypushvalidator.middleware.queue.producer.abstractions.actionspool.Action;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.MessageHeaders;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class NotificationValidationHandlerTest {

    @Mock
    private TimelineUtils timelineUtils;

    @Mock
    private NotificationValidationActionHandler notificationValidationActionHandler;

    @Mock
    private MessageHeaders headers;

    @InjectMocks
    private NotificationValidationHandler handler;

    @Test
    void getSupportedEventTypeReturnsCorrectType() {
        assertEquals(SupportedEventType.NOTIFICATION_VALIDATION, handler.getSupportedEventType());
    }

    @Test
    void handleExecutesValidationWhenNotificationNotCancelled() {
        NotificationValidationActionDetails details = new NotificationValidationActionDetails();
        Action action = Action.builder()
                .iun("iun_123")
                .communicationType(CommunicationType.LEGAL)
                .details(details)
                .build();

        Mockito.when(timelineUtils.checkIsNotificationCancellationRequested("iun_123", CommunicationType.LEGAL)).thenReturn(false);

        handler.handle(action, headers);

        Mockito.verify(notificationValidationActionHandler).validateNotification("iun_123", details, CommunicationType.LEGAL);
        Mockito.verify(timelineUtils).checkIsNotificationCancellationRequested("iun_123", CommunicationType.LEGAL);
    }

    @Test
    void handleDoesNotExecuteValidationWhenNotificationCancelled() {
        Action action = Action.builder().iun("iun_123").communicationType(CommunicationType.LEGAL).build();

        Mockito.when(timelineUtils.checkIsNotificationCancellationRequested("iun_123", CommunicationType.LEGAL)).thenReturn(true);

        handler.handle(action, headers);

        Mockito.verify(notificationValidationActionHandler, Mockito.never()).validateNotification(Mockito.anyString(), Mockito.any(), Mockito.isNull());
        Mockito.verify(timelineUtils).checkIsNotificationCancellationRequested("iun_123", CommunicationType.LEGAL);
    }

    @Test
    void handleLogsAndThrowsExceptionOnError() {
        NotificationValidationActionDetails details = new NotificationValidationActionDetails();
        Action action = Action.builder()
                .iun("iun_123")
                .communicationType(CommunicationType.LEGAL)
                .details(details)
                .build();

        Mockito.when(timelineUtils.checkIsNotificationCancellationRequested("iun_123", CommunicationType.LEGAL)).thenReturn(false);
        Mockito.doThrow(new RuntimeException("Validation error")).when(notificationValidationActionHandler).validateNotification("iun_123", details, CommunicationType.LEGAL);

        assertThrows(RuntimeException.class, () -> handler.handle(action, headers));

        Mockito.verify(notificationValidationActionHandler).validateNotification("iun_123", details, CommunicationType.LEGAL);
        Mockito.verify(timelineUtils).checkIsNotificationCancellationRequested("iun_123", CommunicationType.LEGAL);
    }
}