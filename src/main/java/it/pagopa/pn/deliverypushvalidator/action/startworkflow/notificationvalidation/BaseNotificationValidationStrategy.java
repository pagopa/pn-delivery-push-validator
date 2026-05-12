package it.pagopa.pn.deliverypushvalidator.action.startworkflow.notificationvalidation;

import it.pagopa.pn.commons.exceptions.PnValidationException;
import it.pagopa.pn.deliverypushvalidator.action.details.NotificationRefusedActionDetails;
import it.pagopa.pn.deliverypushvalidator.action.details.NotificationValidationActionDetails;
import it.pagopa.pn.deliverypushvalidator.dto.ext.delivery.notification.NotificationInt;
import it.pagopa.pn.deliverypushvalidator.dto.timeline.CommunicationType;
import it.pagopa.pn.deliverypushvalidator.dto.timeline.NotificationRefusedErrorInt;
import it.pagopa.pn.deliverypushvalidator.exception.PnLookupAddressValidationFailedException;
import it.pagopa.pn.deliverypushvalidator.middleware.queue.producer.abstractions.actionspool.ActionType;
import it.pagopa.pn.deliverypushvalidator.service.SchedulerService;
import lombok.AllArgsConstructor;
import lombok.CustomLog;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@AllArgsConstructor
@CustomLog
public class BaseNotificationValidationStrategy {

    private final NotificationValidationScheduler notificationValidationScheduler;
    private final SchedulerService schedulerService;

    protected void handleValidationError(NotificationInt notification, PnValidationException ex) {
        List<NotificationRefusedErrorInt> errors = new ArrayList<>();
        if (Objects.nonNull(ex.getProblem())) {
            ex.getProblem().getErrors().forEach(elem -> {
                NotificationRefusedErrorInt notificationRefusedError = NotificationRefusedErrorInt.builder().errorCode(elem.getCode()).detail(elem.getDetail()).build();

                errors.add(notificationRefusedError);
            });
        }
        log.info("Notification refused, errors {} - iun {}", errors, notification.getIun());
        scheduleNotificationRefused(notification.getIun(), errors, notification.getCommunicationType());
    }

    protected void handleRuntimeException(String iun, NotificationValidationActionDetails details, NotificationInt notification, RuntimeException ex, Instant startWorkflowTime) {
        log.warn(String.format("RuntimeException in validateNotification - iun=%s", iun), ex);
        log.info("Notification validation need to be rescheduled for ex={} - iun={}", ex, iun);
        notificationValidationScheduler.scheduleNotificationValidation(notification, details.getRetryAttempt(), ex, startWorkflowTime);
    }

    protected void handleLookupAddressValidationError(NotificationInt notification, PnLookupAddressValidationFailedException ex) {
        List<NotificationRefusedErrorInt> errors = new ArrayList<>();
        if (Objects.nonNull(ex.getProblem())) {
            ex.getProblem().getErrors().forEach(elem -> {
                assert elem.getElement() != null;
                NotificationRefusedErrorInt notificationRefusedError = NotificationRefusedErrorInt.builder().errorCode(elem.getCode()).detail(elem.getDetail()).recIndex(Integer.valueOf(elem.getElement())).build();

                errors.add(notificationRefusedError);
            });
        }
        log.info("Notification refused by lookupAddress error validation, errors {} - iun {}", errors, notification.getIun());
        scheduleNotificationRefused(notification.getIun(), errors, notification.getCommunicationType());
    }

    protected void scheduleNotificationRefused(String iun, List<NotificationRefusedErrorInt> errors, CommunicationType communicationType) {
        Instant schedulingDate = Instant.now();

        NotificationRefusedActionDetails details = NotificationRefusedActionDetails.builder().errors(errors).build();

        log.debug("Scheduling Notification refused schedulingDate={} - iun={}", schedulingDate, iun);
        schedulerService.scheduleEvent(iun, schedulingDate, ActionType.NOTIFICATION_REFUSED, details, communicationType);
    }
}
