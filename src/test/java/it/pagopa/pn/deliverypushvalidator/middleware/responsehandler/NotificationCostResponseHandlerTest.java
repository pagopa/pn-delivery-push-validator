package it.pagopa.pn.deliverypushvalidator.middleware.responsehandler;

import it.pagopa.pn.commons.exceptions.PnInternalException;
import it.pagopa.pn.deliverypushvalidator.action.startworkflow.notificationvalidation.NotificationValidationActionHandler;
import it.pagopa.pn.deliverypushvalidator.action.utils.TimelineUtils;
import it.pagopa.pn.deliverypushvalidator.dto.ext.delivery.notification.CommunicationType;
import it.pagopa.pn.deliverypushvalidator.dto.ext.notificationcost.NotificationCostValidationResponse;
import it.pagopa.pn.deliverypushvalidator.dto.timeline.TimelineElementInternal;
import it.pagopa.pn.deliverypushvalidator.service.TimelineService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
class NotificationCostResponseHandlerTest {
    @Mock
    private NotificationValidationActionHandler handler;

    @Mock
    private TimelineService timelineService;

    @InjectMocks
    private NotificationCostResponseHandler responseHandler;

    @Test
    void handleResponseReceived_shouldInvokeHandlerWhenTimelineElementExists() {
        String iun = "iun-1";
        NotificationCostValidationResponse response = NotificationCostValidationResponse.builder()
                .iun(iun)
                .build();
        TimelineElementInternal requestElement = mock(TimelineElementInternal.class);
        String requestElementId = TimelineUtils.buildNotificationCostValidationRequestElementId(iun);
        when(timelineService.getTimelineElement(iun, requestElementId)).thenReturn(Optional.of(requestElement));
        when(requestElement.getCommunicationType()).thenReturn(CommunicationType.INFORMAL);

        responseHandler.handleResponseReceived(response);

        verify(timelineService).getTimelineElement(iun, requestElementId);
        verify(handler).handleValidateNotificationCost(iun, response, CommunicationType.INFORMAL);
    }

    @Test
    void handleResponseReceived_shouldThrowWhenTimelineElementDoesNotExist() {
        String iun = "iun-2";
        NotificationCostValidationResponse response = NotificationCostValidationResponse.builder()
                .iun(iun)
                .build();
        String requestElementId = TimelineUtils.buildNotificationCostValidationRequestElementId(iun);
        when(timelineService.getTimelineElement(iun, requestElementId)).thenReturn(Optional.empty());

        assertThrows(PnInternalException.class, () -> responseHandler.handleResponseReceived(response));

        verify(handler, never()).handleValidateNotificationCost(anyString(), any(), any());
    }
}