package it.pagopa.pn.deliverypushvalidator.service.impl;

import it.pagopa.pn.deliverypushvalidator.action.details.DocumentCreationResponseActionDetails;
import it.pagopa.pn.deliverypushvalidator.action.utils.TimelineUtils;
import it.pagopa.pn.deliverypushvalidator.dto.documentcreation.DocumentCreationTypeInt;
import it.pagopa.pn.deliverypushvalidator.dto.timeline.CommunicationType;
import it.pagopa.pn.deliverypushvalidator.middleware.queue.producer.abstractions.actionspool.Action;
import it.pagopa.pn.deliverypushvalidator.middleware.queue.producer.abstractions.actionspool.ActionDetails;
import it.pagopa.pn.deliverypushvalidator.middleware.queue.producer.abstractions.actionspool.ActionType;
import it.pagopa.pn.deliverypushvalidator.middleware.queue.producer.abstractions.actionspool.ActionsPool;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;

@ExtendWith(SpringExtension.class)
class SchedulerServiceImplTest {

    private ActionsPool actionsPool;

    @Mock
    private TimelineUtils timelineUtils;

    
    private SchedulerServiceImpl schedulerService;
    
    @BeforeEach
    void setup() {
        actionsPool = Mockito.mock(ActionsPool.class);

        schedulerService = new SchedulerServiceImpl(actionsPool, timelineUtils);
    }

    @Test
    void scheduleEvent2(){
        Action action = buildAction(ActionType.DOCUMENT_CREATION_RESPONSE);
        ActionDetails actionDetails = DocumentCreationResponseActionDetails.builder()
                .documentCreationType(DocumentCreationTypeInt.NOTIFICATION_CANCELLED.getValue())
                .key("key")
                .timelineId("timelineId")
                .build();
        Instant instant = Instant.parse("2022-08-30T16:04:13.913859900Z");

        Mockito.when(timelineUtils.checkIsNotificationCancellationRequested(action.getIun()))
                .thenReturn(false);

        schedulerService.scheduleEvent("01", instant, ActionType.DOCUMENT_CREATION_RESPONSE,actionDetails);

        Mockito.verify(actionsPool, Mockito.times(1)).addOnlyAction(any(Action.class));
    }

    @Test
    void scheduleEvent1(){
        Action action = buildAction(ActionType.CHECK_ATTACHMENT_RETENTION);
        Instant instant = Instant.parse("2022-08-30T16:04:13.913859900Z");

        Mockito.when(timelineUtils.checkIsNotificationCancellationRequested(action.getIun()))
                .thenReturn(false);

        schedulerService.scheduleEvent("01", instant, ActionType.CHECK_ATTACHMENT_RETENTION);

        Mockito.verify(actionsPool, Mockito.times(1)).addOnlyAction(any(Action.class));
    }

    private Action buildAction(ActionType type) {

        Instant instant = Instant.parse("2022-08-30T16:04:13.913859900Z");

        return Action.builder()
                .iun("01")
                .actionId("01_analog_workflow_e_3")
                .notBefore(instant)
                .type(type)
                .recipientIndex(3)
                .communicationType(CommunicationType.INFORMAL)
                .build();
    }


}