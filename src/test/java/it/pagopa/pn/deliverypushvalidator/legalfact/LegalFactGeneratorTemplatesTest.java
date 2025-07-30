package it.pagopa.pn.deliverypushvalidator.legalfact;

import it.pagopa.pn.deliverypushvalidator.config.PnDeliveryPushValidatorConfigs;
import it.pagopa.pn.deliverypushvalidator.dto.address.LegalDigitalAddressInt;
import it.pagopa.pn.deliverypushvalidator.dto.address.PhysicalAddressInt;
import it.pagopa.pn.deliverypushvalidator.dto.ext.datavault.RecipientTypeInt;
import it.pagopa.pn.deliverypushvalidator.dto.ext.delivery.notification.*;
import it.pagopa.pn.deliverypushvalidator.generated.openapi.msclient.templatesengine.model.LanguageEnum;
import it.pagopa.pn.deliverypushvalidator.generated.openapi.msclient.templatesengine.model.NotificationReceivedLegalFact;
import it.pagopa.pn.deliverypushvalidator.action.it.CommonTestConfiguration;
import it.pagopa.pn.deliverypushvalidator.action.it.mockbean.TemplatesClientMock;
import it.pagopa.pn.deliverypushvalidator.middleware.externalclient.pnclient.templatesengine.TemplatesClient;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.List;

@SpringBootTest
class LegalFactGeneratorTemplatesTest extends CommonTestConfiguration {

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


    @Test
    void testBuildAarSenderLogo() {
        // Arrange
        String paId = "12345";
        String templateUrl = "TO_BASE64_RESOLVER:https://example.com/<PA_ID>/logo.png";
        String expectedUrl = "TO_BASE64_RESOLVER:https://example.com/" + paId + "/logo.png";

        PnDeliveryPushValidatorConfigs mockPnDeliveryPushConfigs = Mockito.mock(PnDeliveryPushValidatorConfigs.class);
        PnDeliveryPushValidatorConfigs.Webapp mockWebapp = Mockito.mock(PnDeliveryPushValidatorConfigs.Webapp.class);

        Mockito.when(mockPnDeliveryPushConfigs.getWebapp()).thenReturn(mockWebapp);
        Mockito.when(mockWebapp.getAarSenderLogoUrlTemplate())
                .thenReturn(templateUrl);

        ReflectionTestUtils.setField(legalFactGeneratorTemplatesTest, "pnDeliveryPushConfigs", mockPnDeliveryPushConfigs);

        // Act
        String actualUrl = ReflectionTestUtils.invokeMethod(legalFactGeneratorTemplatesTest, "buildAarSenderLogo", paId);

        // Assert
        Assertions.assertEquals(expectedUrl, actualUrl);
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
                        .sha256("string")
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
}