package it.pagopa.pn.deliverypushvalidator.validation;

import it.pagopa.pn.deliverypushvalidator.dto.ext.delivery.notification.NotificationInt;
import it.pagopa.pn.deliverypushvalidator.dto.ext.delivery.notification.NotificationSenderInt;
import it.pagopa.pn.deliverypushvalidator.exception.PnValidationMessageException;
import it.pagopa.pn.deliverypushvalidator.service.MessageClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class MessageValidatorTest {

    @Mock
    private MessageClient messageClient;
    private MessageValidator messageValidator;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        messageValidator = new MessageValidator(messageClient);
    }

    private NotificationInt n(String msgId, List<String> langs) {
        return NotificationInt.builder().iun("IUN-1").messageId(msgId).campaignId("C1")
                .additionalLanguages(langs)
                .sender(NotificationSenderInt.builder().paId("PA").paTaxId("T").build()).build();
    }

    private CampaignData c(List<CampaignMessageRef> msgs) {
        return CampaignData.builder().campaignId("C1").messages(msgs).build();
    }

    @Test
    void directMessageId_ok() {
        Mockito.when(messageClient.getById("M1")).thenReturn(Optional.of(
                MessageData.builder().messageId("M1").language("IT").subject("s").longBody("b").build()));
        assertEquals("M1", messageValidator.validateMessage(n("M1", null), c(List.of())).getMessageId());
    }

    @Test
    void directMessageId_notFound() {
        Mockito.when(messageClient.getById("MX")).thenReturn(Optional.empty());
        assertThrows(PnValidationMessageException.class, () -> messageValidator.validateMessage(n("MX", null), c(List.of())));
    }

    @Test
    void campaignFallback_itMono_ok() {
        Mockito.when(messageClient.getById("MIT")).thenReturn(Optional.of(
                MessageData.builder().messageId("MIT").language("IT").subject("s").longBody("b").build()));
        MessageData r = messageValidator.validateMessage(n(null, null),
                c(List.of(CampaignMessageRef.builder().messageId("MIT").build())));
        assertEquals("MIT", r.getMessageId());
    }

    @Test
    void campaignFallback_bilingualDE_ok() {
        Mockito.when(messageClient.getById("MDE")).thenReturn(Optional.of(
                MessageData.builder().messageId("MDE").language("IT").subject("s").longBody("b")
                        .additionalMessage("DE content").build()));
        MessageData r = messageValidator.validateMessage(n(null, List.of("DE")),
                c(List.of(CampaignMessageRef.builder().messageId("MDE").secondaryLanguage("DE").build())));
        assertEquals("MDE", r.getMessageId());
    }

    @Test
    void campaignFallback_bilingualDE_missingAdditional() {
        Mockito.when(messageClient.getById("MDE")).thenReturn(Optional.of(
                MessageData.builder().messageId("MDE").language("IT").subject("s").longBody("b").build()));
        assertThrows(PnValidationMessageException.class, () ->
                messageValidator.validateMessage(n(null, List.of("DE")),
                        c(List.of(CampaignMessageRef.builder().messageId("MDE").secondaryLanguage("DE").build()))));
    }

    @Test
    void primaryLanguageNotIT() {
        Mockito.when(messageClient.getById("M1")).thenReturn(Optional.of(
                MessageData.builder().messageId("M1").language("EN").subject("s").longBody("b").build()));
        assertThrows(PnValidationMessageException.class, () -> messageValidator.validateMessage(n("M1", null), c(List.of())));
    }

    @Test
    void noCampaignMessages_noNotificationMessageId() {
        assertThrows(PnValidationMessageException.class, () -> messageValidator.validateMessage(n(null, null), c(null)));
    }
}

