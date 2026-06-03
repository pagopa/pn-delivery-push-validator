package it.pagopa.pn.deliverypushvalidator.middleware.queue.consumer.handler.action;

import it.pagopa.pn.deliverypushvalidator.action.startworkflow.PostValidationCompletedHandler;
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
public class PostValidationCompletedActionHandler extends AbstractActionEventHandler {

    private final PostValidationCompletedHandler postValidationCompletedHandler;

    public PostValidationCompletedActionHandler(TimelineUtils timelineUtils, PostValidationCompletedHandler postValidationCompletedHandler) {
        super(timelineUtils);
        this.postValidationCompletedHandler = postValidationCompletedHandler;
    }

    @Override
    public SupportedEventType getSupportedEventType() {
        return SupportedEventType.POST_VALIDATION_COMPLETED;
    }

    @Override
    public void handle(Action action, MessageHeaders headers) {
        final String processName = ActionType.POST_VALIDATION_COMPLETED.name();
        try {
            log.debug("Handle action of type POST_VALIDATION_COMPLETED, with payload {}", action);
            HandleEventUtils.addIunAndCorrIdToMdc(action.getIun(), action.getActionId());
            log.logStartingProcess(processName);

            checkNotificationCancelledAndExecute(
                    action,
                    a -> postValidationCompletedHandler.acceptNotification(a.getIun())
            );

            log.logEndingProcess(processName);
        } catch (Exception ex) {
            log.logEndingProcess(processName, false, ex.getMessage(), ex);
            HandleEventUtils.handleException(headers, ex);
            throw ex;
        }
    }
}

