package it.pagopa.pn.deliverypushvalidator.action.it.mockbean;

import it.pagopa.pn.deliverypushvalidator.middleware.queue.consumer.handler.action.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.stereotype.Component;

@AllArgsConstructor
@Getter
@Component
public class ActionHandlerRegistry {

    private final NotificationRefusedHandler notificationRefusedHandler;
    private final NotificationValidationHandler notificationValidationHandler;
    private final ReceivedLegalFactGenerationHandler receivedLegalFactGenerationHandler;
}
