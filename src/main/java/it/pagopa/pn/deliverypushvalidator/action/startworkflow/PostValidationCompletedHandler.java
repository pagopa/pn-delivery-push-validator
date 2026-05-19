package it.pagopa.pn.deliverypushvalidator.action.startworkflow;
import it.pagopa.pn.deliverypushvalidator.action.startworkflow.notificationvalidation.AttachmentUtils;
import it.pagopa.pn.deliverypushvalidator.action.utils.TimelineUtils;
import it.pagopa.pn.deliverypushvalidator.dto.ext.delivery.notification.NotificationInt;
import it.pagopa.pn.deliverypushvalidator.dto.timeline.CommunicationType;
import it.pagopa.pn.deliverypushvalidator.dto.timeline.TimelineElementInternal;
import it.pagopa.pn.deliverypushvalidator.middleware.queue.producer.abstractions.actionspool.ActionType;
import it.pagopa.pn.deliverypushvalidator.service.NotificationService;
import it.pagopa.pn.deliverypushvalidator.service.SchedulerService;
import it.pagopa.pn.deliverypushvalidator.service.TimelineService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.time.Instant;
@Component
@AllArgsConstructor
@Slf4j
public class PostValidationCompletedHandler {
    private final CheckAttachmentRetentionScheduler checkAttachmentRetentionScheduler;
    private final AttachmentUtils attachmentUtils;
    private final NotificationService notificationService;
    private final TimelineUtils timelineUtils;
    private final TimelineService timelineService;
    private final SchedulerService schedulerService;

    public void acceptNotification(String iun) {
        NotificationInt notification = notificationService.getNotificationByIun(iun);
        checkAttachmentRetentionScheduler.scheduleCheckAttachmentRetentionBeforeExpiration(iun, CommunicationType.INFORMAL);
        attachmentUtils.changeAttachmentsStatusToAttached(notification);
        TimelineElementInternal acceptedTimelineElement = timelineUtils.buildAcceptedRequestTimelineElement(notification);
        timelineService.addTimelineElement(acceptedTimelineElement, notification);
        schedulerService.scheduleEvent(iun, Instant.now(), ActionType.POST_ACCEPTED_PROCESSING_COMPLETED, null, CommunicationType.INFORMAL);
        log.debug("Scheduled POST_ACCEPTED_PROCESSING_COMPLETED for iun={} communicationType={}", iun, CommunicationType.INFORMAL);
    }
}
