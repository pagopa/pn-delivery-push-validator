package it.pagopa.pn.deliverypushvalidator.middleware.queue.consumer.handler.action;

import it.pagopa.pn.deliverypushvalidator.action.details.NotificationValidationActionDetails;
import it.pagopa.pn.deliverypushvalidator.action.startworkflow.notificationvalidation.NotificationValidationActionHandler;
import it.pagopa.pn.deliverypushvalidator.action.utils.TimelineUtils;
import it.pagopa.pn.deliverypushvalidator.middleware.queue.consumer.handler.utils.HandleEventUtils;
import it.pagopa.pn.deliverypushvalidator.middleware.queue.consumer.router.SupportedEventType;
import it.pagopa.pn.deliverypushvalidator.middleware.queue.producer.abstractions.actionspool.Action;
import it.pagopa.pn.deliverypushvalidator.middleware.queue.producer.abstractions.actionspool.ActionType;
import lombok.CustomLog;
import org.springframework.messaging.MessageHeaders;
import org.springframework.stereotype.Component;

@Component
@CustomLog
public class NotificationValidationHandler extends AbstractActionEventHandler {
    private final NotificationValidationActionHandler notificationValidationActionHandler;

    public NotificationValidationHandler(TimelineUtils timelineUtils, NotificationValidationActionHandler notificationValidationActionHandler) {
        super(timelineUtils);
        this.notificationValidationActionHandler = notificationValidationActionHandler;
    }

    @Override
    public SupportedEventType getSupportedEventType() {
        return SupportedEventType.NOTIFICATION_VALIDATION;
    }

    @Override
    public void handle(Action action, MessageHeaders headers) {
        final String processName = ActionType.NOTIFICATION_VALIDATION.name();
        try {
            log.debug("Handle action of type NOTIFICATION_VALIDATION, with payload {}", action);
            HandleEventUtils.addIunAndCorrIdToMdc(action.getIun(), action.getActionId());
            log.logStartingProcess(processName);

            checkNotificationCancelledAndExecute(
                    action,
                    a -> notificationValidationActionHandler.validateNotification(a.getIun(), (NotificationValidationActionDetails) a.getDetails() )
            );
            log.logEndingProcess(processName);
        } catch (Exception ex) {
            log.logEndingProcess(processName, false, ex.getMessage());
            HandleEventUtils.handleException(headers, ex);
            throw ex;
        }
    }

}
