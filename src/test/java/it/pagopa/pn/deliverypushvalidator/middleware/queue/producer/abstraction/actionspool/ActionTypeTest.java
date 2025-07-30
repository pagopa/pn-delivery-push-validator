package it.pagopa.pn.deliverypushvalidator.middleware.queue.producer.abstraction.actionspool;

import it.pagopa.pn.deliverypushvalidator.middleware.queue.producer.abstractions.actionspool.Action;
import it.pagopa.pn.deliverypushvalidator.middleware.queue.producer.abstractions.actionspool.ActionType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ActionTypeTest {

    @Test
    void buildActionId() {

        Action action = Action.builder()
                .iun("1")
                .actionId("1")
                .recipientIndex(1)
                .timelineId("tim123")
                .build();

        Assertions.assertAll(
                () -> Assertions.assertEquals("1_start", ActionType.SENDER_ACK.buildActionId(action))
        );
    }

}