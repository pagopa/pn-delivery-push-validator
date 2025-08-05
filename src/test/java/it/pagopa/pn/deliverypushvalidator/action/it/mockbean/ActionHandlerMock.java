package it.pagopa.pn.deliverypushvalidator.action.it.mockbean;

import it.pagopa.pn.deliverypushvalidator.middleware.queue.producer.abstractions.actionspool.Action;
import it.pagopa.pn.deliverypushvalidator.utils.ThreadPool;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;

import java.util.HashMap;

@Slf4j
public class ActionHandlerMock {
    private final ActionHandlerRegistry actionHandlerRegistry;
    
    public ActionHandlerMock(ActionHandlerRegistry actionHandlerRegistry) {
        this.actionHandlerRegistry = actionHandlerRegistry;
    }

    public void handleSchedulingAction(Action action) {
        ThreadPool.start(new Thread(() ->{
            switch (action.getType()) {
                case NOTIFICATION_REFUSED ->{
                    final Message<Action> message = getBaseActionMessage(action);
                    var handler = actionHandlerRegistry.getNotificationRefusedHandler();
                    handler.handle(message.getPayload(), message.getHeaders());
                }
                case NOTIFICATION_VALIDATION ->{
                    final Message<Action> message = getBaseActionMessage(action);
                    var handler = actionHandlerRegistry.getNotificationValidationHandler();
                    handler.handle(message.getPayload(), message.getHeaders());
                }
                case SCHEDULE_RECEIVED_LEGALFACT_GENERATION ->{
                    final Message<Action> message = getBaseActionMessage(action);
                    var handler = actionHandlerRegistry.getReceivedLegalFactGenerationHandler();
                    handler.handle(message.getPayload(), message.getHeaders());
                }
                case DOCUMENT_CREATION_RESPONSE ->{
                    final Message<Action> message = getBaseActionMessage(action);
                    var handler = actionHandlerRegistry.getDocumentCreationResponseEventHandler();
                    handler.handle(message.getPayload(), message.getHeaders());
                }
                default ->
                        log.error("[TEST] actionType not found {}", action.getType());
            }
        }));
    }

    @NotNull
    private static Message<Action> getBaseActionMessage(Action action) {
        return new Message<>() {
            @Override
            @NotNull
            public Action getPayload() {
                return action;
            }
            @Override
            @NotNull
            public MessageHeaders getHeaders() {
                return new MessageHeaders(new HashMap<>());
            }
        };
    }

}
