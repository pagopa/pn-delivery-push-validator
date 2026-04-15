package it.pagopa.pn.deliverypushvalidator.legalfact;

import it.pagopa.pn.deliverypushvalidator.action.it.mockbean.TemplatesClientMock;
import it.pagopa.pn.deliverypushvalidator.config.PnDeliveryPushValidatorConfigs;
import it.pagopa.pn.deliverypushvalidator.dto.address.LegalDigitalAddressInt;
import it.pagopa.pn.deliverypushvalidator.dto.address.PhysicalAddressInt;
import it.pagopa.pn.deliverypushvalidator.dto.ext.datavault.RecipientTypeInt;
import it.pagopa.pn.deliverypushvalidator.dto.ext.delivery.notification.*;
import it.pagopa.pn.deliverypushvalidator.generated.openapi.msclient.templatesengine.model.LanguageEnum;
import it.pagopa.pn.deliverypushvalidator.generated.openapi.msclient.templatesengine.model.NotificationReceivedLegalFact;
import it.pagopa.pn.deliverypushvalidator.middleware.externalclient.pnclient.templatesengine.TemplatesClient;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.junit.jupiter.api.extension.ExtendWith;

import java.time.Instant;
import java.util.List;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = LegalFactGeneratorTemplatesTest.TestConfig.class)
class LegalFactGeneratorTemplatesTest {

    @Autowired
    LegalFactGenerator legalFactGeneratorTemplatesTest;
    @MockitoBean
    TemplatesClient templatesClient;

    TemplatesClientMock templatesClientMock = new TemplatesClientMock();

    @Test
    void generateNotificationReceivedLegalFact() {
        Mockito.when(templatesClient.notificationReceivedLegalFact(Mockito.any(LanguageEnum.class), Mockito.any(NotificationReceivedLegalFact.class)))
                .thenReturn(templatesClientMock.notificationReceivedLegalFact(LanguageEnum.IT, new NotificationReceivedLegalFact()));
        var result = Assertions.assertDoesNotThrow(() ->
                legalFactGeneratorTemplatesTest.generateNotificationReceivedLegalFact(notificationInt()));
        Assertions.assertNotNull(result);
    }

    private static NotificationInt notificationInt() {
        return NotificationInt.builder()
                .iun("TEST_TEST")
                .recipients(List.of(notificationRecipientInt()))
                .subject("subject_test")
                .sentAt(Instant.now())
                .sender(notificationSenderInt())
                .sentAt(Instant.now())
                .pagoPaIntMode(PagoPaIntMode.NONE)
                .documents(List.of(notificationDocumentInt()))
                .build();
    }

    private static NotificationRecipientInt notificationRecipientInt() {
        return NotificationRecipientInt.builder()
                .denomination("denomination_test")
                .taxId("taxId_test_test")
                .recipientType(RecipientTypeInt.PF)
                .physicalAddress(physicalAddressInt())
                .digitalDomicile(legalDigitalAddressInt())
                .build();
    }

    private static PhysicalAddressInt physicalAddressInt() {
        return PhysicalAddressInt.builder()
                .zip("00000")
                .build();
    }

    private static LegalDigitalAddressInt legalDigitalAddressInt() {
        return LegalDigitalAddressInt.builder()
                .type(LegalDigitalAddressInt.LEGAL_DIGITAL_ADDRESS_TYPE.PEC)
                .address("DigitalAddress_TEST")
                .build();
    }

    private static NotificationDocumentInt notificationDocumentInt() {
        return NotificationDocumentInt.builder()
                .contentType("PDF_TEST_TEST")
                .digests(NotificationDocumentInt.Digests.builder()
                        .sha256("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=")
                        .build())
                .build();
    }

    private static NotificationSenderInt notificationSenderInt() {
        return NotificationSenderInt.builder()
                .paDenomination("paDenomination_TEST_TEST")
                .paId("paId_TEST")
                .paTaxId("paTaxId_TEST_TEST")
                .build();
    }

    @TestConfiguration
    static class TestConfig {
        @Bean
        LegalFactGenerator legalFactGeneratorTemplates(CustomInstantWriter instantWriter,
                                                       PhysicalAddressWriter physicalAddressWriter,
                                                       PnDeliveryPushValidatorConfigs configs,
                                                       TemplatesClient templatesClient) {
            return new LegalFactGeneratorTemplates(instantWriter, physicalAddressWriter, configs, templatesClient);
        }

        @Bean
        CustomInstantWriter customInstantWriter() {
            return new CustomInstantWriter();
        }

        @Bean
        PhysicalAddressWriter physicalAddressWriter() {
            return new PhysicalAddressWriter();
        }

        @Bean
        PnDeliveryPushValidatorConfigs pnDeliveryPushValidatorConfigs() {
            PnDeliveryPushValidatorConfigs configs = Mockito.mock(PnDeliveryPushValidatorConfigs.class);
            Mockito.when(configs.isAdditionalLangsEnabled()).thenReturn(false);
            return configs;
        }
    }
}