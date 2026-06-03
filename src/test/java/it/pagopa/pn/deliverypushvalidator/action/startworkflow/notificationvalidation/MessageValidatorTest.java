package it.pagopa.pn.deliverypushvalidator.action.startworkflow.notificationvalidation;

import it.pagopa.pn.commons.exceptions.PnInternalException;
import it.pagopa.pn.deliverypushvalidator.dto.ext.delivery.notification.NotificationInt;
import it.pagopa.pn.deliverypushvalidator.dto.ext.delivery.notification.NotificationRecipientInt;
import it.pagopa.pn.deliverypushvalidator.dto.ext.delivery.notification.NotificationSenderInt;
import it.pagopa.pn.deliverypushvalidator.exception.PnMessageNotFoundException;
import it.pagopa.pn.deliverypushvalidator.exception.PnValidationMessageLanguageMismatchException;
import it.pagopa.pn.deliverypushvalidator.exception.PnValidationMessageNotFoundException;
import it.pagopa.pn.deliverypushvalidator.exception.PnValidationSenderIdNotValidException;
import it.pagopa.pn.deliverypushvalidator.generated.openapi.msclient.datavault_reactive.model.MessageResponseDto;
import it.pagopa.pn.deliverypushvalidator.middleware.externalclient.pnclient.datavault.PnDataVaultClientReactive;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static it.pagopa.pn.deliverypushvalidator.exception.PnDeliveryPushValidatorExceptionCodes.ERROR_CODE_DELIVERYPUSH_NO_RECIPIENT_IN_NOTIFICATION;
import static it.pagopa.pn.deliverypushvalidator.exception.PnDeliveryPushValidatorExceptionCodes.ERROR_CODE_DELIVERYPUSH_SENDER_ID_NOT_VALID;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;

@ExtendWith(MockitoExtension.class)
class MessageValidatorTest {

    @Mock
    private PnDataVaultClientReactive pnDataVaultClientReactive;

    private MessageValidator messageValidator;

    @BeforeEach
    void setUp() {
        messageValidator = new MessageValidator(pnDataVaultClientReactive);
    }

    @Test
    void validateWhenRecipientsEmptyThenThrowPnInternalException() {
        NotificationInt notification = NotificationInt.builder()
                .iun("IUN_01")
                .sender(NotificationSenderInt.builder().paId(UUID.randomUUID().toString()).build())
                .recipients(Collections.emptyList())
                .build();

        StepVerifier.create(messageValidator.validate(notification))
                .expectErrorSatisfies(throwable -> {
                    Assertions.assertInstanceOf(PnInternalException.class, throwable);
                    PnInternalException ex = (PnInternalException) throwable;
                    Assertions.assertEquals(ERROR_CODE_DELIVERYPUSH_NO_RECIPIENT_IN_NOTIFICATION,
                            ex.getProblem().getErrors().getFirst().getCode());
                })
                .verify();
        verify(pnDataVaultClientReactive, never()).getMessageById(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
    }

    @Test
    void validateWhenRecipientsNullThenThrowPnInternalException() {
        NotificationInt notification = NotificationInt.builder()
                .iun("IUN_01")
                .sender(NotificationSenderInt.builder().paId(UUID.randomUUID().toString()).build())
                .recipients(null)
                .build();

        StepVerifier.create(messageValidator.validate(notification))
                .expectErrorSatisfies(throwable -> {
                    Assertions.assertInstanceOf(PnInternalException.class, throwable);
                    PnInternalException ex = (PnInternalException) throwable;
                    Assertions.assertEquals(ERROR_CODE_DELIVERYPUSH_NO_RECIPIENT_IN_NOTIFICATION,
                            ex.getProblem().getErrors().getFirst().getCode());
                })
                .verify();
        verify(pnDataVaultClientReactive, never()).getMessageById(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
    }

    @Test
    void validateWhenMessageIdMissingThenThrowValidationMessageNotFound() {
        NotificationInt notification = buildNotification(null, List.of("it"));

        StepVerifier.create(messageValidator.validate(notification))
                .expectErrorSatisfies(throwable -> {
                    Assertions.assertInstanceOf(PnValidationMessageNotFoundException.class, throwable);
                    PnValidationMessageNotFoundException ex = (PnValidationMessageNotFoundException) throwable;
                    Assertions.assertEquals("MESSAGE_NOT_FOUND", ex.getProblem().getErrors().getFirst().getCode());
                    Assertions.assertEquals("recipients[0].messageId", ex.getProblem().getErrors().getFirst().getElement());
                })
                .verify();
    }

    @Test
    void validateWhenMessageIdIsNotUuidThenThrowValidationMessageNotFound() {
        NotificationInt notification = buildNotification("not-a-uuid", List.of("it"));

        StepVerifier.create(messageValidator.validate(notification))
                .expectErrorSatisfies(throwable -> {
                    Assertions.assertInstanceOf(PnValidationMessageNotFoundException.class, throwable);
                    PnValidationMessageNotFoundException ex = (PnValidationMessageNotFoundException) throwable;
                    Assertions.assertEquals("MESSAGE_NOT_FOUND", ex.getProblem().getErrors().getFirst().getCode());
                    Assertions.assertEquals("recipients[0].messageId", ex.getProblem().getErrors().getFirst().getElement());
                })
                .verify();
    }

    @Test
    void validateWhenSenderPaIdIsNullThenThrowSenderIdNotValid() {
        NotificationInt notification = buildNotificationWithSender(null, UUID.randomUUID().toString(), List.of("it"));

        StepVerifier.create(messageValidator.validate(notification))
                .expectErrorSatisfies(throwable -> {
                    Assertions.assertInstanceOf(PnValidationSenderIdNotValidException.class, throwable);
                    PnValidationSenderIdNotValidException ex = (PnValidationSenderIdNotValidException) throwable;
                    Assertions.assertEquals(ERROR_CODE_DELIVERYPUSH_SENDER_ID_NOT_VALID,
                            ex.getProblem().getErrors().getFirst().getCode());
                    Assertions.assertEquals("sender.paId", ex.getProblem().getErrors().getFirst().getElement());
                })
                .verify();
        verify(pnDataVaultClientReactive, never()).getMessageById(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
    }

    @Test
    void validateWhenSenderPaIdIsNotUuidThenThrowSenderIdNotValid() {
        NotificationInt notification = buildNotificationWithSender("pa_02", UUID.randomUUID().toString(), List.of("it"));

        StepVerifier.create(messageValidator.validate(notification))
                .expectErrorSatisfies(throwable -> {
                    Assertions.assertInstanceOf(PnValidationSenderIdNotValidException.class, throwable);
                    PnValidationSenderIdNotValidException ex = (PnValidationSenderIdNotValidException) throwable;
                    Assertions.assertEquals(ERROR_CODE_DELIVERYPUSH_SENDER_ID_NOT_VALID,
                            ex.getProblem().getErrors().getFirst().getCode());
                    Assertions.assertEquals("sender.paId", ex.getProblem().getErrors().getFirst().getElement());
                })
                .verify();
        verify(pnDataVaultClientReactive, never()).getMessageById(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
    }

    @Test
    void validateWhenDataVaultReturnsNotFoundThenThrowValidationMessageNotFound() {
        UUID senderId = UUID.randomUUID();
        UUID messageId = UUID.randomUUID();
        NotificationInt notification = buildNotification(senderId, messageId, List.of("it"));

        when(pnDataVaultClientReactive.getMessageById(messageId, senderId))
                .thenReturn(Mono.error(new PnMessageNotFoundException("not found")));

        StepVerifier.create(messageValidator.validate(notification))
                .expectErrorSatisfies(throwable -> {
                    Assertions.assertInstanceOf(PnValidationMessageNotFoundException.class, throwable);
                    PnValidationMessageNotFoundException ex = (PnValidationMessageNotFoundException) throwable;
                    Assertions.assertEquals("MESSAGE_NOT_FOUND", ex.getProblem().getErrors().getFirst().getCode());
                    Assertions.assertEquals("recipients[0].messageId", ex.getProblem().getErrors().getFirst().getElement());
                })
                .verify();
    }

    @Test
    void validateWhenDataVaultReturnsNullThenThrowValidationMessageNotFound() {
        UUID senderId = UUID.randomUUID();
        UUID messageId = UUID.randomUUID();
        NotificationInt notification = buildNotification(senderId, messageId, List.of("it"));

        when(pnDataVaultClientReactive.getMessageById(messageId, senderId)).thenReturn(Mono.empty());

        StepVerifier.create(messageValidator.validate(notification))
                .expectErrorSatisfies(throwable -> {
                    Assertions.assertInstanceOf(PnValidationMessageNotFoundException.class, throwable);
                    PnValidationMessageNotFoundException ex = (PnValidationMessageNotFoundException) throwable;
                    Assertions.assertEquals("MESSAGE_NOT_FOUND", ex.getProblem().getErrors().getFirst().getCode());
                    Assertions.assertEquals("recipients[0].messageId", ex.getProblem().getErrors().getFirst().getElement());
                })
                .verify();
    }

    @Test
    void validateWhenSecondaryLanguageNotInAdditionalLanguagesThenThrowMismatch() {
        UUID senderId = UUID.randomUUID();
        UUID messageId = UUID.randomUUID();
        NotificationInt notification = buildNotification(senderId, messageId, List.of("it", "de"));

        MessageResponseDto message = mock(MessageResponseDto.class, RETURNS_DEEP_STUBS);
        when(message.getSecondaryContent().getLanguage().getValue()).thenReturn("fr");
        when(pnDataVaultClientReactive.getMessageById(messageId, senderId)).thenReturn(Mono.just(message));

        StepVerifier.create(messageValidator.validate(notification))
                .expectErrorSatisfies(throwable -> {
                    Assertions.assertInstanceOf(PnValidationMessageLanguageMismatchException.class, throwable);
                    PnValidationMessageLanguageMismatchException ex = (PnValidationMessageLanguageMismatchException) throwable;
                    Assertions.assertEquals("MESSAGE_LANGUAGE_MISMATCH", ex.getProblem().getErrors().getFirst().getCode());
                    Assertions.assertEquals("recipients[0].messageId", ex.getProblem().getErrors().getFirst().getElement());
                })
                .verify();
    }

    @Test
    void validateWhenSecondaryLanguageIsNullAndAdditionalLanguagesIsPresentThenThrowMismatch() {
        UUID senderId = UUID.randomUUID();
        UUID messageId = UUID.randomUUID();
        NotificationInt notification = buildNotification(senderId, messageId, List.of("it", "de"));

        MessageResponseDto message = mock(MessageResponseDto.class, RETURNS_DEEP_STUBS);
        when(message.getSecondaryContent()).thenReturn(null);
        when(pnDataVaultClientReactive.getMessageById(messageId, senderId)).thenReturn(Mono.just(message));

        StepVerifier.create(messageValidator.validate(notification))
                .expectErrorSatisfies(throwable -> {
                    Assertions.assertInstanceOf(PnValidationMessageLanguageMismatchException.class, throwable);
                    PnValidationMessageLanguageMismatchException ex = (PnValidationMessageLanguageMismatchException) throwable;
                    Assertions.assertEquals("MESSAGE_LANGUAGE_MISMATCH", ex.getProblem().getErrors().getFirst().getCode());
                    Assertions.assertEquals("recipients[0].messageId", ex.getProblem().getErrors().getFirst().getElement());
                })
                .verify();
    }

    @Test
    void validateWhenSecondaryLanguageIsNullAndAdditionalLanguagesIsNullThenThrowMismatch() {
        UUID senderId = UUID.randomUUID();
        UUID messageId = UUID.randomUUID();
        NotificationInt notification = buildNotification(senderId, messageId, Collections.emptyList());

        MessageResponseDto message = mock(MessageResponseDto.class, RETURNS_DEEP_STUBS);
        when(message.getSecondaryContent()).thenReturn(null);
        when(pnDataVaultClientReactive.getMessageById(messageId, senderId)).thenReturn(Mono.just(message));

        StepVerifier.create(messageValidator.validate(notification))
                .verifyComplete();
    }

    @Test
    void validateWhenSecondaryLanguageMatchesIgnoringCaseThenPass() {
        UUID senderId = UUID.randomUUID();
        UUID messageId = UUID.randomUUID();
        NotificationInt notification = buildNotification(senderId, messageId, List.of("it", "EN"));

        MessageResponseDto message = mock(MessageResponseDto.class, RETURNS_DEEP_STUBS);
        when(message.getSecondaryContent().getLanguage().getValue()).thenReturn("en");
        when(pnDataVaultClientReactive.getMessageById(messageId, senderId)).thenReturn(Mono.just(message));

        StepVerifier.create(messageValidator.validate(notification))
                .verifyComplete();
    }

    private NotificationInt buildNotification(String messageId, List<String> additionalLanguages) {
        return NotificationInt.builder()
                .iun("IUN_01")
                .sender(NotificationSenderInt.builder().paId(UUID.randomUUID().toString()).build())
                .recipients(List.of(NotificationRecipientInt.builder().messageId(messageId).build()))
                .additionalLanguages(additionalLanguages)
                .build();
    }

    private NotificationInt buildNotification(UUID senderId, UUID messageId, List<String> additionalLanguages) {
        return NotificationInt.builder()
                .iun("IUN_01")
                .sender(NotificationSenderInt.builder().paId(senderId.toString()).build())
                .recipients(List.of(NotificationRecipientInt.builder().messageId(messageId.toString()).build()))
                .additionalLanguages(additionalLanguages)
                .build();
    }

    private NotificationInt buildNotificationWithSender(String senderPaId, String messageId, List<String> additionalLanguages) {
        return NotificationInt.builder()
                .iun("IUN_01")
                .sender(NotificationSenderInt.builder().paId(senderPaId).build())
                .recipients(List.of(NotificationRecipientInt.builder().messageId(messageId).build()))
                .additionalLanguages(additionalLanguages)
                .build();
    }
}

