package it.pagopa.pn.deliverypushvalidator.action.refused;

import it.pagopa.pn.deliverypushvalidator.action.utils.TimelineUtils;
import it.pagopa.pn.deliverypushvalidator.dto.ext.delivery.notification.NotificationInt;
import it.pagopa.pn.deliverypushvalidator.dto.timeline.NotificationRefusedErrorInt;
import it.pagopa.pn.deliverypushvalidator.dto.timeline.TimelineElementInternal;
import it.pagopa.pn.deliverypushvalidator.service.NotificationService;
import it.pagopa.pn.deliverypushvalidator.service.TimelineService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InformalNotificationRefusedStrategyTest {

    @Mock
    private NotificationService notificationService;
    @Mock
    private TimelineUtils timelineUtils;
    @Mock
    private TimelineService timelineService;

    @InjectMocks
    private InformalNotificationRefusedStrategy strategy;

    @Test
    void getNotificationDelegatesToInformalNotificationService() {
        NotificationInt expected = NotificationInt.builder().iun("IUN_INF_1").build();
        when(notificationService.getInformalNotificationByIun("IUN_INF_1")).thenReturn(expected);

        NotificationInt result = strategy.getNotification("IUN_INF_1");

        assertEquals(expected, result);
        verify(notificationService, times(1)).getInformalNotificationByIun("IUN_INF_1");
    }

    @Test
    void handleNotificationRefusedBuildsRefusedTimelineWithoutCost() {
        String iun = "IUN_INF_2";
        Instant notBefore = Instant.parse("2026-05-12T10:00:00Z");
        List<NotificationRefusedErrorInt> errors = List.of(
                NotificationRefusedErrorInt.builder().errorCode("ERR").detail("detail").build()
        );
        NotificationInt notification = NotificationInt.builder().iun(iun).build();
        TimelineElementInternal timelineElement = TimelineElementInternal.builder().build();

        when(notificationService.getInformalNotificationByIun(iun)).thenReturn(notification);
        when(timelineUtils.buildRefusedRequestTimelineElement(eq(notification), eq(errors), isNull())).thenReturn(timelineElement);

        strategy.handleNotificationRefused(iun, errors, notBefore);

        verify(notificationService, times(1)).getInformalNotificationByIun(iun);
        verify(timelineUtils, times(1)).buildRefusedRequestTimelineElement(eq(notification), eq(errors), isNull());
        verify(timelineService, times(1)).addTimelineElement(timelineElement, notification);
    }
}

