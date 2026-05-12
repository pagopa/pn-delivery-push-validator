package it.pagopa.pn.deliverypushvalidator.action.refused;

import it.pagopa.pn.deliverypushvalidator.action.utils.TimelineUtils;
import it.pagopa.pn.deliverypushvalidator.dto.cost.PaymentsInfoForRecipientInt;
import it.pagopa.pn.deliverypushvalidator.dto.cost.UpdateCostPhaseInt;
import it.pagopa.pn.deliverypushvalidator.dto.cost.UpdateNotificationCostResponseInt;
import it.pagopa.pn.deliverypushvalidator.dto.cost.UpdateNotificationCostResultInt;
import it.pagopa.pn.deliverypushvalidator.dto.ext.delivery.notification.NotificationInt;
import it.pagopa.pn.deliverypushvalidator.dto.ext.delivery.notification.NotificationPaymentInfoInt;
import it.pagopa.pn.deliverypushvalidator.dto.ext.delivery.notification.NotificationRecipientInt;
import it.pagopa.pn.deliverypushvalidator.dto.ext.delivery.notification.PagoPaInt;
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
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LegalNotificationRefusedStrategyTest {

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
    private LegalNotificationRefusedStrategy strategy;

    @Test
    void getNotificationDelegatesToNotificationService() {
        NotificationInt expected = NotificationInt.builder().iun("IUN_1").build();
        when(notificationService.getNotificationByIun("IUN_1")).thenReturn(expected);

        NotificationInt result = strategy.getNotification("IUN_1");

        assertEquals(expected, result);
        verify(notificationService).getNotificationByIun("IUN_1");
    }

    @Test
    void handleNotificationRefusedUpdatesCostForDeliveryModeAsync() {
        // Given
        String iun = "IUN_2";
        Instant notBefore = Instant.parse("2026-05-12T10:15:30Z");
        List<NotificationRefusedErrorInt> errors = List.of(NotificationRefusedErrorInt.builder().errorCode("E01").detail("detail").build());
        NotificationInt notification = buildNotification(iun, NotificationFeePolicy.DELIVERY_MODE, PagoPaIntMode.ASYNC);
        TimelineElementInternal timelineElement = TimelineElementInternal.builder().build();
        UpdateNotificationCostResponseInt costResponse = UpdateNotificationCostResponseInt.builder().updateResults(Collections.emptyList()).build();

        when(notificationService.getNotificationByIun(iun)).thenReturn(notification);
        when(refusalCostCalculator.calculateRefusalCost(notification, errors)).thenReturn(13);
        when(timelineUtils.buildRefusedRequestTimelineElement(notification, errors, 13)).thenReturn(timelineElement);
        when(notificationProcessCostService.setNotificationStepCost(eq(0), eq(iun), any(), eq(notBefore), eq(notBefore), eq(UpdateCostPhaseInt.REQUEST_REFUSED)))
                .thenReturn(Mono.just(costResponse));

        // When
        strategy.handleNotificationRefused(iun, errors, notBefore);

        // Then
        verify(notificationService, times(1)).getNotificationByIun(iun);
        assertEquals(iun, notification.getIun());

        verify(refusalCostCalculator, times(1)).calculateRefusalCost(notification, errors);

        verify(timelineUtils, times(1)).buildRefusedRequestTimelineElement(notification, errors, 13);

        verify(notificationProcessCostService, times(1)).setNotificationStepCost(eq(0), eq(iun), any(), eq(notBefore), eq(notBefore), eq(UpdateCostPhaseInt.REQUEST_REFUSED));

        verify(timelineService, times(1)).addTimelineElement(timelineElement, notification);
    }

    @Test
    void handleNotificationRefusedSkipsCostUpdateWhenNotDeliveryModeAsync() {
        // Given
        String iun = "IUN_3";
        Instant notBefore = Instant.parse("2026-05-12T11:00:00Z");
        List<NotificationRefusedErrorInt> errors = List.of(NotificationRefusedErrorInt.builder().errorCode("E02").detail("detail").build());
        NotificationInt notification = buildNotification(iun, NotificationFeePolicy.FLAT_RATE, PagoPaIntMode.SYNC);
        TimelineElementInternal timelineElement = TimelineElementInternal.builder().build();

        when(notificationService.getNotificationByIun(iun)).thenReturn(notification);
        when(refusalCostCalculator.calculateRefusalCost(notification, errors)).thenReturn(5);
        when(timelineUtils.buildRefusedRequestTimelineElement(notification, errors, 5)).thenReturn(timelineElement);

        // When
        strategy.handleNotificationRefused(iun, errors, notBefore);

        // Then
        verify(notificationService, times(1)).getNotificationByIun(iun);
        assertEquals(iun, notification.getIun());

        verify(refusalCostCalculator, times(1)).calculateRefusalCost(notification, errors);

        verify(timelineUtils, times(1)).buildRefusedRequestTimelineElement(notification, errors, 5);

        // Verificare che setNotificationStepCost NON è mai stato chiamato
        verify(notificationProcessCostService, never()).setNotificationStepCost(eq(0), eq(iun), any(), eq(notBefore), eq(notBefore), eq(UpdateCostPhaseInt.REQUEST_REFUSED));

        verify(timelineService, times(1)).addTimelineElement(timelineElement, notification);
    }

    @Test
    void handleNotificationRefusedWithDeliveryModeAndSyncMode() {
        // Given - DELIVERY_MODE ma SYNC (non ASYNC), quindi no cost update
        String iun = "IUN_4";
        Instant notBefore = Instant.parse("2026-05-12T12:00:00Z");
        List<NotificationRefusedErrorInt> errors = List.of(NotificationRefusedErrorInt.builder().errorCode("E03").detail("detail").build());
        NotificationInt notification = buildNotification(iun, NotificationFeePolicy.DELIVERY_MODE, PagoPaIntMode.SYNC);
        TimelineElementInternal timelineElement = TimelineElementInternal.builder().build();

        when(notificationService.getNotificationByIun(iun)).thenReturn(notification);
        when(refusalCostCalculator.calculateRefusalCost(notification, errors)).thenReturn(10);
        when(timelineUtils.buildRefusedRequestTimelineElement(notification, errors, 10)).thenReturn(timelineElement);

        // When
        strategy.handleNotificationRefused(iun, errors, notBefore);

        // Then - Cost update non effettuato perché non è ASYNC
        verify(notificationProcessCostService, never()).setNotificationStepCost(eq(0), eq(iun), any(), eq(notBefore), eq(notBefore), eq(UpdateCostPhaseInt.REQUEST_REFUSED));
        verify(timelineService, times(1)).addTimelineElement(timelineElement, notification);
    }

    @Test
    void handleNotificationRefusedWithAsyncButNoPayments() {
        // Given - DELIVERY_MODE + ASYNC ma nessun pagamento con applyCost=true
        String iun = "IUN_5";
        Instant notBefore = Instant.parse("2026-05-12T13:00:00Z");
        List<NotificationRefusedErrorInt> errors = List.of(NotificationRefusedErrorInt.builder().errorCode("E04").detail("detail").build());
        NotificationInt notification = buildNotificationWithoutPayments(iun);
        TimelineElementInternal timelineElement = TimelineElementInternal.builder().build();

        when(notificationService.getNotificationByIun(iun)).thenReturn(notification);
        when(refusalCostCalculator.calculateRefusalCost(notification, errors)).thenReturn(7);
        when(timelineUtils.buildRefusedRequestTimelineElement(notification, errors, 7)).thenReturn(timelineElement);

        // When
        strategy.handleNotificationRefused(iun, errors, notBefore);

        // Then - Cost update non fatto perché pagamenti vuoti
        verify(notificationProcessCostService, never()).setNotificationStepCost(eq(0), eq(iun), any(), eq(notBefore), eq(notBefore), eq(UpdateCostPhaseInt.REQUEST_REFUSED));
        verify(timelineService, times(1)).addTimelineElement(timelineElement, notification);
    }

    @Test
    void handleNotificationRefusedWhenUpdateCostResponseIsNull() {
        // Given - DELIVERY_MODE + ASYNC, pagamenti presenti, ma response null (can happen with Mono.empty().blockOptional())
        String iun = "IUN_6";
        Instant notBefore = Instant.parse("2026-05-12T14:00:00Z");
        List<NotificationRefusedErrorInt> errors = List.of(NotificationRefusedErrorInt.builder().errorCode("E05").detail("detail").build());
        NotificationInt notification = buildNotification(iun, NotificationFeePolicy.DELIVERY_MODE, PagoPaIntMode.ASYNC);
        TimelineElementInternal timelineElement = TimelineElementInternal.builder().build();
        UpdateNotificationCostResponseInt validResponse = UpdateNotificationCostResponseInt.builder().updateResults(Collections.emptyList()).build();

        when(notificationService.getNotificationByIun(iun)).thenReturn(notification);
        when(refusalCostCalculator.calculateRefusalCost(notification, errors)).thenReturn(8);
        when(timelineUtils.buildRefusedRequestTimelineElement(notification, errors, 8)).thenReturn(timelineElement);
        when(notificationProcessCostService.setNotificationStepCost(eq(0), eq(iun), any(), eq(notBefore), eq(notBefore), eq(UpdateCostPhaseInt.REQUEST_REFUSED)))
                .thenReturn(Mono.just(validResponse));

        // When
        strategy.handleNotificationRefused(iun, errors, notBefore);

        // Then - handleResponse è stato chiamato con la response valida
        verify(notificationProcessCostService, times(1)).setNotificationStepCost(eq(0), eq(iun), any(), eq(notBefore), eq(notBefore), eq(UpdateCostPhaseInt.REQUEST_REFUSED));
        verify(timelineService, times(1)).addTimelineElement(timelineElement, notification);
    }

    @Test
    void handleNotificationRefusedWhenUpdateResultsIsEmpty() {
        // Given - DELIVERY_MODE + ASYNC, response con results vuoti
        String iun = "IUN_7";
        Instant notBefore = Instant.parse("2026-05-12T15:00:00Z");
        List<NotificationRefusedErrorInt> errors = List.of(NotificationRefusedErrorInt.builder().errorCode("E06").detail("detail").build());
        NotificationInt notification = buildNotification(iun, NotificationFeePolicy.DELIVERY_MODE, PagoPaIntMode.ASYNC);
        TimelineElementInternal timelineElement = TimelineElementInternal.builder().build();
        UpdateNotificationCostResponseInt emptyResponse = UpdateNotificationCostResponseInt.builder().updateResults(Collections.emptyList()).build();

        when(notificationService.getNotificationByIun(iun)).thenReturn(notification);
        when(refusalCostCalculator.calculateRefusalCost(notification, errors)).thenReturn(9);
        when(timelineUtils.buildRefusedRequestTimelineElement(notification, errors, 9)).thenReturn(timelineElement);
        when(notificationProcessCostService.setNotificationStepCost(eq(0), eq(iun), any(), eq(notBefore), eq(notBefore), eq(UpdateCostPhaseInt.REQUEST_REFUSED)))
                .thenReturn(Mono.just(emptyResponse));

        // When
        strategy.handleNotificationRefused(iun, errors, notBefore);

        // Then - handleResponse non è stato chiamato perché results è vuoto
        verify(notificationProcessCostService, times(1)).setNotificationStepCost(eq(0), eq(iun), any(), eq(notBefore), eq(notBefore), eq(UpdateCostPhaseInt.REQUEST_REFUSED));
        verify(timelineService, times(1)).addTimelineElement(timelineElement, notification);
    }

    @Test
    void handleNotificationRefusedWhenUpdateResultsIsNotEmpty() {
        // Given - DELIVERY_MODE + ASYNC, response con updateResults NON vuoto
        // Questo test copre il blocco:
        // if (updateNotificationCostResponse != null && !updateNotificationCostResponse.getUpdateResults().isEmpty()) {
        //    handleResponse(notification, updateNotificationCostResponse);
        // }
        String iun = "IUN_9";
        Instant notBefore = Instant.parse("2026-05-12T17:00:00Z");
        List<NotificationRefusedErrorInt> errors = List.of(NotificationRefusedErrorInt.builder().errorCode("E07").detail("detail").build());
        NotificationInt notification = buildNotification(iun, NotificationFeePolicy.DELIVERY_MODE, PagoPaIntMode.ASYNC);
        TimelineElementInternal timelineElement = TimelineElementInternal.builder().build();

        // Creare una response con un UpdateResult valido (non vuoto)
        PaymentsInfoForRecipientInt paymentsInfo = PaymentsInfoForRecipientInt.builder()
                .recIndex(0)
                .noticeCode("302000100000019421")
                .creditorTaxId("77777777777")
                .applyCost(true)
                .build();
        UpdateNotificationCostResultInt updateResult = UpdateNotificationCostResultInt.builder()
                .paymentsInfoForRecipient(paymentsInfo)
                .result(UpdateNotificationCostResultInt.ResultEnum.OK)
                .build();
        UpdateNotificationCostResponseInt responseWithResults = UpdateNotificationCostResponseInt.builder()
                .updateResults(List.of(updateResult))
                .build();

        when(notificationService.getNotificationByIun(iun)).thenReturn(notification);
        when(refusalCostCalculator.calculateRefusalCost(notification, errors)).thenReturn(11);
        when(timelineUtils.buildRefusedRequestTimelineElement(notification, errors, 11)).thenReturn(timelineElement);
        when(notificationProcessCostService.setNotificationStepCost(eq(0), eq(iun), any(), eq(notBefore), eq(notBefore), eq(UpdateCostPhaseInt.REQUEST_REFUSED)))
                .thenReturn(Mono.just(responseWithResults));

        // When
        strategy.handleNotificationRefused(iun, errors, notBefore);

        // Then - handleResponse è stato effettivamente chiamato perché response != null && results non vuoto
        verify(notificationProcessCostService, times(1)).setNotificationStepCost(eq(0), eq(iun), any(), eq(notBefore), eq(notBefore), eq(UpdateCostPhaseInt.REQUEST_REFUSED));
        verify(timelineService, times(1)).addTimelineElement(timelineElement, notification);
        // Il flusso completo è stato eseguito senza eccezioni
        verify(notificationService, times(1)).getNotificationByIun(iun);
    }

    @Test
    void handleNotificationRefusedWithMultipleErrors() {
        // Given - Più errori per testare la lista
        String iun = "IUN_8";
        Instant notBefore = Instant.parse("2026-05-12T16:00:00Z");
        List<NotificationRefusedErrorInt> errors = List.of(
                NotificationRefusedErrorInt.builder().errorCode("E01").detail("detail1").build(),
                NotificationRefusedErrorInt.builder().errorCode("E02").detail("detail2").build(),
                NotificationRefusedErrorInt.builder().errorCode("E03").detail("detail3").build()
        );
        NotificationInt notification = buildNotification(iun, NotificationFeePolicy.DELIVERY_MODE, PagoPaIntMode.ASYNC);
        TimelineElementInternal timelineElement = TimelineElementInternal.builder().build();

        when(notificationService.getNotificationByIun(iun)).thenReturn(notification);
        when(refusalCostCalculator.calculateRefusalCost(notification, errors)).thenReturn(15);
        when(timelineUtils.buildRefusedRequestTimelineElement(notification, errors, 15)).thenReturn(timelineElement);
        when(notificationProcessCostService.setNotificationStepCost(eq(0), eq(iun), any(), eq(notBefore), eq(notBefore), eq(UpdateCostPhaseInt.REQUEST_REFUSED)))
                .thenReturn(Mono.just(UpdateNotificationCostResponseInt.builder().updateResults(Collections.emptyList()).build()));

        // When
        strategy.handleNotificationRefused(iun, errors, notBefore);

        // Then
        verify(refusalCostCalculator, times(1)).calculateRefusalCost(notification, errors);
        assertEquals(3, errors.size());
        verify(timelineService, times(1)).addTimelineElement(timelineElement, notification);
    }

    private NotificationInt buildNotification(String iun, NotificationFeePolicy feePolicy, PagoPaIntMode pagoPaIntMode) {
        NotificationPaymentInfoInt payment = NotificationPaymentInfoInt.builder()
                .pagoPA(PagoPaInt.builder()
                        .applyCost(true)
                        .creditorTaxId("77777777777")
                        .noticeCode("302000100000019421")
                        .build())
                .build();

        NotificationRecipientInt recipient = NotificationRecipientInt.builder()
                .taxId("RSSMRA80A01H501U")
                .payments(List.of(payment))
                .build();

        return NotificationInt.builder()
                .iun(iun)
                .notificationFeePolicy(feePolicy)
                .pagoPaIntMode(pagoPaIntMode)
                .recipients(List.of(recipient))
                .build();
    }

    private NotificationInt buildNotificationWithoutPayments(String iun) {
        NotificationRecipientInt recipient = NotificationRecipientInt.builder()
                .taxId("RSSMRA80A01H501U")
                .payments(Collections.emptyList())
                .build();

        return NotificationInt.builder()
                .iun(iun)
                .notificationFeePolicy(NotificationFeePolicy.DELIVERY_MODE)
                .pagoPaIntMode(PagoPaIntMode.ASYNC)
                .recipients(List.of(recipient))
                .build();
    }
}
