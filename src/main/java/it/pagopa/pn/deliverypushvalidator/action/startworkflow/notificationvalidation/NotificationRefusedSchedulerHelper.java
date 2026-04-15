package it.pagopa.pn.deliverypushvalidator.action.startworkflow.notificationvalidation;

import it.pagopa.pn.deliverypushvalidator.action.details.NotificationRefusedActionDetails;
import it.pagopa.pn.deliverypushvalidator.dto.timeline.NotificationRefusedErrorInt;
import it.pagopa.pn.deliverypushvalidator.middleware.queue.producer.abstractions.actionspool.ActionType;
import it.pagopa.pn.deliverypushvalidator.service.SchedulerService;
import lombok.AllArgsConstructor;
import lombok.CustomLog;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

/**
 * Shared helper to schedule notification refusal.
 * Used by both LEGAL and INFORMAL validation strategies to avoid circular dependencies.
 */
@Component
@AllArgsConstructor
@CustomLog
public class NotificationRefusedSchedulerHelper {

    private final SchedulerService schedulerService;

    public void scheduleNotificationRefused(String iun, List<NotificationRefusedErrorInt> errors) {
        Instant schedulingDate = Instant.now();
        NotificationRefusedActionDetails details = NotificationRefusedActionDetails.builder()
                .errors(errors)
                .build();
        log.debug("Scheduling Notification refused schedulingDate={} - iun={}", schedulingDate, iun);
        schedulerService.scheduleEvent(iun, schedulingDate, ActionType.NOTIFICATION_REFUSED, details);
    }
}

