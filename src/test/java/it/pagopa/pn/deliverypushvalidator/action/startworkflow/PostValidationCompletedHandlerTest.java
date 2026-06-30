package it.pagopa.pn.deliverypushvalidator.action.startworkflow;

import it.pagopa.pn.deliverypushvalidator.action.startworkflow.notificationvalidation.AttachmentUtils;
import it.pagopa.pn.deliverypushvalidator.action.utils.TimelineUtils;
import it.pagopa.pn.deliverypushvalidator.dto.ext.delivery.notification.NotificationInt;
import it.pagopa.pn.deliverypushvalidator.dto.ext.delivery.notification.CommunicationType;
import it.pagopa.pn.deliverypushvalidator.dto.timeline.TimelineElementInternal;
import it.pagopa.pn.deliverypushvalidator.middleware.queue.producer.abstractions.actionspool.ActionType;
import it.pagopa.pn.deliverypushvalidator.service.NotificationService;
import it.pagopa.pn.deliverypushvalidator.service.SchedulerService;
import it.pagopa.pn.deliverypushvalidator.service.TimelineService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PostValidationCompletedHandlerTest {

    @Mock
    private CheckAttachmentRetentionScheduler checkAttachmentRetentionScheduler;
    @Mock
    private AttachmentUtils attachmentUtils;
    @Mock
    private NotificationService notificationService;
    @Mock
    private TimelineUtils timelineUtils;
    @Mock
    private TimelineService timelineService;
    @Mock
    private SchedulerService schedulerService;

    private PostValidationCompletedHandler handler;

    @BeforeEach
    void setup() {
        handler = new PostValidationCompletedHandler(
                checkAttachmentRetentionScheduler,
                attachmentUtils,
                notificationService,
                timelineUtils,
                timelineService,
                schedulerService
        );
    }

    @Test
    void acceptNotificationExecutesInformalAcceptanceFlow() {
        String iun = "IUN_123";
        NotificationInt notification = NotificationInt.builder().iun(iun).build();
        TimelineElementInternal timelineElementInternal = TimelineElementInternal.builder().elementId("REQUEST_ACCEPTED").build();

        Mockito.when(notificationService.getInformalNotificationByIun(iun)).thenReturn(notification);
        Mockito.when(timelineUtils.buildAcceptedRequestTimelineElement(notification, null)).thenReturn(timelineElementInternal);

        handler.acceptNotification(iun);

        Mockito.verify(notificationService).getInformalNotificationByIun(iun);
        Mockito.verify(checkAttachmentRetentionScheduler)
                .scheduleCheckAttachmentRetentionBeforeExpiration(iun, CommunicationType.INFORMAL);
        Mockito.verify(attachmentUtils).changeAttachmentsStatusToAttached(notification);
        Mockito.verify(timelineService).addTimelineElement(timelineElementInternal, notification);
        Mockito.verify(schedulerService).scheduleEvent(
                Mockito.eq(iun),
                Mockito.any(),
                Mockito.eq(ActionType.POST_ACCEPTED_PROCESSING_COMPLETED),
                Mockito.isNull(),
                Mockito.eq(CommunicationType.INFORMAL)
        );
    }
}

