package it.pagopa.pn.deliverypushvalidator.middleware.queue.consumer;


import io.awspring.cloud.sqs.annotation.SqsListener;
import it.pagopa.pn.deliverypushvalidator.config.PnDeliveryPushValidatorConfigs;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;

import static it.pagopa.pn.deliverypushvalidator.middleware.queue.utils.ChannelUtils.setMdc;


@Configuration
@Slf4j
@RequiredArgsConstructor
public class NewNotificationConsumer {
    private final PnDeliveryPushValidatorConfigs pnDeliveryPushValidatorConfigs;

    @SqsListener(queueNames = "#{@pnDeliveryPushValidatorConfigs.topics.deliveryValidationEvents}")
    public void pnDeliveryNewNotificationEventConsumer(Message<String> message) {
        setMdc(message);
        try {
            log.info("messaggio ricevuto {}", message);
            //Todo: to be implemented
        } catch (Exception ex) {
            log.error("Error processing message from {}: {}", pnDeliveryPushValidatorConfigs, ex.getMessage(), ex);
            throw ex;
        }
    }
}