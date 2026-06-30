package it.pagopa.pn.deliverypushvalidator.middleware.queue.consumer.router;

// Dovranno essere censiti qui tutti i tipi di eventi supportati dal router.
public enum SupportedEventType {
    NOTIFICATION_VALIDATION,
    NOTIFICATION_REFUSED,
    SCHEDULE_RECEIVED_LEGALFACT_GENERATION,
    DOCUMENT_CREATION_RESPONSE,
    POST_VALIDATION_COMPLETED,
}
