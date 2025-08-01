package it.pagopa.pn.deliverypushvalidator.middleware.queue.consumer;

import io.awspring.cloud.sqs.annotation.SqsListener;
import it.pagopa.pn.deliverypushvalidator.config.PnDeliveryPushValidatorConfigs;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import static it.pagopa.pn.deliverypushvalidator.middleware.queue.utils.ChannelUtils.setMdc;

@Data
@Slf4j
@RequiredArgsConstructor
public class F24Consumer {
    private PnDeliveryPushValidatorConfigs pnDeliveryPushValidatorConfigs;

    @SqsListener(queueNames = "#{@pnDeliveryPushValidatorConfigs.topics.f24Events}")
    public void pnF24EventInboundConsumer(Message<String> message) {
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
