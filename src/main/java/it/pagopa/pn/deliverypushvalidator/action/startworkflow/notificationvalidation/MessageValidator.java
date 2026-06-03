package it.pagopa.pn.deliverypushvalidator.action.startworkflow.notificationvalidation;

import it.pagopa.pn.commons.exceptions.PnInternalException;
import it.pagopa.pn.deliverypushvalidator.dto.ext.delivery.notification.NotificationInt;
import it.pagopa.pn.deliverypushvalidator.dto.ext.delivery.notification.NotificationRecipientInt;
import it.pagopa.pn.deliverypushvalidator.exception.PnMessageNotFoundException;
import it.pagopa.pn.deliverypushvalidator.exception.PnValidationMessageLanguageMismatchException;
import it.pagopa.pn.deliverypushvalidator.exception.PnValidationMessageNotFoundException;
import it.pagopa.pn.deliverypushvalidator.exception.PnValidationSenderIdNotValidException;
import it.pagopa.pn.deliverypushvalidator.generated.openapi.msclient.datavault_reactive.model.LocalizedContent;
import it.pagopa.pn.deliverypushvalidator.generated.openapi.msclient.datavault_reactive.model.MessageResponseDto;
import it.pagopa.pn.deliverypushvalidator.middleware.externalclient.pnclient.datavault.PnDataVaultClientReactive;
import lombok.AllArgsConstructor;
import lombok.CustomLog;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

import static it.pagopa.pn.deliverypushvalidator.exception.PnDeliveryPushValidatorExceptionCodes.ERROR_CODE_DELIVERYPUSH_NO_RECIPIENT_IN_NOTIFICATION;

/**
 * Validator for the message associated to each notification recipient.
 * <p>
 * For every recipient it:
 * <ol>
 *   <li>checks that {@code messageId} is present;</li>
 *   <li>retrieves the message from pn-data-vault via {@code getMessageById};</li>
 *   <li>verifies language consistency between the message's secondary content and
 *       {@code notification.getAdditionalLanguages()}.</li>
 * </ol>
 */
@Component
@AllArgsConstructor
@CustomLog
public class MessageValidator {

    private static final String VALIDATE_MESSAGE_PROCESS = "Validate message";
    private static final String MESSAGE_VALIDATION_FAILED_LOG = "Message validation failed: {} - iun={}, element={}";
    private static final String ELEMENT_SENDER_PA_ID = "sender.paId";

    private final PnDataVaultClientReactive pnDataVaultClientReactive;

    /**
     * Validates message-related constraints for all recipients of the given notification.
     *
     * @param notification the notification to validate
     * @throws PnValidationMessageNotFoundException     if a recipient's {@code messageId} is missing or the
     *                                                  corresponding message cannot be found in pn-data-vault
     * @throws PnValidationMessageLanguageMismatchException if the language declared in the data-vault message
     *                                                      does not match the notification's additional languages
     */
    public Mono<Void> validate(NotificationInt notification) {
        log.logChecking(VALIDATE_MESSAGE_PROCESS);

        List<NotificationRecipientInt> recipients = notification.getRecipients();
        if (CollectionUtils.isEmpty(recipients)) {
            String detail = "No recipients found for notification";
            log.error("Message validation failed: {} - iun={}", detail, notification.getIun());
            return Mono.error(new PnInternalException(detail, ERROR_CODE_DELIVERYPUSH_NO_RECIPIENT_IN_NOTIFICATION));
        }

        return Flux.range(0, recipients.size())
                .concatMap(i -> {
                    NotificationRecipientInt recipient = recipients.get(i);
                    String element = "recipients[" + i + "].messageId";
                    return validateRecipientMessage(notification, recipient, element);
                })
                .then(Mono.fromRunnable(() -> log.logCheckingOutcome(VALIDATE_MESSAGE_PROCESS, true)));
    }

    private Mono<Void> validateRecipientMessage(NotificationInt notification,
                                                NotificationRecipientInt recipient,
                                                String element) {

        return Mono.defer(() -> {

            // 1. Check messageId presence
            if (!StringUtils.hasText(recipient.getMessageId())) {
                String detail = "messageId is missing for recipient";
                return Mono.error(logValidationFailure(notification, element, detail,
                        new PnValidationMessageNotFoundException(detail, element)));
            }

            UUID messageId;
            try {
                messageId = UUID.fromString(recipient.getMessageId());
            } catch (IllegalArgumentException e) {
                String detail = "messageId is not a valid UUID: " + recipient.getMessageId();
                return Mono.error(logValidationFailure(notification, element, detail,
                        new PnValidationMessageNotFoundException(detail, element)));
            }

            return resolveSenderId(notification)
                    .flatMap(senderId -> pnDataVaultClientReactive.getMessageById(messageId, senderId)
                            .onErrorMap(PnMessageNotFoundException.class, e -> {
                                String detail = "Message with id: " + recipient.getMessageId() + " not found";
                                return logValidationFailure(notification, element, detail,
                                        new PnValidationMessageNotFoundException(detail, element));
                            })
                            .switchIfEmpty(Mono.defer(() -> {
                                String detail = "Message with id: " + recipient.getMessageId() + " not found";
                                return Mono.error(logValidationFailure(notification, element, detail,
                                        new PnValidationMessageNotFoundException(detail, element)));
                            }))
                            .flatMap(message -> validateMessageLanguage(notification, recipient, element, message)));
        });
    }

    private Mono<Void> validateMessageLanguage(NotificationInt notification,
                                               NotificationRecipientInt recipient,
                                               String element,
                                               MessageResponseDto message) {
        List<String> additionalLanguages = notification.getAdditionalLanguages();
        LocalizedContent secondaryContent = message.getSecondaryContent();

        if (secondaryContent == null) {
            if (!CollectionUtils.isEmpty(additionalLanguages)) {
                String detail = "Message has no secondary content but notification declares additional languages "
                        + additionalLanguages + " for messageId: " + recipient.getMessageId();
                return Mono.error(logValidationFailure(notification, element, detail,
                        new PnValidationMessageLanguageMismatchException(detail, element)));
            }
            return Mono.empty();
        }

        String messageLanguage = secondaryContent.getLanguage().getValue();
        boolean languageMatches = !CollectionUtils.isEmpty(additionalLanguages)
                && additionalLanguages.stream().anyMatch(lang -> lang.equalsIgnoreCase(messageLanguage));

        if (!languageMatches) {
            String detail = "Message language '" + messageLanguage
                    + "' does not match notification additional languages " + additionalLanguages
                    + " for messageId: " + recipient.getMessageId();
            return Mono.error(logValidationFailure(notification, element, detail,
                    new PnValidationMessageLanguageMismatchException(detail, element)));
        }

        return Mono.empty();
    }

    private Mono<UUID> resolveSenderId(NotificationInt notification) {
        if (notification.getSender() == null || !StringUtils.hasText(notification.getSender().getPaId())) {
            String detail = "sender.paId is missing for notification";
            return Mono.error(logValidationFailure(notification, ELEMENT_SENDER_PA_ID, detail,
                    new PnValidationSenderIdNotValidException(detail, ELEMENT_SENDER_PA_ID)));
        }

        try {
            return Mono.just(UUID.fromString(notification.getSender().getPaId()));
        } catch (IllegalArgumentException e) {
            String detail = "sender.paId is not a valid UUID: " + notification.getSender().getPaId();
            return Mono.error(logValidationFailure(notification, ELEMENT_SENDER_PA_ID, detail,
                    new PnValidationSenderIdNotValidException(detail, ELEMENT_SENDER_PA_ID)));
        }
    }

    private <T extends Throwable> T logValidationFailure(NotificationInt notification,
                                                        String element,
                                                        String detail,
                                                        T exception) {
        log.warn(MESSAGE_VALIDATION_FAILED_LOG, detail, notification.getIun(), element);
        return exception;
    }
}

