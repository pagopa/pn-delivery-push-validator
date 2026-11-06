package it.pagopa.pn.deliverypushvalidator.middleware.queue.consumer;

import io.awspring.cloud.sqs.annotation.SqsListener;
import it.pagopa.pn.api.dto.events.notificationcost.validation.PnNotificationCostValidationEvent;
import it.pagopa.pn.api.dto.events.notificationcost.validation.PnNotificationCostValidationEventPayload;
import it.pagopa.pn.deliverypushvalidator.config.PnDeliveryPushValidatorConfigs;
import it.pagopa.pn.deliverypushvalidator.dto.ext.notificationcost.NotificationCostValidationResponse;
import it.pagopa.pn.deliverypushvalidator.dto.ext.notificationcost.ValidationStatus;
import it.pagopa.pn.deliverypushvalidator.middleware.externalclient.pnclient.notificationcostservice.NotificationCostServiceClient;
import it.pagopa.pn.deliverypushvalidator.middleware.queue.consumer.handler.utils.HandleEventUtils;
import it.pagopa.pn.deliverypushvalidator.middleware.responsehandler.NotificationCostResponseHandler;
import lombok.AllArgsConstructor;
import lombok.CustomLog;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

import static it.pagopa.pn.deliverypushvalidator.middleware.queue.utils.ChannelUtils.setMdc;

@Component
@AllArgsConstructor
@CustomLog
public class NotificationCostConsumer {
    private PnDeliveryPushValidatorConfigs pnDeliveryPushValidatorConfigs;
    private final NotificationCostResponseHandler handler;

    @SqsListener(queueNames = "#{@pnDeliveryPushValidatorConfigs.topics.pnNotificationCostToDeliveryPushValidatorEvents}")
    public void pnNotificationCostEventInboundConsumer(Message<PnNotificationCostValidationEvent.Detail> message) {
        setMdc(message);
        final String processName = "NOTIFICATION COST";
        try {
            log.info("Handle message from {} with content {}", NotificationCostServiceClient.CLIENT_NAME, message);

            String iun = message.getPayload().getPnNotificationCostValidationPayload().getIun();
            HandleEventUtils.addIunToMdc(iun);
            log.logStartingProcess(processName);

            NotificationCostValidationResponse response = mapToInternalResponse(message.getPayload().getPnNotificationCostValidationPayload());
            handler.handleResponseReceived(response);

            log.logEndingProcess(processName);
        } catch (Exception ex) {
            log.logEndingProcess(processName, false, ex.getMessage(), ex);
            HandleEventUtils.handleException(message.getHeaders(), ex);
            throw ex;
        }
    }

    private NotificationCostValidationResponse mapToInternalResponse(PnNotificationCostValidationEventPayload payload) {
        return NotificationCostValidationResponse.builder()
                .iun(payload.getIun())
                .status(ValidationStatus.valueOf(payload.getStatus().name()))
                .build();
    }
}
