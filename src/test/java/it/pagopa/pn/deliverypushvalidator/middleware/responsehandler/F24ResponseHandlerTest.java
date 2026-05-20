package it.pagopa.pn.deliverypushvalidator.middleware.responsehandler;

import it.pagopa.pn.api.dto.events.*;
import it.pagopa.pn.commons.exceptions.PnInternalException;
import it.pagopa.pn.deliverypushvalidator.action.startworkflow.notificationvalidation.NotificationValidationActionHandler;
import it.pagopa.pn.deliverypushvalidator.action.utils.TimelineUtils;
import it.pagopa.pn.deliverypushvalidator.exception.PnValidationNotValidF24Exception;
import it.pagopa.pn.deliverypushvalidator.middleware.queue.producer.abstractions.actionspool.ActionType;
import it.pagopa.pn.deliverypushvalidator.service.F24Service;
import it.pagopa.pn.deliverypushvalidator.service.SchedulerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class F24ResponseHandlerTest {

    @Mock private TimelineUtils timelineUtils;
    @Mock private NotificationValidationActionHandler validationActionHandler;
    @Mock private F24Service f24Service;
    @Mock private SchedulerService schedulerService;

    @InjectMocks
    private F24ResponseHandler handler;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        handler = new F24ResponseHandler(timelineUtils, validationActionHandler, f24Service, schedulerService);
    }

    @Test
    void handleEventF24_shouldCallHandleValidationResponseReceived() {
        PnF24MetadataValidationEndEvent.Detail event = mock(PnF24MetadataValidationEndEvent.Detail.class);
        when(event.getMetadataValidationEnd()).thenReturn(mock(PnF24MetadataValidationEndEventPayload.class));
        when(event.getMetadataValidationEnd().getSetId()).thenReturn("IUN123");
        when(timelineUtils.checkIsNotificationCancellationRequested("IUN123")).thenReturn(false);

        handler.handleEventF24(event);

        verify(validationActionHandler).handleValidateF24Response(any(), Mockito.isNull());
    }

    @Test
    void handleEventF24_shouldCallHandlePrepareResponseReceived() {
        PnF24PdfSetReadyEvent.Detail event = mock(PnF24PdfSetReadyEvent.Detail.class);
        PnF24PdfSetReadyEventPayload payload = mock(PnF24PdfSetReadyEventPayload.class);
        PnF24PdfSetReadyEventItem item = mock(PnF24PdfSetReadyEventItem.class);

        when(event.getPdfSetReady()).thenReturn(payload);
        when(payload.getGeneratedPdfsUrls()).thenReturn(List.of(item));
        when(item.getPathTokens()).thenReturn("0_token");
        when(item.getUri()).thenReturn("uri1");
        when(payload.getRequestId()).thenReturn("timelineId");
        when(timelineUtils.getIunFromTimelineId("timelineId")).thenReturn("IUN456");

        handler.handleEventF24(event);

        verify(f24Service).handleF24PrepareResponse(eq("IUN456"), any(Map.class));
        verify(schedulerService).scheduleEvent(eq("IUN456"), any(Instant.class), eq(ActionType.POST_ACCEPTED_PROCESSING_COMPLETED));
    }

    @Test
    void handleEventF24_shouldThrowOnInvalidType() {
        DetailedTypePayload event = mock(DetailedTypePayload.class);
        assertThrows(PnInternalException.class, () -> handler.handleEventF24(event));
    }

    @Test
    void handleValidationResponseReceived_shouldThrowOnNullPayload() {
        PnF24MetadataValidationEndEvent.Detail event = mock(PnF24MetadataValidationEndEvent.Detail.class);
        when(event.getMetadataValidationEnd()).thenReturn(null);
        assertThrows(PnValidationNotValidF24Exception.class, () -> {
            handler.handleEventF24(event);
        });
    }
}
