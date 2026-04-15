package it.pagopa.pn.deliverypushvalidator.service;

import it.pagopa.pn.deliverypushvalidator.validation.MessageData;

import java.util.Optional;

/**
 * Client interface for message CRUD operations.
 * In MVP this is backed by a mock implementation.
 * Will be replaced by a real client calling the pn-message microservice.
 */
public interface MessageClient {

    Optional<MessageData> getById(String messageId);

    MessageData create(MessageData message);

    MessageData update(MessageData message);

    void delete(String messageId);
}

