package it.pagopa.pn.deliverypushvalidator.action.refused;

import it.pagopa.pn.deliverypushvalidator.action.utils.TimelineUtils;
import it.pagopa.pn.deliverypushvalidator.dto.ext.delivery.notification.NotificationInt;
import it.pagopa.pn.deliverypushvalidator.dto.timeline.NotificationRefusedErrorInt;
import it.pagopa.pn.deliverypushvalidator.service.NotificationService;
import it.pagopa.pn.deliverypushvalidator.service.TimelineService;
import lombok.AllArgsConstructor;
import lombok.CustomLog;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Component
@AllArgsConstructor
@CustomLog
public class InformalNotificationRefusedStrategy implements NotificationRefusedStrategy {

    private final NotificationService notificationService;
    private final TimelineUtils timelineUtils;
    private final TimelineService timelineService;

    @Override
    public NotificationInt getNotification(String iun) {
        return notificationService.getInformalNotificationByIun(iun);
    }

    @Override
    public void handleNotificationRefused(String iun, List<NotificationRefusedErrorInt> errors, Instant notBefore) {
        log.debug("Start handleNotificationRefused for informal notification - iun={}, notBefore={}", iun, notBefore);

        NotificationInt notification = getNotification(iun);

        timelineService.addTimelineElement(
                timelineUtils.buildRefusedRequestTimelineElement(notification, errors, null),
                notification
        );
    }
}

