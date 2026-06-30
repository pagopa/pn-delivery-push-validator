package it.pagopa.pn.deliverypushvalidator.middleware.queue.consumer.handler.action;


import it.pagopa.pn.deliverypushvalidator.action.details.NotificationRefusedActionDetails;
import it.pagopa.pn.deliverypushvalidator.action.refused.NotificationRefusedActionHandler;
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
public class NotificationRefusedHandler extends AbstractActionEventHandler {
    private final NotificationRefusedActionHandler notificationRefusedActionHandler;

    public NotificationRefusedHandler(TimelineUtils timelineUtils, NotificationRefusedActionHandler notificationRefusedActionHandler) {
        super(timelineUtils);
        this.notificationRefusedActionHandler = notificationRefusedActionHandler;
    }

    @Override
    public SupportedEventType getSupportedEventType() {
        return SupportedEventType.NOTIFICATION_REFUSED;
    }

    @Override
    public void handle(Action action, MessageHeaders headers) {
        final String processName = ActionType.NOTIFICATION_REFUSED.name();
        try {
            log.debug("Handle action of type NOTIFICATION_REFUSED, with payload {}", action);
            HandleEventUtils.addIunAndCorrIdToMdc(action.getIun(), action.getActionId());

            log.logStartingProcess(processName);
            NotificationRefusedActionDetails details = (NotificationRefusedActionDetails) action.getDetails();

            notificationRefusedActionHandler.notificationRefusedHandler(action.getIun(), details.getErrors(), action.getNotBefore(), action.getCommunicationType());
            log.logEndingProcess(processName);
        } catch (Exception ex) {
            log.logEndingProcess(processName, false, ex.getMessage(),ex);
            HandleEventUtils.handleException(headers, ex);
            throw ex;
        }
    }

}
