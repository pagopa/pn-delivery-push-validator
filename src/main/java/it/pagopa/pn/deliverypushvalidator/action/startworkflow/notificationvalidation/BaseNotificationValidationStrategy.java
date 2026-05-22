package it.pagopa.pn.deliverypushvalidator.action.startworkflow.notificationvalidation;

import it.pagopa.pn.commons.exceptions.PnValidationException;
import it.pagopa.pn.deliverypushvalidator.action.details.NotificationRefusedActionDetails;
import it.pagopa.pn.deliverypushvalidator.action.details.NotificationValidationActionDetails;
import it.pagopa.pn.deliverypushvalidator.config.PnDeliveryPushValidatorConfigs;
import it.pagopa.pn.deliverypushvalidator.dto.ext.delivery.notification.NotificationInt;
import it.pagopa.pn.deliverypushvalidator.dto.ext.delivery.notification.CommunicationType;
import it.pagopa.pn.deliverypushvalidator.dto.timeline.NotificationRefusedErrorInt;
import it.pagopa.pn.deliverypushvalidator.exception.PnLookupAddressValidationFailedException;
import it.pagopa.pn.deliverypushvalidator.exception.PnValidationFileNotFoundException;
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
    private final PnDeliveryPushValidatorConfigs cfg;

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
                NotificationRefusedErrorInt.NotificationRefusedErrorIntBuilder builder = NotificationRefusedErrorInt.builder()
                        .errorCode(elem.getCode())
                        .detail(elem.getDetail());
                if (Objects.nonNull(elem.getElement())) {
                    builder.recIndex(Integer.valueOf(elem.getElement()));
                }
                errors.add(builder.build());
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

    protected void handlePnValidationFileNotFoundException(String iun, NotificationValidationActionDetails details, NotificationInt notification, PnValidationFileNotFoundException ex, Instant startWorkflowTime) {
    /* Per la PnValidationFileNotFoundException la notifica non viene portata in rifiutata MA è prevista una gestione ad hoc. Questo avviene
       perchè al momento non c'è possibilità di distinguere un 404 dovuto ad un mancato caricamento file da parte della PA (che dovrebbe portare
       regolarmente la notifica in rifiutata) e un 404 dovuto ad un ritardo nel caricamento del file nel bucket corretto da parte di
       safeStorage (in questo caso si di deve procedere con i ritentativi). Si sceglie dunque per ore di ritentare in entrambi i casi
    */
        log.warn(String.format("File not found exception in validateNotification - iun=%s", iun), ex);
        if (cfg.isSafeStorageFileNotFoundRetry()) {
            log.info("Notification validation need to be rescheduled  - iun={}", iun);
            notificationValidationScheduler.scheduleNotificationValidation(notification, details.getRetryAttempt(), ex, startWorkflowTime);
        } else {
            handleValidationError(notification, ex);
        }
    }
}
