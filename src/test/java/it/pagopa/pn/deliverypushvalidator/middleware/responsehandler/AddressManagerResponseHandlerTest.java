package it.pagopa.pn.deliverypushvalidator.middleware.responsehandler;

import it.pagopa.pn.commons.exceptions.PnInternalException;
import it.pagopa.pn.deliverypushvalidator.action.startworkflow.notificationvalidation.NotificationValidationActionHandler;
import it.pagopa.pn.deliverypushvalidator.action.utils.TimelineUtils;
import it.pagopa.pn.deliverypushvalidator.dto.ext.addressmanager.NormalizeItemsResultInt;
import it.pagopa.pn.deliverypushvalidator.dto.ext.delivery.notification.CommunicationType;
import it.pagopa.pn.deliverypushvalidator.dto.timeline.TimelineElementInternal;
import it.pagopa.pn.deliverypushvalidator.generated.openapi.msclient.addressmanager.model.NormalizeItemsResult;
import it.pagopa.pn.deliverypushvalidator.service.TimelineService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class AddressManagerResponseHandlerTest {
    @Mock
    private TimelineUtils timelineUtils;

    @Mock
    private NotificationValidationActionHandler notificationValidationActionHandler;

    @Mock
    private TimelineService timelineService;

    private AddressManagerResponseHandler handler;

    @BeforeEach
    void setup() {
        handler = new AddressManagerResponseHandler(notificationValidationActionHandler, timelineUtils, timelineService);
    }

    @Test
    void handleResponseCancelled() {
        //GIVEN
        String correlationId = "corr-123";
        String iun = "iun-123";
        Mockito.when(timelineUtils.getIunFromTimelineId(correlationId)).thenReturn(iun);
        Mockito.when(timelineUtils.checkIsNotificationCancellationRequested(iun)).thenReturn(true);

        NormalizeItemsResult normalizeItemsResult = new NormalizeItemsResult();
        normalizeItemsResult.setCorrelationId(correlationId);

        //WHEN
        handler.handleResponseReceived(normalizeItemsResult);

        //THEN
        Mockito.verify(notificationValidationActionHandler, Mockito.never())
                .handleValidateAndNormalizeAddressResponse(Mockito.anyString(), Mockito.any(), Mockito.any());
    }

    @Test
    void handleResponseReceived_validFlow() {
        // GIVEN
        String correlationId = "corr-456";
        String iun = "iun-456";
        Mockito.when(timelineUtils.getIunFromTimelineId(correlationId)).thenReturn(iun);
        Mockito.when(timelineUtils.checkIsNotificationCancellationRequested(iun)).thenReturn(false);

        TimelineElementInternal timelineElement = Mockito.mock(TimelineElementInternal.class);
        Mockito.when(timelineElement.getCommunicationType()).thenReturn(CommunicationType.LEGAL);
        Mockito.when(timelineService.getTimelineElement(iun, correlationId)).thenReturn(Optional.of(timelineElement));

        NormalizeItemsResult normalizeItemsResult = new NormalizeItemsResult();
        normalizeItemsResult.setCorrelationId(correlationId);

        // WHEN
        handler.handleResponseReceived(normalizeItemsResult);

        // THEN
        Mockito.verify(notificationValidationActionHandler)
                .handleValidateAndNormalizeAddressResponse(
                        Mockito.eq(iun),
                        Mockito.any(NormalizeItemsResultInt.class),
                        Mockito.eq(CommunicationType.LEGAL)
                );
    }

    @Test
    void handleResponseReceived_timelineElementMissing_shouldThrow() {
        //GIVEN
        String correlationId = "corr-789";
        String iun = "iun-789";
        Mockito.when(timelineUtils.getIunFromTimelineId(correlationId)).thenReturn(iun);
        Mockito.when(timelineUtils.checkIsNotificationCancellationRequested(iun)).thenReturn(false);
        Mockito.when(timelineService.getTimelineElement(iun, correlationId)).thenReturn(Optional.empty());

        NormalizeItemsResult normalizeItemsResult = new NormalizeItemsResult();
        normalizeItemsResult.setCorrelationId(correlationId);

        //WHEN + THEN
        assertThrows(PnInternalException.class, () -> handler.handleResponseReceived(normalizeItemsResult));
    }
}