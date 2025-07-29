package it.pagopa.pn.deliverypushvalidator.dto.timeline.details;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

class NotificationCancellationRequestDetailsIntTest {
    private NotificationCancellationRequestDetailsInt detailsInt;
    private String cancellationRequestId;

    @BeforeEach
    void setUp() {
        cancellationRequestId = UUID.randomUUID().toString();
        detailsInt = new NotificationCancellationRequestDetailsInt();
        detailsInt.setCancellationRequestId(cancellationRequestId);
    }

    @Test
    void toLog() {
        String expectedValue = String.format("cancellationRequestId=%s", this.cancellationRequestId);
        Assertions.assertEquals(expectedValue, detailsInt.toLog());
    }

}
