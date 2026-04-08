package it.pagopa.pn.deliverypushvalidator.middleware.externalclient.pnclient.timeline;

import it.pagopa.pn.common.rest.error.v1.dto.Problem;
import it.pagopa.pn.commons.exceptions.PnHttpResponseException;
import it.pagopa.pn.deliverypushvalidator.dto.ext.delivery.notification.NotificationInt;
import it.pagopa.pn.deliverypushvalidator.dto.timeline.TimelineElementInternal;
import it.pagopa.pn.deliverypushvalidator.dto.timeline.details.TimelineElementCategoryInt;
import it.pagopa.pn.deliverypushvalidator.dto.timeline.details.TimelineElementDetailsInt;
import it.pagopa.pn.deliverypushvalidator.generated.openapi.msclient.timelineservice.api.TimelineControllerApi;
import it.pagopa.pn.deliverypushvalidator.generated.openapi.msclient.timelineservice.model.*;
import it.pagopa.pn.deliverypushvalidator.service.mapper.TimelineServiceMapper;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static it.pagopa.pn.deliverypushvalidator.exception.PnDeliveryPushValidatorExceptionCodes.ERROR_CODE_TIMELINESERVICE_TIMELINE_ELEMENT_NOT_PRESENT;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TimelineClientImplTest {
    @Mock
    private TimelineControllerApi timelineControllerApi;

    @Mock
    private TimelineServiceMapper timelineServiceMapper;

    @InjectMocks
    private TimelineClientImpl timelineServiceClient;

    @Test
    void addTimelineElementReturnsTrueWhenConflictOccurs() {
        TimelineElementInternal timelineElementInternal = Mockito.mock(TimelineElementInternal.class);
        NotificationInt notificationInt = Mockito.mock(NotificationInt.class);

        NewTimelineElement newTimelineElement = Mockito.mock(NewTimelineElement.class);
        Mockito.when(timelineServiceMapper.getNewTimelineElement(timelineElementInternal, notificationInt))
                .thenReturn(newTimelineElement);

        PnHttpResponseException exception = new PnHttpResponseException("Conflict", HttpStatus.SC_CONFLICT);

        Mockito.doThrow(exception)
                .when(timelineControllerApi)
                .addTimelineElement(newTimelineElement);

        boolean result = timelineServiceClient.addTimelineElement(timelineElementInternal, notificationInt);

        assertTrue(result);
    }

    @Test
    void addTimelineElementReturnsFalseWhenOtherErrorOccurs() {
        TimelineElementInternal timelineElementInternal = Mockito.mock(TimelineElementInternal.class);
        NotificationInt notificationInt = Mockito.mock(NotificationInt.class);

        NewTimelineElement newTimelineElement = new NewTimelineElement();
        Mockito.when(timelineServiceMapper.getNewTimelineElement(timelineElementInternal, notificationInt))
                .thenReturn(newTimelineElement);

        Mockito.when(timelineControllerApi.addTimelineElement(Mockito.any()))
                .thenReturn(new TimelineElementIdResponse());

        boolean result = timelineServiceClient.addTimelineElement(timelineElementInternal, notificationInt);

        assertFalse(result);
    }

    @Test
    void addTimelineElement_throwsExceptionOnError() {
        TimelineElementInternal timelineElementInternal = Mockito.mock(TimelineElementInternal.class);
        NotificationInt notificationInt = Mockito.mock(NotificationInt.class);
        NewTimelineElement newTimelineElement = Mockito.mock(NewTimelineElement.class);
        PnHttpResponseException exception = new PnHttpResponseException("Errore generico", HttpStatus.SC_INTERNAL_SERVER_ERROR);

        Mockito.when(timelineServiceMapper.getNewTimelineElement(timelineElementInternal, notificationInt))
                .thenReturn(newTimelineElement);

        Mockito.doThrow(exception)
                .when(timelineControllerApi)
                .addTimelineElement(newTimelineElement);

        PnHttpResponseException thrown = assertThrows(PnHttpResponseException.class, () ->
                timelineServiceClient.addTimelineElement(timelineElementInternal, notificationInt)
        );

        assertEquals(HttpStatus.SC_INTERNAL_SERVER_ERROR, thrown.getStatusCode());
    }

    @Test
    void retrieveAndIncrementCounterForTimelineEvent_returnsExpectedCounter() {
        String timelineId = "timeline123";
        Long expectedCounter = 42L;

        Mockito.when(timelineControllerApi.retrieveAndIncrementCounterForTimelineEvent(timelineId))
                .thenReturn(expectedCounter);

        Long result = timelineServiceClient.retrieveAndIncrementCounterForTimelineEvent(timelineId);

        assertEquals(expectedCounter, result);
        Mockito.verify(timelineControllerApi).retrieveAndIncrementCounterForTimelineEvent(timelineId);
    }

    @Test
    void getTimelineElement_returnsExpectedElement() {
        String iun = "iun123";
        String timelineId = "timeline123";
        Boolean strongly = true;
        TimelineElementInternal expectedElement = Mockito.mock(TimelineElementInternal.class);
        TimelineElement timelineElement = new TimelineElement();

        Mockito.when(timelineControllerApi.getTimelineElement(iun, timelineId, strongly))
                .thenReturn(timelineElement);

        Mockito.when(timelineServiceMapper.toTimelineElementInternal(timelineElement)).thenReturn(expectedElement);

        TimelineElementInternal result = timelineServiceClient.getTimelineElement(iun, timelineId, strongly);

        assertEquals(expectedElement, result);
        Mockito.verify(timelineControllerApi).getTimelineElement(iun, timelineId, strongly);
    }

    @Test
    void getTimelineElementDetails_handlesNullDetails() {
        String iun = "iun123";
        String timelineId = "timeline123";

        Mockito.when(timelineControllerApi.getTimelineElementDetails(iun, timelineId))
                .thenReturn(null);

        TimelineElementDetailsInt result = timelineServiceClient.getTimelineElementDetails(iun, timelineId);

        assertNull(result);
        Mockito.verify(timelineControllerApi).getTimelineElementDetails(iun, timelineId);
        Mockito.verify(timelineServiceMapper, never()).toTimelineElementDetailsInt(Mockito.any(), Mockito.any());
    }

    @Test
    void getTimelineElementDetails_returnsExpectedDetails() {
        String iun = "iun123";
        String timelineId = "timeline123";
        TimelineElementDetails timelineElementDetails = new SenderAckCreationRequestDetails().categoryType("SENDER_ACK_CREATION_REQUEST");
        TimelineElementDetailsInt expectedDetails = Mockito.mock(TimelineElementDetailsInt.class);

        Mockito.when(timelineControllerApi.getTimelineElementDetails(iun, timelineId))
                .thenReturn(timelineElementDetails);

        Mockito.when(timelineServiceMapper.toTimelineElementDetailsInt(timelineElementDetails, TimelineElementCategoryInt.SENDER_ACK_CREATION_REQUEST))
                .thenReturn(expectedDetails);

        TimelineElementDetailsInt result = timelineServiceClient.getTimelineElementDetails(iun, timelineId);

        assertEquals(expectedDetails, result);
        Mockito.verify(timelineControllerApi).getTimelineElementDetails(iun, timelineId);
    }

    @Test
    void getTimelineElementDetailForSpecificRecipient_returnsExpectedDetails() {
        String iun = "iun123";
        Integer recIndex = 1;
        Boolean confidentialInfoRequired = true;
        TimelineCategory category = TimelineCategory.SENDER_ACK_CREATION_REQUEST;
        TimelineElementCategoryInt categoryInt = TimelineElementCategoryInt.SENDER_ACK_CREATION_REQUEST;
        TimelineElementDetails timelineElementDetails = new SenderAckCreationRequestDetails().categoryType("SENDER_ACK_CREATION_REQUEST");
        TimelineElementDetailsInt expectedDetails = Mockito.mock(TimelineElementDetailsInt.class);

        Mockito.when(timelineControllerApi.getTimelineElementDetailForSpecificRecipient(iun, recIndex, confidentialInfoRequired, category))
                .thenReturn(timelineElementDetails);

        Mockito.when(timelineServiceMapper.toTimelineElementDetailsInt(timelineElementDetails, TimelineElementCategoryInt.SENDER_ACK_CREATION_REQUEST))
                .thenReturn(expectedDetails);

        TimelineElementDetailsInt result = timelineServiceClient.getTimelineElementDetailForSpecificRecipient(iun, recIndex, confidentialInfoRequired, categoryInt);

        assertEquals(expectedDetails, result);
        Mockito.verify(timelineControllerApi).getTimelineElementDetailForSpecificRecipient(iun, recIndex, confidentialInfoRequired, category);
    }

    @Test
    void getTimelineElementDetailForSpecificRecipient_throwsException() {
        String iun = "iun123";
        Integer recIndex = 1;
        Boolean confidentialInfoRequired = true;
        TimelineCategory category = TimelineCategory.VALIDATED_F24;
        TimelineElementCategoryInt categoryInt = TimelineElementCategoryInt.VALIDATED_F24;

        Mockito.when(timelineControllerApi.getTimelineElementDetailForSpecificRecipient(iun, recIndex, confidentialInfoRequired, category))
                .thenThrow(new RuntimeException("Errore"));

        assertThrows(RuntimeException.class, () ->
                timelineServiceClient.getTimelineElementDetailForSpecificRecipient(iun, recIndex, confidentialInfoRequired, categoryInt)
        );
    }

    @Test
    void getTimelineElementForSpecificRecipient_returnsExpectedElement() {
        String iun = "iun123";
        Integer recIndex = 1;
        TimelineCategory category = TimelineCategory.VALIDATED_F24;
        TimelineElementCategoryInt categoryInt = TimelineElementCategoryInt.VALIDATED_F24;
        TimelineElement timelineElement = new TimelineElement();
        TimelineElementInternal expectedElement = Mockito.mock(TimelineElementInternal.class);

        Mockito.when(timelineControllerApi.getTimelineElementForSpecificRecipient(iun, recIndex, category))
                .thenReturn(timelineElement);

        Mockito.when(timelineServiceMapper.toTimelineElementInternal(timelineElement)).thenReturn(expectedElement);

        TimelineElementInternal result = timelineServiceClient.getTimelineElementForSpecificRecipient(iun, recIndex, categoryInt);

        assertEquals(expectedElement, result);
        Mockito.verify(timelineControllerApi).getTimelineElementForSpecificRecipient(iun, recIndex, category);
    }

    @Test
    void getTimelineElementForSpecificRecipient_throwsException() {
        String iun = "iun123";
        Integer recIndex = 1;
        TimelineCategory category = TimelineCategory.VALIDATED_F24;
        TimelineElementCategoryInt categoryInt = TimelineElementCategoryInt.VALIDATED_F24;

        Mockito.when(timelineControllerApi.getTimelineElementForSpecificRecipient(iun, recIndex, category))
                .thenThrow(new RuntimeException("Errore"));

        assertThrows(RuntimeException.class, () ->
                timelineServiceClient.getTimelineElementForSpecificRecipient(iun, recIndex, categoryInt)
        );
    }

    @Test
    void getTimeline_returnsExpectedList() {
        String iun = "iun123";
        Boolean confidentialInfoRequired = true;
        Boolean strongly = false;
        String timelineId = "timeline123";
        TimelineElement timelineElementKnown = new TimelineElement().category(TimelineCategory.NORMALIZED_ADDRESS);
        TimelineElement timelineElementUnknown = new TimelineElement().category(TimelineCategory.NOTIFICATION_VIEWED);
        TimelineElementInternal expectedElement = new TimelineElementInternal();

        Mockito.when(timelineControllerApi.getTimeline(iun, confidentialInfoRequired, strongly, timelineId))
                .thenReturn(List.of(timelineElementKnown, timelineElementUnknown));

        Mockito.when(timelineServiceMapper.toTimelineElementInternal(timelineElementKnown)).thenReturn(expectedElement);

        List<TimelineElementInternal> result = timelineServiceClient.getTimeline(iun, confidentialInfoRequired, strongly, timelineId);

        assertEquals(1, result.size()); // Only one known category should be returned
        assertEquals(expectedElement, result.getFirst());
        Mockito.verify(timelineControllerApi).getTimeline(iun, confidentialInfoRequired, strongly, timelineId);
    }

    @Test
    void getTimeline_throwsException() {
        String iun = "iun123";
        Boolean confidentialInfoRequired = true;
        Boolean strongly = false;
        String timelineId = "timeline123";

        Mockito.when(timelineControllerApi.getTimeline(iun, confidentialInfoRequired, strongly, timelineId))
                .thenThrow(new RuntimeException("Errore"));

        assertThrows(RuntimeException.class, () ->
                timelineServiceClient.getTimeline(iun, confidentialInfoRequired, strongly, timelineId)
        );
    }

    @Test
    void getNotificationCancellationRequested_returnsOptionalWithResponse() {
        String iun = "iun123";
        Instant expectedInstant = Instant.now();
        CancellationRequestResponse cancellationRequestResponse = new CancellationRequestResponse();
        cancellationRequestResponse.setTimestamp(expectedInstant);


        when(timelineControllerApi.getCancellationRequest(iun))
                .thenReturn(cancellationRequestResponse);

        Optional<CancellationRequestResponse> result = timelineServiceClient.getNotificationCancellationRequested(iun);

        assertTrue(result.isPresent());
        assertEquals(cancellationRequestResponse, result.get());
        Mockito.verify(timelineControllerApi).getCancellationRequest(iun);
    }

    @Test
    void getNotificationCancellationRequested_returnsOptionalEmpty() {
        String iun = "iun123";
        it.pagopa.pn.common.rest.error.v1.dto.ProblemError problemError = new it.pagopa.pn.common.rest.error.v1.dto.ProblemError();
        problemError.setCode(ERROR_CODE_TIMELINESERVICE_TIMELINE_ELEMENT_NOT_PRESENT);
        problemError.setDetail("");
        problemError.setElement("");

        it.pagopa.pn.common.rest.error.v1.dto.Problem problem = new Problem();
        problem.setErrors(Collections.singletonList(problemError));
        PnHttpResponseException exception = Mockito.mock(PnHttpResponseException.class);

        Mockito.when(exception.getStatusCode()).thenReturn(org.springframework.http.HttpStatus.NOT_FOUND.value());
        Mockito.when(exception.getProblem()).thenReturn(problem);


        when(timelineControllerApi.getCancellationRequest(iun))
                .thenThrow(exception);

        Optional<CancellationRequestResponse> result = timelineServiceClient.getNotificationCancellationRequested(iun);

        assertTrue(result.isEmpty());
        Mockito.verify(timelineControllerApi).getCancellationRequest(iun);
    }

    @Test
    void getNotificationCancellationRequested_throwsException() {
        String iun = "iun123";
        PnHttpResponseException exception = Mockito.mock(PnHttpResponseException.class);
        Mockito.when(exception.getStatusCode()).thenReturn(org.springframework.http.HttpStatus.BAD_REQUEST.value());

        when(timelineControllerApi.getCancellationRequest(iun))
                .thenThrow(exception);

        assertThrows(PnHttpResponseException.class, () ->
                timelineServiceClient.getNotificationCancellationRequested(iun)
        );
        Mockito.verify(timelineControllerApi).getCancellationRequest(iun);
    }
}
