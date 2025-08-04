package it.pagopa.pn.deliverypushvalidator.service.mapper;

import it.pagopa.pn.deliverypushvalidator.dto.ext.delivery.NotificationCostResponseInt;
import it.pagopa.pn.deliverypushvalidator.generated.openapi.msclient.delivery.model.NotificationCostResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class NotificationCostResponseMapperTest {

    @Test
    void externalToInternal() {
        NotificationCostResponseInt actual = NotificationCostResponseMapper.externalToInternal(buildNotificationCostResponse());

        Assertions.assertEquals(buildNotificationCostResponseInt(), actual);

    }

    private NotificationCostResponse buildNotificationCostResponse() {
        NotificationCostResponse response = new NotificationCostResponse();
        response.setIun("001");
        response.setRecipientIdx(2);
        return response;
    }

    private NotificationCostResponseInt buildNotificationCostResponseInt() {
        return NotificationCostResponseInt.builder()
                .iun("001")
                .recipientIdx(2)
                .build();
    }
}