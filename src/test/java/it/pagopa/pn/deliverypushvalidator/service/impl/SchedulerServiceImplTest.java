package it.pagopa.pn.deliverypushvalidator.service.impl;

import it.pagopa.pn.deliverypushvalidator.action.details.DocumentCreationResponseActionDetails;
import it.pagopa.pn.deliverypushvalidator.action.utils.TimelineUtils;
import it.pagopa.pn.deliverypushvalidator.dto.documentcreation.DocumentCreationTypeInt;
import it.pagopa.pn.deliverypushvalidator.dto.ext.delivery.notification.CommunicationType;
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
    void scheduleEvent_withCommunicationType_shouldAddAction() {
        String iun = "IUN01";
        Instant dateToSchedule = Instant.parse("2022-08-30T16:04:13.913859900Z");
        ActionType actionType = ActionType.POST_ACCEPTED_PROCESSING_COMPLETED;
        CommunicationType communicationType = CommunicationType.LEGAL;

        Mockito.when(timelineUtils.checkIsNotificationCancellationRequested(iun)).thenReturn(false);

        schedulerService.scheduleEvent(iun, dateToSchedule, actionType, communicationType);

        Mockito.verify(actionsPool).addOnlyAction(any(Action.class));
    }

    @Test
    void scheduleEvent_withActionDetails_shouldAddAction() {
        String iun = "IUN01";
        Instant dateToSchedule = Instant.parse("2022-08-30T16:04:13.913859900Z");
        ActionType actionType = ActionType.POST_ACCEPTED_PROCESSING_COMPLETED;
        ActionDetails actionDetails = Mockito.mock(ActionDetails.class);
        CommunicationType communicationType = CommunicationType.INFORMAL;

        Mockito.when(timelineUtils.checkIsNotificationCancellationRequested(iun)).thenReturn(false);

        schedulerService.scheduleEvent(iun, dateToSchedule, actionType, actionDetails, communicationType);

        Mockito.verify(actionsPool).addOnlyAction(any(Action.class));
    }

    @Test
    void scheduleEvent_whenNotificationCancelled_shouldNotAddAction() {
        String iun = "IUN01";
        Instant dateToSchedule = Instant.parse("2022-08-30T16:04:13.913859900Z");
        ActionType actionType = ActionType.POST_ACCEPTED_PROCESSING_COMPLETED;
        CommunicationType communicationType = CommunicationType.LEGAL;

        Mockito.when(timelineUtils.checkIsNotificationCancellationRequested(iun)).thenReturn(true);

        schedulerService.scheduleEvent(iun, dateToSchedule, actionType, communicationType);

        Mockito.verify(actionsPool, Mockito.never()).addOnlyAction(any(Action.class));
    }

    @Test
    void scheduleEvent_whenNotificationCancelledButActionIsForCancellation_shouldAddAction() {
        String iun = "IUN01";
        Instant dateToSchedule = Instant.parse("2022-08-30T16:04:13.913859900Z");
        ActionType actionType = ActionType.POST_ACCEPTED_PROCESSING_COMPLETED;
        DocumentCreationResponseActionDetails actionDetails = Mockito.mock(DocumentCreationResponseActionDetails.class);
        Mockito.when(actionDetails.getDocumentCreationType()).thenReturn(DocumentCreationTypeInt.NOTIFICATION_CANCELLED.getValue());

        Mockito.when(timelineUtils.checkIsNotificationCancellationRequested(iun)).thenReturn(true);

        schedulerService.scheduleEvent(iun, null, dateToSchedule, actionType, null, actionDetails, null);

        Mockito.verify(actionsPool).addOnlyAction(any(Action.class));
    }

    @Test
    void scheduleEvent_fullSignature_shouldBuildActionWithCorrectFields() {
        String iun = "IUN01";
        Integer recIndex = 3;
        Instant dateToSchedule = Instant.parse("2022-08-30T16:04:13.913859900Z");
        ActionType actionType = ActionType.POST_ACCEPTED_PROCESSING_COMPLETED;
        String timelineEventId = "timeline_01";
        CommunicationType communicationType = CommunicationType.LEGAL;

        Mockito.when(timelineUtils.checkIsNotificationCancellationRequested(iun)).thenReturn(false);

        schedulerService.scheduleEvent(iun, recIndex, dateToSchedule, actionType, timelineEventId, null, communicationType);

        Mockito.verify(actionsPool).addOnlyAction(Mockito.argThat(action ->
                iun.equals(action.getIun()) &&
                        recIndex.equals(action.getRecipientIndex()) &&
                        dateToSchedule.equals(action.getNotBefore()) &&
                        actionType.equals(action.getType()) &&
                        timelineEventId.equals(action.getTimelineId()) &&
                        communicationType.equals(action.getCommunicationType()) &&
                        action.getActionId() != null
        ));
    }


}