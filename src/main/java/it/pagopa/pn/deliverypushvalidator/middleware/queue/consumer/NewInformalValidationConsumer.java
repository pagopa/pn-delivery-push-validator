package it.pagopa.pn.deliverypushvalidator.middleware.queue.consumer;

import io.awspring.cloud.sqs.annotation.SqsListener;
import it.pagopa.pn.api.dto.events.PnDeliveryNewNotificationEvent;
import it.pagopa.pn.deliverypushvalidator.action.startworkflow.StartWorkflowHandler;
import it.pagopa.pn.deliverypushvalidator.config.PnDeliveryPushValidatorConfigs;
import it.pagopa.pn.deliverypushvalidator.middleware.externalclient.pnclient.delivery.PnDeliveryClient;
import it.pagopa.pn.deliverypushvalidator.middleware.queue.consumer.handler.utils.HandleEventUtils;
import lombok.CustomLog;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;

import static it.pagopa.pn.deliverypushvalidator.middleware.queue.utils.ChannelUtils.setMdc;

@Configuration
@CustomLog
@RequiredArgsConstructor
public class NewInformalValidationConsumer {

    private PnDeliveryPushValidatorConfigs pnDeliveryPushValidatorConfigs;
    private final StartWorkflowHandler startWorkflowHandler;

    @SqsListener(queueNames = "#{@pnDeliveryPushValidatorConfigs.topics.informalValidationInputEvents}")
    public void informalValidationInputsEventConsumer(Message<PnDeliveryNewNotificationEvent.Payload> message) {
        setMdc(message);
        final String processName = "NEW INFORMAL NOTIFICATION";
        try {
            log.info("Handle message from {} with content {}", PnDeliveryClient.CLIENT_NAME, message);
            String iun = message.getPayload().getIun();
            HandleEventUtils.addIunToMdc(iun);
            log.logStartingProcess(processName);
            log.info("Informal validation process for iun {} started", iun);
            startWorkflowHandler.startInformalWorkflow(iun);
            log.logEndingProcess(processName);
        } catch (Exception ex) {
            log.logEndingProcess(processName, false, ex.getMessage(), ex);
            HandleEventUtils.handleException(message.getHeaders(), ex);
            throw ex;
        }
    }
}
