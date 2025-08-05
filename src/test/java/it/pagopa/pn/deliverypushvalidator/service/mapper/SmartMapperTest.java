package it.pagopa.pn.deliverypushvalidator.service.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.pagopa.pn.deliverypushvalidator.dto.timeline.TimelineElementInternal;
import it.pagopa.pn.deliverypushvalidator.dto.timeline.details.NotificationRequestAcceptedDetailsInt;
import it.pagopa.pn.deliverypushvalidator.generated.openapi.msclient.timelineservice.model.NotificationRequestAcceptedDetails;
import it.pagopa.pn.deliverypushvalidator.generated.openapi.msclient.timelineservice.model.TimelineElementDetails;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SmartMapperTest {
    private SmartMapper smartMapper;
    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = new ObjectMapper();
        smartMapper = new SmartMapper(objectMapper);
    }

    @Test
    void fromInternalToExternalNormalizedAddressRequestDetails() {
        NotificationRequestAcceptedDetailsInt notificationRequestAcceptedDetailsInt = NotificationRequestAcceptedDetailsInt.builder()
                .notificationRequestId("notificationRequestId")
                .idempotenceToken("idempotenceToken")
                .paProtocolNumber("paProtocolNumber")
                .categoryType("REQUEST_ACCEPTED")
                .build();

        var details = smartMapper.mapToClassWithObjectMapper(notificationRequestAcceptedDetailsInt, TimelineElementDetails.class);
        var notificationRequestAcceptedDetailsIntExt = (NotificationRequestAcceptedDetails) details;
        Assertions.assertEquals(((NotificationRequestAcceptedDetails) details).getIdempotenceToken(),  notificationRequestAcceptedDetailsIntExt.getIdempotenceToken());
        Assertions.assertEquals(((NotificationRequestAcceptedDetails) details).getNotificationRequestId(), notificationRequestAcceptedDetailsIntExt.getNotificationRequestId());
    }

    @Test
    void fromExternalToInternalNormalizedAddressRequestDetails() {
        var timelineElementDetails = new NotificationRequestAcceptedDetails()
                .paProtocolNumber("paProtocolNumber")
                .idempotenceToken("idempotenceToken")
                .notificationRequestId("notificationRequestId");

        NotificationRequestAcceptedDetailsInt details = SmartMapper.mapToClass(timelineElementDetails, NotificationRequestAcceptedDetailsInt.class);

        Assertions.assertEquals(timelineElementDetails.getIdempotenceToken(), details.getIdempotenceToken());
        Assertions.assertEquals(timelineElementDetails.getNotificationRequestId(), details.getNotificationRequestId());
        Assertions.assertEquals(timelineElementDetails.getPaProtocolNumber(), details.getPaProtocolNumber());
    }

    @Test
    void mapToClassWithNullSource() {
        TimelineElementInternal source = null;

        TimelineElementInternal ret = SmartMapper.mapToClass(source, TimelineElementInternal.class);

        Assertions.assertNull(ret);
    }

    @Test
    void mapToClassWithObjectMappperWithNullSource() {
        TimelineElementInternal source = null;

        TimelineElementDetails ret = smartMapper.mapToClassWithObjectMapper(source, TimelineElementDetails.class);

        Assertions.assertNull(ret);
    }
}