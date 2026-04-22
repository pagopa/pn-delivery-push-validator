package it.pagopa.pn.deliverypushvalidator.middleware.queue.consumer;

import io.awspring.cloud.sqs.annotation.SqsListener;
import it.pagopa.pn.api.dto.events.notificationcost.validation.PnNotificationCostValidationEvent;
import it.pagopa.pn.deliverypushvalidator.action.startworkflow.notificationvalidation.NotificationValidationActionHandler;
import it.pagopa.pn.deliverypushvalidator.config.PnDeliveryPushValidatorConfigs;
import it.pagopa.pn.deliverypushvalidator.middleware.externalclient.pnclient.notificationcostservice.NotificationCostServiceClient;
import it.pagopa.pn.deliverypushvalidator.middleware.queue.consumer.handler.utils.HandleEventUtils;
import lombok.AllArgsConstructor;
import lombok.CustomLog;
import org.springframework.stereotype.Component;
import org.springframework.messaging.Message;

import static it.pagopa.pn.deliverypushvalidator.middleware.queue.utils.ChannelUtils.setMdc;

@Component
@AllArgsConstructor
@CustomLog
public class NotificationCostConsumer {
    private PnDeliveryPushValidatorConfigs pnDeliveryPushValidatorConfigs;
    private final NotificationValidationActionHandler handler;

    @SqsListener(queueNames = "#{@pnDeliveryPushValidatorConfigs.topics.pnNotificationCostToDeliveryPushValidatorEvents}")
    public void pnNotificationCostEventInboundConsumer(Message<PnNotificationCostValidationEvent.Detail> message) {
        setMdc(message);
        final String processName = "NOTIFICATION COST";
        try {
            log.info("Handle message from {} with content {}", NotificationCostServiceClient.CLIENT_NAME, message);

            String iun = message.getPayload().getPnNotificationCostValidationPayload().getIun();

            HandleEventUtils.addIunToMdc(iun);
            log.logStartingProcess(processName);

            handler.handleValidateNotificationCost(iun, message.getPayload().getPnNotificationCostValidationPayload());

            log.logEndingProcess(processName);
        } catch (Exception ex) {
            log.logEndingProcess(processName, false, ex.getMessage(), ex);
            HandleEventUtils.handleException(message.getHeaders(), ex);
            throw ex;
        }
    }
}
