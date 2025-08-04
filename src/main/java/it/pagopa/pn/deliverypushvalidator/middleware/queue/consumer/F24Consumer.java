package it.pagopa.pn.deliverypushvalidator.middleware.queue.consumer;

import io.awspring.cloud.sqs.annotation.SqsListener;
import it.pagopa.pn.api.dto.events.DetailedTypePayload;
import it.pagopa.pn.deliverypushvalidator.config.PnDeliveryPushValidatorConfigs;
import it.pagopa.pn.deliverypushvalidator.middleware.externalclient.pnclient.f24.PnF24Client;
import it.pagopa.pn.deliverypushvalidator.middleware.queue.consumer.handler.utils.HandleEventUtils;
import it.pagopa.pn.deliverypushvalidator.middleware.responsehandler.F24ResponseHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;

import static it.pagopa.pn.deliverypushvalidator.middleware.queue.utils.ChannelUtils.setMdc;

@Slf4j
@RequiredArgsConstructor
@Configuration
public class F24Consumer {
    private PnDeliveryPushValidatorConfigs pnDeliveryPushValidatorConfigs;
    private F24ResponseHandler handler;

    @SqsListener(queueNames = "#{@pnDeliveryPushValidatorConfigs.topics.f24Events}")
    public void pnF24EventInboundConsumer(Message<DetailedTypePayload> message) {
        setMdc(message);
        try {
            log.info("Handle message from {} with content {}", PnF24Client.CLIENT_NAME, message);
            DetailedTypePayload event = message.getPayload();
            handler.handleEventF24(event);
        } catch (Exception ex) {
            HandleEventUtils.handleException(message.getHeaders(), ex);
            throw ex;
        }
    }
}
