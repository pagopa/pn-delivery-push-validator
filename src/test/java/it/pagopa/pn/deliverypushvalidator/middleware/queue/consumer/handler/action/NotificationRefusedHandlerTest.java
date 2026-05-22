package it.pagopa.pn.deliverypushvalidator.middleware.queue.consumer.handler.action;

import it.pagopa.pn.deliverypushvalidator.action.details.NotificationRefusedActionDetails;
import it.pagopa.pn.deliverypushvalidator.action.refused.NotificationRefusedActionHandler;
import it.pagopa.pn.deliverypushvalidator.action.utils.TimelineUtils;
import it.pagopa.pn.deliverypushvalidator.dto.ext.delivery.notification.CommunicationType;
import it.pagopa.pn.deliverypushvalidator.dto.timeline.NotificationRefusedErrorInt;
import it.pagopa.pn.deliverypushvalidator.middleware.queue.consumer.router.SupportedEventType;
import it.pagopa.pn.deliverypushvalidator.middleware.queue.producer.abstractions.actionspool.Action;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.MessageHeaders;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class NotificationRefusedHandlerTest {
    @Mock
    private TimelineUtils timelineUtils;

    @Mock
    private NotificationRefusedActionHandler notificationRefusedActionHandler;

    @Mock
    private MessageHeaders headers;

    @InjectMocks
    private NotificationRefusedHandler handler;

    @Test
    void getSupportedEventTypeReturnsCorrectType() {
        Assertions.assertEquals(SupportedEventType.NOTIFICATION_REFUSED, handler.getSupportedEventType());
    }

    @Test
    void handleExecutesRefusal() {
        NotificationRefusedActionDetails details = NotificationRefusedActionDetails.builder()
                .errors(List.of(NotificationRefusedErrorInt.builder()
                        .errorCode("error_code")
                        .detail("error_detail")
                        .build()))
                .build();
        Instant notBefore = Instant.now();
        Action action = Action.builder()
                .iun("iun_123")
                .details(details)
                .notBefore(notBefore)
                .communicationType(CommunicationType.INFORMAL)
                .build();

        handler.handle(action, headers);

        Mockito.verify(notificationRefusedActionHandler).notificationRefusedHandler("iun_123", details.getErrors(), notBefore, CommunicationType.INFORMAL);
        Mockito.verify(timelineUtils, Mockito.never()).checkIsNotificationCancellationRequested("iun_123");
    }

    @Test
    void handleLogsAndThrowsExceptionOnError() {
        NotificationRefusedActionDetails details = NotificationRefusedActionDetails.builder()
                .errors(List.of(NotificationRefusedErrorInt.builder()
                        .errorCode("error_code")
                        .detail("error_detail")
                        .build()))
                .build();
        Instant notBefore = Instant.now();
        Action action = Action.builder()
                .iun("iun_123")
                .details(details)
                .notBefore(notBefore)
                .communicationType(CommunicationType.INFORMAL)
                .build();

        Mockito.doThrow(new RuntimeException("Validation error")).when(notificationRefusedActionHandler).notificationRefusedHandler("iun_123", details.getErrors(), notBefore, CommunicationType.INFORMAL);

        assertThrows(RuntimeException.class, () -> handler.handle(action, headers));

        Mockito.verify(notificationRefusedActionHandler).notificationRefusedHandler("iun_123", details.getErrors(), notBefore, CommunicationType.INFORMAL);
        Mockito.verify(timelineUtils, Mockito.never()).checkIsNotificationCancellationRequested("iun_123");
    }

}