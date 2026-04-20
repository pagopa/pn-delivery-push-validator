package it.pagopa.pn.deliverypushvalidator.middleware.queue.consumer;

import io.awspring.cloud.sqs.annotation.SqsListener;
import it.pagopa.pn.commons.exceptions.PnInternalException;
import it.pagopa.pn.deliverypushvalidator.config.PnDeliveryPushValidatorConfigs;
import it.pagopa.pn.deliverypushvalidator.middleware.queue.consumer.handler.utils.HandleEventUtils;
import it.pagopa.pn.deliverypushvalidator.middleware.queue.consumer.router.EventRouter;
import it.pagopa.pn.deliverypushvalidator.middleware.queue.producer.abstractions.actionspool.Action;
import lombok.CustomLog;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;

import static it.pagopa.pn.deliverypushvalidator.exception.PnDeliveryPushValidatorExceptionCodes.ERROR_CODE_DELIVERYPUSH_ACTIONTYPENOTSUPPORTED;
import static it.pagopa.pn.deliverypushvalidator.middleware.queue.utils.ChannelUtils.setMdc;

@Configuration
@CustomLog
@RequiredArgsConstructor
public class ValidationActionsConsumer {
    private PnDeliveryPushValidatorConfigs pnDeliveryPushValidatorConfigs;
    private final EventRouter eventRouter;

    @SqsListener(queueNames = "#{@pnDeliveryPushValidatorConfigs.topics.validationActions}")
    public void pnDeliveryPushValidationActionsInboundConsumer(Message<Action> message) {
        setMdc(message);
        final String processName = "VALIDATION_ACTIONS_INBOUND";
        try {
            log.info("Handle action pnDeliveryPushValidationActionsInboundConsumer, with content {}", message);
            String actionType = extractActionType(message.getPayload());
            EventRouter.RoutingConfig routerConfig = EventRouter.RoutingConfig.builder()
                    .eventType(actionType)
                    .build();
            eventRouter.route(message, routerConfig);
            log.logEndingProcess(processName);
        } catch (Exception ex) {
            log.logEndingProcess(processName, false, ex.getMessage(),ex);
            HandleEventUtils.handleException(message.getHeaders(), ex);
            throw ex;
        }
    }
    private String extractActionType(Action action) {
        String actionType = action.getType() != null ? action.getType().name() : null;
        if (actionType == null) {
            log.error("actionType not present, cannot start scheduled action");
            throw new PnInternalException("actionType not present, cannot start scheduled action", ERROR_CODE_DELIVERYPUSH_ACTIONTYPENOTSUPPORTED);
        }
        return actionType;
    }
}
