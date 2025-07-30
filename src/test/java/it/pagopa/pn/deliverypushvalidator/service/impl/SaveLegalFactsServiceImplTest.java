package it.pagopa.pn.deliverypushvalidator.service.impl;

import it.pagopa.pn.commons.exceptions.PnInternalException;
import it.pagopa.pn.deliverypushvalidator.dto.address.LegalDigitalAddressInt;
import it.pagopa.pn.deliverypushvalidator.dto.address.PhysicalAddressInt;
import it.pagopa.pn.deliverypushvalidator.dto.ext.delivery.notification.NotificationDocumentInt;
import it.pagopa.pn.deliverypushvalidator.dto.ext.delivery.notification.NotificationInt;
import it.pagopa.pn.deliverypushvalidator.dto.ext.delivery.notification.NotificationRecipientInt;
import it.pagopa.pn.deliverypushvalidator.dto.ext.delivery.notification.NotificationSenderInt;
import it.pagopa.pn.deliverypushvalidator.dto.ext.safestorage.FileCreationResponseInt;
import it.pagopa.pn.deliverypushvalidator.dto.ext.safestorage.FileCreationWithContentRequest;
import it.pagopa.pn.deliverypushvalidator.legalfact.LegalFactGenerator;
import it.pagopa.pn.deliverypushvalidator.service.SafeStorageService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.time.Instant;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;

class SaveLegalFactsServiceImplTest {

    public static final String LEGALFACTS_MEDIATYPE_STRING = "application/pdf";
    public static final String PN_LEGAL_FACTS = "PN_LEGAL_FACTS";
    public static final String SAVED = "SAVED";

    @Mock
    private LegalFactGenerator legalFactBuilder;

    @Mock
    private SafeStorageService safeStorageService;

    private SaveLegalFactsServiceImpl saveLegalFactsService;

    @BeforeEach
    void setUp() {

        legalFactBuilder = Mockito.mock(LegalFactGenerator.class);
        safeStorageService = Mockito.mock(SafeStorageService.class);

        saveLegalFactsService = new SaveLegalFactsServiceImpl(legalFactBuilder, safeStorageService);
    }

    @Test
    void saveNotificationReceivedLegalFact() throws IOException {
        String denomination = "<h1>SSRF WITH IMAGE POC</h1> <img src='https://prova.it'></img>";
        NotificationInt notification = buildNotification(denomination);
        FileCreationWithContentRequest fileCreation = buildFileCreationWithContentRequest(PN_LEGAL_FACTS);
        FileCreationResponseInt file = buildFileCreationResponseInt();

        Mockito.when(legalFactBuilder.generateNotificationReceivedLegalFact(notification)).thenReturn(denomination.getBytes());
        Mockito.when(safeStorageService.createAndUploadContent(fileCreation)).thenReturn(Mono.just(file));

        String actual = saveLegalFactsService.sendCreationRequestForNotificationReceivedLegalFact(notification);

        Assertions.assertEquals("safestorage://001", actual);
    }

    @Test
    void saveNotificationReceivedLegalFactFailed() {
        String denomination = "<h1>SSRF WITH IMAGE POC</h1> <img src='https://prova.it'></img>";
        NotificationInt notification = buildNotification(denomination);
        FileCreationWithContentRequest fileCreation = buildFileCreationWithContentRequest(PN_LEGAL_FACTS);
        FileCreationResponseInt file = buildFileCreationResponseInt();

        PnInternalException pnInternalException = Assertions.assertThrows(PnInternalException.class, () -> {
            saveLegalFactsService.sendCreationRequestForNotificationReceivedLegalFact(notification);
        });

        String expectErrorMsg = "PN_DELIVERYPUSH_SAVELEGALFACTSFAILED";

        Assertions.assertEquals(expectErrorMsg, pnInternalException.getProblem().getErrors().get(0).getCode());
    }

    private FileCreationWithContentRequest buildFileCreationWithContentRequest(String type) {
        String denomination = "<h1>SSRF WITH IMAGE POC</h1> <img src='https://prova.it'></img>";

        FileCreationWithContentRequest fileCreationRequest = new FileCreationWithContentRequest();
        fileCreationRequest.setContentType(LEGALFACTS_MEDIATYPE_STRING);
        fileCreationRequest.setDocumentType(type);
        fileCreationRequest.setStatus(SAVED);
        fileCreationRequest.setContent(denomination.getBytes());
        return fileCreationRequest;
    }

    private FileCreationResponseInt buildFileCreationResponseInt() {
        return FileCreationResponseInt.builder()
                .key("001")
                .build();
    }

    private NotificationInt buildNotification(String denomination) {
        return NotificationInt.builder()
                .sender(createSender())
                .sentAt(Instant.now())
                .iun("Example_IUN_1234_Test")
                .subject("notification test subject")
                .documents(Arrays.asList(
                                NotificationDocumentInt.builder()
                                        .ref(NotificationDocumentInt.Ref.builder()
                                                .key("doc00")
                                                .versionToken("v01_doc00")
                                                .build()
                                        )
                                        .digests(NotificationDocumentInt.Digests.builder()
                                                .sha256((Base64.getEncoder().encodeToString("sha256_doc01".getBytes())))
                                                .build()
                                        )
                                        .build()
                        )
                )
                .recipients(Collections.singletonList(buildRecipient(denomination)))
                .build();
    }

    private NotificationRecipientInt buildRecipient(String denomination) {
        String defaultDenomination = StringUtils.hasText(denomination) ? denomination : "Galileo Bruno";
        NotificationRecipientInt rec1 = NotificationRecipientInt.builder()
                .taxId("CDCFSC11R99X001Z")
                .denomination(defaultDenomination)
                .digitalDomicile(LegalDigitalAddressInt.builder()
                        .address("test@dominioPec.it")
                        .type(LegalDigitalAddressInt.LEGAL_DIGITAL_ADDRESS_TYPE.PEC)
                        .build())
                .physicalAddress(buildPhysicalAddressInt())
                .build();

        return rec1;
    }

    private PhysicalAddressInt buildPhysicalAddressInt() {
        return new PhysicalAddressInt(
                "Galileo Bruno",
                "Palazzo dell'Inquisizione",
                "corso Italia 666",
                "Piano Terra (piatta)",
                "00100",
                "Roma",
                null,
                "RM",
                "IT"
        );
    }

    private NotificationSenderInt createSender() {
        return NotificationSenderInt.builder()
                .paId("TEST_PA_ID")
                .paTaxId("TEST_TAX_ID")
                .paDenomination("TEST_PA_DENOMINATION")
                .build();
    }
}
