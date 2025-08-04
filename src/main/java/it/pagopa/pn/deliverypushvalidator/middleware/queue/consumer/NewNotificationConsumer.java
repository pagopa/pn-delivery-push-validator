package it.pagopa.pn.deliverypushvalidator.middleware.queue.consumer;

import io.awspring.cloud.sqs.annotation.SqsListener;
import it.pagopa.pn.api.dto.events.PnDeliveryNewNotificationEvent;
import it.pagopa.pn.deliverypushvalidator.action.startworkflow.StartWorkflowHandler;
import it.pagopa.pn.deliverypushvalidator.config.PnDeliveryPushValidatorConfigs;
import it.pagopa.pn.deliverypushvalidator.middleware.externalclient.pnclient.delivery.PnDeliveryClient;
import it.pagopa.pn.deliverypushvalidator.middleware.queue.consumer.handler.utils.HandleEventUtils;
import lombok.AllArgsConstructor;
import lombok.CustomLog;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import static it.pagopa.pn.deliverypushvalidator.middleware.queue.utils.ChannelUtils.setMdc;

@Configuration
@AllArgsConstructor
@CustomLog
public class NewNotificationConsumer {
    private final PnDeliveryPushValidatorConfigs pnDeliveryPushValidatorConfigs;
    private final StartWorkflowHandler startWorkflowHandler;

    @SqsListener(queueNames = "#{@pnDeliveryPushValidatorConfigs.topics.deliveryValidationEvents}")
    public void pnDeliveryNewNotificationEventConsumer(Message<PnDeliveryNewNotificationEvent> message) {
        setMdc(message);
        final String processName = "NEW NOTIFICATION";
        try {
            log.info("Handle message from {} with content {}", PnDeliveryClient.CLIENT_NAME, message);
            PnDeliveryNewNotificationEvent pnDeliveryNewNotificationEvent = PnDeliveryNewNotificationEvent.builder()
                    .payload(message.getPayload().getPayload())
                    .header(HandleEventUtils.mapStandardEventHeader(message.getHeaders()))
                    .build();
            String iun = pnDeliveryNewNotificationEvent.getPayload().getIun();
            HandleEventUtils.addIunToMdc(iun);
            log.logStartingProcess(processName);
            startWorkflowHandler.startWorkflow(iun);
            log.logEndingProcess(processName);
        } catch (Exception ex) {
            HandleEventUtils.handleException(message.getHeaders(), ex);
            throw ex;
        }
    }
}