package it.pagopa.pn.deliverypushvalidator.action.startworkflow;

import it.pagopa.pn.deliverypushvalidator.action.startworkflow.notificationvalidation.AttachmentUtils;
import it.pagopa.pn.deliverypushvalidator.action.utils.TimelineUtils;
import it.pagopa.pn.deliverypushvalidator.dto.ext.delivery.notification.NotificationInt;
import it.pagopa.pn.deliverypushvalidator.dto.timeline.TimelineElementInternal;
import it.pagopa.pn.deliverypushvalidator.dto.timeline.details.NotificationRequestAcceptedDetailsInt;
import it.pagopa.pn.deliverypushvalidator.dto.timeline.details.TimelineElementCategoryInt;
import it.pagopa.pn.deliverypushvalidator.dto.timeline.EventId;
import it.pagopa.pn.deliverypushvalidator.dto.timeline.TimelineEventId;
import it.pagopa.pn.deliverypushvalidator.middleware.queue.producer.abstractions.actionspool.ActionType;
import it.pagopa.pn.deliverypushvalidator.service.NotificationService;
import it.pagopa.pn.deliverypushvalidator.service.SchedulerService;
import it.pagopa.pn.deliverypushvalidator.service.TimelineService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Base64;

/**
 * Handles post-validation for INFORMAL notifications.
 * Unlike LEGAL notifications, no AAR (legalFact) is generated.
 * The notification is accepted and attachments are marked as ATTACHED.
 */
@Component
@AllArgsConstructor
@Slf4j
public class InformalNotificationAcceptedHandler {

    private final NotificationService notificationService;
    private final AttachmentUtils attachmentUtils;
    private final TimelineService timelineService;
    private final TimelineUtils timelineUtils;
    private final SchedulerService schedulerService;

    public void handleInformalNotificationAccepted(String iun) {
        log.info("Start handleInformalNotificationAccepted - iun={}", iun);
        NotificationInt notification = notificationService.getNotificationByIun(iun);

        // Mark attachments as ATTACHED (same as LEGAL flow)
        attachmentUtils.changeAttachmentsStatusToAttached(notification);

        // Build REQUEST_ACCEPTED timeline element without legalFactId (no AAR for INFORMAL)
        String elementId = TimelineEventId.REQUEST_ACCEPTED.buildEventId(
                EventId.builder()
                        .iun(notification.getIun())
                        .build());

        NotificationRequestAcceptedDetailsInt details = NotificationRequestAcceptedDetailsInt.builder()
                .paProtocolNumber(notification.getPaProtocolNumber())
                .idempotenceToken(notification.getIdempotenceToken())
                .notificationRequestId(Base64.getEncoder().encodeToString(notification.getIun().getBytes()))
                .build();

        TimelineElementInternal timelineElement = timelineUtils.buildTimeline(
                notification,
                TimelineElementCategoryInt.REQUEST_ACCEPTED,
                elementId,
                details
        );

        timelineService.addTimelineElement(timelineElement, notification);

        // Schedule post-accepted processing
        log.debug("Scheduling POST_ACCEPTED_PROCESSING_COMPLETED for INFORMAL - iun={}", iun);
        schedulerService.scheduleEvent(iun, Instant.now(), ActionType.POST_ACCEPTED_PROCESSING_COMPLETED);

        log.info("End handleInformalNotificationAccepted - iun={}", iun);
    }
}

