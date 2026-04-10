package it.pagopa.pn.deliverypushvalidator.service.impl;

import it.pagopa.pn.deliverypushvalidator.action.utils.TimelineUtils;
import it.pagopa.pn.deliverypushvalidator.dto.ext.delivery.notification.NotificationInt;
import it.pagopa.pn.deliverypushvalidator.dto.ext.delivery.notification.NotificationRecipientInt;
import it.pagopa.pn.deliverypushvalidator.dto.ext.delivery.notification.NotificationSenderInt;
import it.pagopa.pn.deliverypushvalidator.dto.timeline.TimelineElementInternal;
import it.pagopa.pn.deliverypushvalidator.dto.timeline.details.NotificationCostValidationRequestDetailsInt;
import it.pagopa.pn.deliverypushvalidator.dto.timeline.details.TimelineElementCategoryInt;
import it.pagopa.pn.deliverypushvalidator.generated.openapi.msclient.notificationcostservice.model.NewNotificationCostRequest;
import it.pagopa.pn.deliverypushvalidator.middleware.externalclient.pnclient.notificationcostservice.NotificationCostServiceClient;
import it.pagopa.pn.deliverypushvalidator.service.TimelineService;
import it.pagopa.pn.deliverypushvalidator.service.mapper.NotificationCostServiceMapper;
import it.pagopa.pn.deliverypushvalidator.utils.NotificationCostServiceFeatureFlagUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationCostServiceImplTest {

    @Mock
    private NotificationCostServiceClient notificationCostServiceClient;
    @Mock
    private TimelineUtils timelineUtils;
    @Mock
    private NotificationCostServiceMapper notificationCostServiceMapper;
    @Mock
    private TimelineService timelineService;
    @Mock
    private NotificationCostServiceFeatureFlagUtils notificationCostServiceFeatureFlagUtils;

    private NotificationCostServiceImpl notificationCostService;

    @BeforeEach
    void setUp() {
        notificationCostService = new NotificationCostServiceImpl(
                notificationCostServiceClient,
                timelineUtils,
                notificationCostServiceMapper,
                timelineService,
                notificationCostServiceFeatureFlagUtils
        );
    }

    @Test
    void initializeAndValidateNotificationCost_shouldCallClientAndAddTimelineElement() {
        // Given
        String iun = "TEST-IUN-123";
        NotificationInt notificationInt = buildNotificationInt(iun);

        TimelineElementInternal timelineElement = buildTimelineElement(iun);
        NewNotificationCostRequest request = new NewNotificationCostRequest();

        when(timelineUtils.buildNotificationCostValidationRequest(notificationInt))
                .thenReturn(timelineElement);
        when(notificationCostServiceFeatureFlagUtils.checkNotificationCostServiceStartDate(notificationInt))
                .thenReturn(true);
        when(notificationCostServiceMapper.mapNotificationToRequest(notificationInt))
                .thenReturn(request);
        when(notificationCostServiceClient.initializeNotificationCost(iun, request))
                .thenReturn(Mono.just("success"));

        // When
        notificationCostService.initializeAndValidateNotificationCost(notificationInt);

        // Then
        verify(timelineUtils, times(1)).buildNotificationCostValidationRequest(notificationInt);
        verify(notificationCostServiceFeatureFlagUtils, times(1)).checkNotificationCostServiceStartDate(notificationInt);
        verify(notificationCostServiceMapper, times(1)).mapNotificationToRequest(notificationInt);
        verify(notificationCostServiceClient, times(1)).initializeNotificationCost(iun, request);
        verify(timelineService, times(1)).addTimelineElement(timelineElement, notificationInt);
    }

    @Test
    void initializeAndValidateNotificationCost_shouldHandleClientError() {
        // Given
        String iun = "TEST-IUN-ERROR";
        NotificationInt notificationInt = buildNotificationInt(iun);

        TimelineElementInternal timelineElement = buildTimelineElement(iun);
        NewNotificationCostRequest request = new NewNotificationCostRequest();

        RuntimeException expectedException = new RuntimeException("Client error");

        when(timelineUtils.buildNotificationCostValidationRequest(notificationInt))
                .thenReturn(timelineElement);
        when(notificationCostServiceFeatureFlagUtils.checkNotificationCostServiceStartDate(notificationInt))
                .thenReturn(true);
        when(notificationCostServiceMapper.mapNotificationToRequest(notificationInt))
                .thenReturn(request);
        when(notificationCostServiceClient.initializeNotificationCost(iun, request))
                .thenReturn(Mono.error(expectedException));

        // When & Then
        assertThrows(RuntimeException.class, () -> notificationCostService.initializeAndValidateNotificationCost(notificationInt));

        verify(timelineUtils, times(1)).buildNotificationCostValidationRequest(notificationInt);
        verify(notificationCostServiceFeatureFlagUtils, times(1)).checkNotificationCostServiceStartDate(notificationInt);
        verify(notificationCostServiceMapper, times(1)).mapNotificationToRequest(notificationInt);
        verify(notificationCostServiceClient, times(1)).initializeNotificationCost(iun, request);
        verify(timelineService, never()).addTimelineElement(any(), any());
    }

    @Test
    void initializeAndValidateNotificationCost_shouldVerifyCorrectIunPassed() {
        // Given
        String expectedIun = "EXPECTED-IUN-456";
        NotificationInt notificationInt = buildNotificationInt(expectedIun);

        TimelineElementInternal timelineElement = buildTimelineElement(expectedIun);
        NewNotificationCostRequest request = new NewNotificationCostRequest();

        when(timelineUtils.buildNotificationCostValidationRequest(notificationInt))
                .thenReturn(timelineElement);
        when(notificationCostServiceFeatureFlagUtils.checkNotificationCostServiceStartDate(notificationInt))
                .thenReturn(true);
        when(notificationCostServiceMapper.mapNotificationToRequest(notificationInt))
                .thenReturn(request);
        when(notificationCostServiceClient.initializeNotificationCost(expectedIun, request))
                .thenReturn(Mono.just("success"));

        // When
        notificationCostService.initializeAndValidateNotificationCost(notificationInt);

        // Then
        ArgumentCaptor<String> iunCaptor = ArgumentCaptor.forClass(String.class);
        verify(notificationCostServiceClient).initializeNotificationCost(iunCaptor.capture(), eq(request));
        assertEquals(expectedIun, iunCaptor.getValue());
    }

    @Test
    void initializeAndValidateNotificationCost_whenFeatureFlagDisabled_shouldSkipInitialization() {
        // Given
        String iun = "TEST-IUN-DISABLED";
        NotificationInt notificationInt = buildNotificationInt(iun);

        TimelineElementInternal timelineElement = buildTimelineElement(iun);

        when(timelineUtils.buildNotificationCostValidationRequest(notificationInt))
                .thenReturn(timelineElement);
        when(notificationCostServiceFeatureFlagUtils.checkNotificationCostServiceStartDate(notificationInt))
                .thenReturn(false);

        // When
        notificationCostService.initializeAndValidateNotificationCost(notificationInt);

        // Then
        verify(timelineUtils, times(1)).buildNotificationCostValidationRequest(notificationInt);
        verify(notificationCostServiceFeatureFlagUtils, times(1)).checkNotificationCostServiceStartDate(notificationInt);
        verify(notificationCostServiceMapper, never()).mapNotificationToRequest(any());
        verify(notificationCostServiceClient, never()).initializeNotificationCost(any(), any());
        verify(timelineService, never()).addTimelineElement(any(), any());
    }

    @Test
    void initializeAndValidateNotificationCost_whenFeatureFlagDisabledDueToNullStartDate_shouldSkipInitialization() {
        // Given
        String iun = "TEST-IUN-NULL-CONFIG";
        NotificationInt notificationInt = buildNotificationInt(iun);

        TimelineElementInternal timelineElement = buildTimelineElement(iun);

        when(timelineUtils.buildNotificationCostValidationRequest(notificationInt))
                .thenReturn(timelineElement);
        when(notificationCostServiceFeatureFlagUtils.checkNotificationCostServiceStartDate(notificationInt))
                .thenReturn(false); // Returns false when start date is null

        // When
        notificationCostService.initializeAndValidateNotificationCost(notificationInt);

        // Then
        verify(timelineUtils, times(1)).buildNotificationCostValidationRequest(notificationInt);
        verify(notificationCostServiceFeatureFlagUtils, times(1)).checkNotificationCostServiceStartDate(notificationInt);
        verify(notificationCostServiceMapper, never()).mapNotificationToRequest(any());
        verify(notificationCostServiceClient, never()).initializeNotificationCost(any(), any());
        verify(timelineService, never()).addTimelineElement(any(), any());
    }

    @Test
    void initializeAndValidateNotificationCost_whenFeatureFlagThrowsException_shouldPropagateException() {
        // Given
        String iun = "TEST-IUN-FF-ERROR";
        NotificationInt notificationInt = buildNotificationInt(iun);

        TimelineElementInternal timelineElement = buildTimelineElement(iun);
        NullPointerException expectedException = new NullPointerException("Notification sentAt cannot be null");

        when(timelineUtils.buildNotificationCostValidationRequest(notificationInt))
                .thenReturn(timelineElement);
        when(notificationCostServiceFeatureFlagUtils.checkNotificationCostServiceStartDate(notificationInt))
                .thenThrow(expectedException);

        // When & Then
        assertThrows(NullPointerException.class, () ->
                notificationCostService.initializeAndValidateNotificationCost(notificationInt));

        verify(timelineUtils, times(1)).buildNotificationCostValidationRequest(notificationInt);
        verify(notificationCostServiceFeatureFlagUtils, times(1)).checkNotificationCostServiceStartDate(notificationInt);
        verify(notificationCostServiceMapper, never()).mapNotificationToRequest(any());
        verify(notificationCostServiceClient, never()).initializeNotificationCost(any(), any());
        verify(timelineService, never()).addTimelineElement(any(), any());
    }

    @Test
    void initializeAndValidateNotificationCost_whenMapperFails_shouldPropagateException() {
        // Given
        String iun = "TEST-IUN-MAPPER-ERROR";
        NotificationInt notificationInt = buildNotificationInt(iun);

        TimelineElementInternal timelineElement = buildTimelineElement(iun);
        IllegalStateException expectedException = new IllegalStateException("Mapping error");

        when(timelineUtils.buildNotificationCostValidationRequest(notificationInt))
                .thenReturn(timelineElement);
        when(notificationCostServiceFeatureFlagUtils.checkNotificationCostServiceStartDate(notificationInt))
                .thenReturn(true);
        when(notificationCostServiceMapper.mapNotificationToRequest(notificationInt))
                .thenThrow(expectedException);

        // When & Then
        assertThrows(IllegalStateException.class, () ->
                notificationCostService.initializeAndValidateNotificationCost(notificationInt));

        verify(timelineUtils, times(1)).buildNotificationCostValidationRequest(notificationInt);
        verify(notificationCostServiceFeatureFlagUtils, times(1)).checkNotificationCostServiceStartDate(notificationInt);
        verify(notificationCostServiceMapper, times(1)).mapNotificationToRequest(notificationInt);
        verify(notificationCostServiceClient, never()).initializeNotificationCost(any(), any());
        verify(timelineService, never()).addTimelineElement(any(), any());
    }

    @Test
    void initializeAndValidateNotificationCost_whenTimelineServiceFails_shouldNotThrowException() {
        // Given
        String iun = "TEST-IUN-TIMELINE-ERROR";
        NotificationInt notificationInt = buildNotificationInt(iun);

        TimelineElementInternal timelineElement = buildTimelineElement(iun);
        NewNotificationCostRequest request = new NewNotificationCostRequest();
        RuntimeException timelineException = new RuntimeException("Timeline error");

        when(timelineUtils.buildNotificationCostValidationRequest(notificationInt))
                .thenReturn(timelineElement);
        when(notificationCostServiceFeatureFlagUtils.checkNotificationCostServiceStartDate(notificationInt))
                .thenReturn(true);
        when(notificationCostServiceMapper.mapNotificationToRequest(notificationInt))
                .thenReturn(request);
        when(notificationCostServiceClient.initializeNotificationCost(iun, request))
                .thenReturn(Mono.just("success"));
        doThrow(timelineException).when(timelineService).addTimelineElement(timelineElement, notificationInt);

        // When & Then
        assertThrows(RuntimeException.class, () ->
                notificationCostService.initializeAndValidateNotificationCost(notificationInt));

        verify(timelineUtils, times(1)).buildNotificationCostValidationRequest(notificationInt);
        verify(notificationCostServiceFeatureFlagUtils, times(1)).checkNotificationCostServiceStartDate(notificationInt);
        verify(notificationCostServiceMapper, times(1)).mapNotificationToRequest(notificationInt);
        verify(notificationCostServiceClient, times(1)).initializeNotificationCost(iun, request);
        verify(timelineService, times(1)).addTimelineElement(timelineElement, notificationInt);
    }

    private NotificationInt buildNotificationInt(String iun) {
        return NotificationInt.builder()
                .iun(iun)
                .sender(NotificationSenderInt.builder()
                        .paId("testPaId")
                        .paTaxId("testTaxId")
                        .paDenomination("Test PA")
                        .build())
                .recipients(Collections.singletonList(
                        NotificationRecipientInt.builder()
                                .internalId("testRecipient")
                                .taxId("RSSMRA80A01H501U")
                                .build()
                ))
                .sentAt(Instant.now())
                .documents(Collections.emptyList())
                .build();
    }

    private TimelineElementInternal buildTimelineElement(String iun) {
        return TimelineElementInternal.builder()
                .iun(iun)
                .elementId("NOTIFICATION_COST_VALIDATION_REQUEST")
                .category(TimelineElementCategoryInt.NOTIFICATION_COST_VALIDATION_REQUEST)
                .timestamp(Instant.now())
                .details(NotificationCostValidationRequestDetailsInt.builder().build())
                .build();
    }
}
