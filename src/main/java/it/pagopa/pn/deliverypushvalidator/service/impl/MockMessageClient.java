package it.pagopa.pn.deliverypushvalidator.service.impl;

import it.pagopa.pn.deliverypushvalidator.service.MessageClient;
import it.pagopa.pn.deliverypushvalidator.validation.MessageData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Mock implementation of MessageClient for MVP.
 * Stores messages in-memory. Will be replaced by a real Dynamo-backed client.
 */
@Slf4j
@Component
public class MockMessageClient implements MessageClient {

    private final Map<String, MessageData> store = new ConcurrentHashMap<>();

    @Override
    public Optional<MessageData> getById(String messageId) {
        log.debug("MockMessageClient.getById messageId={}", messageId);
        return Optional.ofNullable(store.get(messageId));
    }

    @Override
    public MessageData create(MessageData message) {
        log.debug("MockMessageClient.create messageId={}", message.getMessageId());
        store.put(message.getMessageId(), message);
        return message;
    }

    @Override
    public MessageData update(MessageData message) {
        log.debug("MockMessageClient.update messageId={}", message.getMessageId());
        store.put(message.getMessageId(), message);
        return message;
    }

    @Override
    public void delete(String messageId) {
        log.debug("MockMessageClient.delete messageId={}", messageId);
        store.remove(messageId);
    }
}

