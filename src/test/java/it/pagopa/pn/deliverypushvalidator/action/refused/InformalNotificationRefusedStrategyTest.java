package it.pagopa.pn.deliverypushvalidator.action.refused;

import it.pagopa.pn.deliverypushvalidator.action.utils.TimelineUtils;
import it.pagopa.pn.deliverypushvalidator.dto.ext.delivery.notification.NotificationInt;
import it.pagopa.pn.deliverypushvalidator.dto.ext.delivery.notification.PagoPaIntMode;
import it.pagopa.pn.deliverypushvalidator.dto.timeline.NotificationRefusedErrorInt;
import it.pagopa.pn.deliverypushvalidator.dto.timeline.TimelineElementInternal;
import it.pagopa.pn.deliverypushvalidator.generated.openapi.msclient.delivery.model.NotificationFeePolicy;
import it.pagopa.pn.deliverypushvalidator.service.NotificationProcessCostService;
import it.pagopa.pn.deliverypushvalidator.service.NotificationService;
import it.pagopa.pn.deliverypushvalidator.service.TimelineService;
import it.pagopa.pn.deliverypushvalidator.utils.RefusalCostCalculator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InformalNotificationRefusedStrategyTest {

    @Mock
    private NotificationService notificationService;
    @Mock
    private TimelineUtils timelineUtils;
    @Mock
    private TimelineService timelineService;
    @Mock
    private NotificationProcessCostService notificationProcessCostService;
    @Mock
    private RefusalCostCalculator refusalCostCalculator;

    @InjectMocks
    private InformalNotificationRefusedStrategy strategy;

    @Test
    void getNotificationDelegatesToInformalNotificationService() {
        NotificationInt expected = NotificationInt.builder().iun("IUN_INF_1").build();
        when(notificationService.getInformalNotificationByIun("IUN_INF_1")).thenReturn(expected);

        NotificationInt result = strategy.getNotification("IUN_INF_1");

        assertEquals(expected, result);
        verify(notificationService, times(1)).getInformalNotificationByIun("IUN_INF_1");
        verify(notificationService, never()).getNotificationByIun("IUN_INF_1");
    }

    @Test
    void handleNotificationRefusedAddsTimelineWithCalculatedCost() {
        // Given
        String iun = "IUN_INF_2";
        Instant notBefore = Instant.parse("2026-05-12T10:00:00Z");
        List<NotificationRefusedErrorInt> errors = List.of(
                NotificationRefusedErrorInt.builder().errorCode("ERR").detail("detail").build()
        );
        NotificationInt notification = NotificationInt.builder().iun(iun).build();
        TimelineElementInternal timelineElement = TimelineElementInternal.builder().build();

        when(notificationService.getInformalNotificationByIun(iun)).thenReturn(notification);
        when(refusalCostCalculator.calculateRefusalCost(notification, errors)).thenReturn(13);
        when(timelineUtils.buildRefusedRequestTimelineElement(notification, errors, 13)).thenReturn(timelineElement);

        // When
        strategy.handleNotificationRefused(iun, errors, notBefore);

        // Then
        verify(notificationService, times(1)).getInformalNotificationByIun(iun);
        verify(refusalCostCalculator, times(1)).calculateRefusalCost(notification, errors);
        verify(timelineUtils, times(1)).buildRefusedRequestTimelineElement(notification, errors, 13);
        verify(timelineService, times(1)).addTimelineElement(timelineElement, notification);
    }

    @Test
    void handleNotificationRefusedNeverUpdatesNotificationCost() {
        // Given - anche con DELIVERY_MODE + ASYNC (caso in cui la legal aggiornerebbe il costo) l'informale non lo fa
        String iun = "IUN_INF_3";
        Instant notBefore = Instant.parse("2026-05-12T11:00:00Z");
        List<NotificationRefusedErrorInt> errors = List.of(
                NotificationRefusedErrorInt.builder().errorCode("ERR").detail("detail").build()
        );
        NotificationInt notification = NotificationInt.builder()
                .iun(iun)
                .notificationFeePolicy(NotificationFeePolicy.DELIVERY_MODE)
                .pagoPaIntMode(PagoPaIntMode.ASYNC)
                .build();
        TimelineElementInternal timelineElement = TimelineElementInternal.builder().build();

        when(notificationService.getInformalNotificationByIun(iun)).thenReturn(notification);
        when(refusalCostCalculator.calculateRefusalCost(notification, errors)).thenReturn(5);
        when(timelineUtils.buildRefusedRequestTimelineElement(notification, errors, 5)).thenReturn(timelineElement);

        // When
        strategy.handleNotificationRefused(iun, errors, notBefore);

        // Then
        verifyNoInteractions(notificationProcessCostService);
        verify(timelineService, times(1)).addTimelineElement(timelineElement, notification);
    }

    @Test
    void handleNotificationRefusedWithMultipleErrors() {
        // Given
        String iun = "IUN_INF_4";
        Instant notBefore = Instant.parse("2026-05-12T12:00:00Z");
        List<NotificationRefusedErrorInt> errors = List.of(
                NotificationRefusedErrorInt.builder().errorCode("E01").detail("detail1").build(),
                NotificationRefusedErrorInt.builder().errorCode("E02").detail("detail2").build(),
                NotificationRefusedErrorInt.builder().errorCode("E03").detail("detail3").build()
        );
        NotificationInt notification = NotificationInt.builder().iun(iun).build();
        TimelineElementInternal timelineElement = TimelineElementInternal.builder().build();

        when(notificationService.getInformalNotificationByIun(iun)).thenReturn(notification);
        when(refusalCostCalculator.calculateRefusalCost(notification, errors)).thenReturn(15);
        when(timelineUtils.buildRefusedRequestTimelineElement(notification, errors, 15)).thenReturn(timelineElement);

        // When
        strategy.handleNotificationRefused(iun, errors, notBefore);

        // Then
        verify(refusalCostCalculator, times(1)).calculateRefusalCost(notification, errors);
        verify(timelineUtils, times(1)).buildRefusedRequestTimelineElement(notification, errors, 15);
        verify(timelineService, times(1)).addTimelineElement(timelineElement, notification);
    }
}