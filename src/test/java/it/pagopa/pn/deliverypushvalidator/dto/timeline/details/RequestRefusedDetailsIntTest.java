package it.pagopa.pn.deliverypushvalidator.dto.timeline.details;


import it.pagopa.pn.deliverypushvalidator.dto.timeline.NotificationRefusedErrorInt;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

class RequestRefusedDetailsIntTest {

    private RequestRefusedDetailsInt request;

    @BeforeEach
    void setup() {
        List<NotificationRefusedErrorInt> errors = new ArrayList<>();
        NotificationRefusedErrorInt notificationRefusedError = NotificationRefusedErrorInt.builder()
                .errorCode("FILE_NOTFOUND")
                .detail("details")
                .build();
        errors.add(notificationRefusedError);

        request = RequestRefusedDetailsInt.builder()
                .refusalReasons(errors)
                .paProtocolNumber("paProtocolNumber")
                .idempotenceToken("idempotenceToken")
                .notificationRequestId("notificationRequestId")
                .build();
    }

    @Test
    void toLog() {
        String log = request.toLog();
        Assertions.assertEquals("errors=[NotificationRefusedErrorInt(errorCode=FILE_NOTFOUND, detail=details, recIndex=null)], notificationRequestId=notificationRequestId, paProtocolNumber=paProtocolNumber, idempotenceToken=idempotenceToken", log);
    }

    @Test
    void getErrors() {
        List<NotificationRefusedErrorInt> actualErrors = request.getRefusalReasons();
        Assertions.assertEquals(1, actualErrors.size());
    }

}