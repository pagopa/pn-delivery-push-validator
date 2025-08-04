package it.pagopa.pn.deliverypushvalidator.middleware.queue.consumer;

import io.awspring.cloud.sqs.annotation.SqsListener;
import it.pagopa.pn.deliverypushvalidator.config.PnDeliveryPushValidatorConfigs;
import it.pagopa.pn.deliverypushvalidator.generated.openapi.msclient.addressmanager.model.NormalizeItemsResult;
import it.pagopa.pn.deliverypushvalidator.middleware.externalclient.pnclient.addressmanager.AddressManagerClient;
import it.pagopa.pn.deliverypushvalidator.middleware.queue.consumer.handler.utils.HandleEventUtils;
import it.pagopa.pn.deliverypushvalidator.middleware.responsehandler.AddressManagerResponseHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;

import static it.pagopa.pn.deliverypushvalidator.middleware.queue.utils.ChannelUtils.setMdc;

@Configuration
@Slf4j
@RequiredArgsConstructor
public class AddressManagerConsumer {
    private PnDeliveryPushValidatorConfigs pnDeliveryPushValidatorConfigs;
    private AddressManagerResponseHandler handler;

    @SqsListener(queueNames = "#{@pnDeliveryPushValidatorConfigs.topics.addressManagerEvents}")
    public void pnAddressManagerEventInboundConsumer(Message<NormalizeItemsResult> message) {
        setMdc(message);
        try {
            log.info("Handle message from {} with content {}", AddressManagerClient.CLIENT_NAME, message);
            NormalizeItemsResult response = message.getPayload();
            handler.handleResponseReceived(response);
        } catch (Exception ex) {
            HandleEventUtils.handleException(message.getHeaders(), ex);
            throw ex;
        }
    }

}
