package it.pagopa.pn.deliverypushvalidator.service.impl;

import it.pagopa.pn.commons.exceptions.PnInternalException;
import it.pagopa.pn.deliverypushvalidator.action.utils.TimelineUtils;
import it.pagopa.pn.deliverypushvalidator.dto.ext.delivery.notification.*;
import it.pagopa.pn.deliverypushvalidator.dto.timeline.TimelineElementInternal;
import it.pagopa.pn.deliverypushvalidator.dto.timeline.details.NotificationCostValidationRequestDetailsInt;
import it.pagopa.pn.deliverypushvalidator.dto.timeline.details.TimelineElementCategoryInt;
import it.pagopa.pn.deliverypushvalidator.generated.openapi.msclient.notificationcostservice.model.NewInformalNotificationCostRequest;
import it.pagopa.pn.deliverypushvalidator.generated.openapi.msclient.notificationcostservice.model.NewNotificationCostRequest;
import it.pagopa.pn.deliverypushvalidator.middleware.externalclient.pnclient.notificationcostservice.NotificationCostServiceClient;
import it.pagopa.pn.deliverypushvalidator.service.TimelineService;
import it.pagopa.pn.deliverypushvalidator.service.mapper.NotificationCostServiceMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
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

    private NotificationCostServiceImpl notificationCostService;

    @BeforeEach
    void setUp() {
        notificationCostService = new NotificationCostServiceImpl(
                notificationCostServiceClient,
                timelineUtils,
                notificationCostServiceMapper,
                timelineService
        );
    }

    @Test
    void initializeAndValidateNotificationCost_shouldCallLegalClientAndAddTimelineElement() {
        // Given
        String iun = "TEST-IUN-123";
        NotificationInt notificationInt = buildNotificationInt(iun, CommunicationType.LEGAL);

        TimelineElementInternal timelineElement = buildTimelineElement(iun);
        NewNotificationCostRequest request = new NewNotificationCostRequest();

        when(timelineUtils.buildNotificationCostValidationRequest(notificationInt))
                .thenReturn(timelineElement);
        when(notificationCostServiceMapper.mapNotificationToNewNotificationCostRequest(notificationInt))
                .thenReturn(request);
        when(notificationCostServiceClient.initializeNotificationCost(iun, request))
                .thenReturn(Mono.just("success"));

        // When
        notificationCostService.initializeAndValidateNotificationCost(notificationInt);

        // Then
        verify(timelineUtils, times(1)).buildNotificationCostValidationRequest(notificationInt);
        verify(notificationCostServiceMapper, times(1)).mapNotificationToNewNotificationCostRequest(notificationInt);
        verify(notificationCostServiceClient, times(1)).initializeNotificationCost(iun, request);
        verify(timelineService, times(1)).addTimelineElement(timelineElement, notificationInt);
    }

    @Test
    void initializeAndValidateNotificationCost_shouldHandleClientError() {
        // Given
        String iun = "TEST-IUN-ERROR";
        NotificationInt notificationInt = buildNotificationInt(iun, CommunicationType.LEGAL);

        TimelineElementInternal timelineElement = buildTimelineElement(iun);
        NewNotificationCostRequest request = new NewNotificationCostRequest();

        RuntimeException expectedException = new RuntimeException("Client error");

        when(timelineUtils.buildNotificationCostValidationRequest(notificationInt))
                .thenReturn(timelineElement);
        when(notificationCostServiceMapper.mapNotificationToNewNotificationCostRequest(notificationInt))
                .thenReturn(request);
        when(notificationCostServiceClient.initializeNotificationCost(iun, request))
                .thenReturn(Mono.error(expectedException));

        // When & Then
        assertThrows(RuntimeException.class, () -> notificationCostService.initializeAndValidateNotificationCost(notificationInt));

        verify(timelineUtils, times(1)).buildNotificationCostValidationRequest(notificationInt);
        verify(notificationCostServiceMapper, times(1)).mapNotificationToNewNotificationCostRequest(notificationInt);
        verify(notificationCostServiceClient, times(1)).initializeNotificationCost(iun, request);
        verify(timelineService, never()).addTimelineElement(any(), any());
    }

    @Test
    void initializeAndValidateNotificationCost_shouldCallInformalClientAndAddTimelineElement() {
        // Given
        String iun = "TEST-IUN-123";
        NotificationInt notificationInt = buildNotificationInt(iun, CommunicationType.INFORMAL);

        TimelineElementInternal timelineElement = buildTimelineElement(iun);
        NewInformalNotificationCostRequest request = new NewInformalNotificationCostRequest();

        when(timelineUtils.buildNotificationCostValidationRequest(notificationInt))
                .thenReturn(timelineElement);
        when(notificationCostServiceMapper.mapNotificationToNewInformalNotificationCostRequest(notificationInt))
                .thenReturn(request);
        when(notificationCostServiceClient.initializeInformalNotificationCost(iun, request))
                .thenReturn(Mono.just("success"));

        // When
        notificationCostService.initializeAndValidateNotificationCost(notificationInt);

        // Then
        verify(timelineUtils, times(1)).buildNotificationCostValidationRequest(notificationInt);
        verify(notificationCostServiceMapper, times(1)).mapNotificationToNewInformalNotificationCostRequest(notificationInt);
        verify(notificationCostServiceClient, times(1)).initializeInformalNotificationCost(iun, request);
        verify(timelineService, times(1)).addTimelineElement(timelineElement, notificationInt);
    }

    @Test
    void initializeAndValidateNotificationCost_shouldThrowErrorWhenCommunicationTypeIsNull() {
        // Given
        String iun = "TEST-IUN-123";
        NotificationInt notificationInt = buildNotificationInt(iun, null);

        TimelineElementInternal timelineElement = buildTimelineElement(iun);
        when(timelineUtils.buildNotificationCostValidationRequest(notificationInt))
                .thenReturn(timelineElement);

        // When
        Assertions.assertThrows(NullPointerException.class, () -> notificationCostService.initializeAndValidateNotificationCost(notificationInt));

        // Then
        verify(timelineUtils, times(1)).buildNotificationCostValidationRequest(notificationInt);
        verifyNoInteractions(notificationCostServiceMapper);
        verifyNoInteractions(notificationCostServiceClient);
        verifyNoInteractions(timelineService);
    }

    @Test
    void shouldValidateInformalNotificationCost_shouldReturnTrueWhenAtLeastOneRecipientHasPagoPaPayment() {
        NotificationInt notificationInt = buildNotificationInt(
                "TEST-IUN-1",
                CommunicationType.INFORMAL,
                List.of(
                        NotificationRecipientInt.builder()
                                .internalId("recipient-1")
                                .payments(List.of(
                                        NotificationPaymentInfoInt.builder()
                                                .pagoPA(new PagoPaInt())
                                                .build()
                                ))
                                .build()
                )
        );

        boolean result = notificationCostService.shouldValidateInformalNotificationCost(notificationInt);

        assertTrue(result);
    }

    @Test
    void shouldValidateInformalNotificationCost_shouldReturnFalseWhenThereAreNoPagoPaPayments() {
        NotificationInt notificationInt = buildNotificationInt(
                "TEST-IUN-2",
                CommunicationType.INFORMAL,
                List.of(
                        NotificationRecipientInt.builder()
                                .internalId("recipient-1")
                                .payments(List.of(
                                        NotificationPaymentInfoInt.builder()
                                                .build()
                                ))
                                .build()
                )
        );

        boolean result = notificationCostService.shouldValidateInformalNotificationCost(notificationInt);

        assertFalse(result);
    }

    @Test
    void shouldValidateInformalNotificationCost_shouldReturnFalseWhenPaymentsAreNull() {
        NotificationInt notificationInt = buildNotificationInt(
                "TEST-IUN-3",
                CommunicationType.INFORMAL,
                List.of(
                        NotificationRecipientInt.builder()
                                .internalId("recipient-1")
                                .payments(null)
                                .build()
                )
        );

        boolean result = notificationCostService.shouldValidateInformalNotificationCost(notificationInt);

        assertFalse(result);
    }

    @Test
    void shouldValidateInformalNotificationCost_shouldThrowWhenCommunicationTypeIsNotInformal() {
        NotificationInt notificationInt = buildNotificationInt(
                "TEST-IUN-4",
                CommunicationType.LEGAL,
                Collections.singletonList(
                        NotificationRecipientInt.builder()
                                .internalId("recipient-1")
                                .payments(List.of(
                                        NotificationPaymentInfoInt.builder()
                                                .pagoPA(new PagoPaInt())
                                                .build()
                                ))
                                .build()
                )
        );

        assertThrows(PnInternalException.class,
                () -> notificationCostService.shouldValidateInformalNotificationCost(notificationInt));
    }

    private NotificationInt buildNotificationInt(String iun, CommunicationType communicationType, List<NotificationRecipientInt> recipients) {
        return NotificationInt.builder()
                .iun(iun)
                .sender(NotificationSenderInt.builder()
                        .paId("testPaId")
                        .paTaxId("testTaxId")
                        .paDenomination("Test PA")
                        .build())
                .recipients(recipients)
                .sentAt(Instant.now())
                .documents(Collections.emptyList())
                .communicationType(communicationType)
                .build();
    }

    private NotificationInt buildNotificationInt(String iun, CommunicationType communicationType) {
        return buildNotificationInt(iun, communicationType, Collections.emptyList());
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
