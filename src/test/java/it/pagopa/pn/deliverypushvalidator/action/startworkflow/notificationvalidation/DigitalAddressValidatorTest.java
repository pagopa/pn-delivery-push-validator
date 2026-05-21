package it.pagopa.pn.deliverypushvalidator.action.startworkflow.notificationvalidation;

import it.pagopa.pn.commons.exceptions.PnInternalException;
import it.pagopa.pn.deliverypushvalidator.dto.address.LegalDigitalAddressInt;
import it.pagopa.pn.deliverypushvalidator.dto.campaign.Campaign;
import it.pagopa.pn.deliverypushvalidator.dto.campaign.Channel;
import it.pagopa.pn.deliverypushvalidator.dto.campaign.WorkflowEntity;
import it.pagopa.pn.deliverypushvalidator.dto.ext.datavault.RecipientTypeInt;
import it.pagopa.pn.deliverypushvalidator.dto.ext.delivery.notification.NotificationInt;
import it.pagopa.pn.deliverypushvalidator.dto.ext.delivery.notification.NotificationRecipientInt;
import it.pagopa.pn.deliverypushvalidator.exception.PnValidationDigitalAddressMissingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DigitalAddressValidatorTest {

    private DigitalAddressValidator validator;

    @BeforeEach
    void setUp() {
        validator = new DigitalAddressValidator();
    }

    @Test
    void validateDigitalAddress_noPecWorkflow_shouldNotThrow() {
        Campaign campaign = Campaign.builder()
                .workflow(List.of(WorkflowEntity.builder().channel(Channel.IO).build()))
                .build();
        NotificationInt notification = NotificationInt.builder().build();

        assertDoesNotThrow(() -> validator.validateDigitalAddress(notification, campaign));
    }

    @Test
    void validateDigitalAddress_nullRecipients_shouldThrow() {
        Campaign campaign = Campaign.builder()
                .workflow(List.of(WorkflowEntity.builder().channel(Channel.PEC).build()))
                .build();
        NotificationInt notification = NotificationInt.builder().recipients(null).build();

        assertThrows(PnInternalException.class, () -> validator.validateDigitalAddress(notification, campaign));
    }

    @Test
    void validateDigitalAddress_pgWithValidDigitalAddress_shouldNotThrow() {
        Campaign campaign = Campaign.builder()
                .workflow(List.of(WorkflowEntity.builder().channel(Channel.PEC).build()))
                .build();
        NotificationRecipientInt recipient = NotificationRecipientInt.builder()
                .recipientType(RecipientTypeInt.PG)
                .digitalDomicile(LegalDigitalAddressInt.builder().address("test@pec.it").build())
                .build();
        NotificationInt notification = NotificationInt.builder().recipients(List.of(recipient)).build();

        assertDoesNotThrow(() -> validator.validateDigitalAddress(notification, campaign));
    }

    @Test
    void validateDigitalAddress_pgWithNullDigitalAddress_shouldThrow() {
        Campaign campaign = Campaign.builder()
                .workflow(List.of(WorkflowEntity.builder().channel(Channel.PEC).build()))
                .build();
        NotificationRecipientInt recipient = NotificationRecipientInt.builder()
                .recipientType(RecipientTypeInt.PG)
                .digitalDomicile(LegalDigitalAddressInt.builder().address(null).build())
                .build();
        NotificationInt notification = NotificationInt.builder().recipients(List.of(recipient)).build();

        assertThrows(PnValidationDigitalAddressMissingException.class, () -> validator.validateDigitalAddress(notification, campaign));
    }

    @Test
    void validateDigitalAddress_pgWithBlankDigitalAddress_shouldThrow() {
        Campaign campaign = Campaign.builder()
                .workflow(List.of(WorkflowEntity.builder().channel(Channel.PEC).build()))
                .build();
        NotificationRecipientInt recipient = NotificationRecipientInt.builder()
                .recipientType(RecipientTypeInt.PG)
                .digitalDomicile(LegalDigitalAddressInt.builder().address("   ").build())
                .build();
        NotificationInt notification = NotificationInt.builder().recipients(List.of(recipient)).build();

        assertThrows(PnValidationDigitalAddressMissingException.class, () -> validator.validateDigitalAddress(notification, campaign));
    }

    @Test
    void validateDigitalAddress_notPgRecipient_shouldNotThrow() {
        Campaign campaign = Campaign.builder()
                .workflow(List.of(WorkflowEntity.builder().channel(Channel.PEC).build()))
                .build();
        NotificationRecipientInt recipient = NotificationRecipientInt.builder()
                .recipientType(RecipientTypeInt.PF)
                .digitalDomicile(LegalDigitalAddressInt.builder().address(null).build())
                .build();
        NotificationInt notification = NotificationInt.builder().recipients(List.of(recipient)).build();

        assertDoesNotThrow(() -> validator.validateDigitalAddress(notification, campaign));
    }

    @Test
    void validateDigitalAddress_multipleRecipients_someWithoutDigitalAddress_shouldThrow() {
        Campaign campaign = Campaign.builder()
                .workflow(List.of(WorkflowEntity.builder().channel(Channel.PEC).build()))
                .build();
        NotificationRecipientInt recipient1 = NotificationRecipientInt.builder()
                .recipientType(RecipientTypeInt.PG)
                .digitalDomicile(LegalDigitalAddressInt.builder().address(null).build())
                .build();
        NotificationRecipientInt recipient2 = NotificationRecipientInt.builder()
                .recipientType(RecipientTypeInt.PG)
                .digitalDomicile(LegalDigitalAddressInt.builder().address("indirizzo@pec.it").build())
                .build();
        NotificationRecipientInt recipient3 = NotificationRecipientInt.builder()
                .recipientType(RecipientTypeInt.PG)
                .digitalDomicile(LegalDigitalAddressInt.builder().address("   ").build())
                .build();
        NotificationInt notification = NotificationInt.builder().recipients(List.of(recipient1, recipient2, recipient3)).build();

        assertThrows(PnValidationDigitalAddressMissingException.class, () -> validator.validateDigitalAddress(notification, campaign));
    }
}