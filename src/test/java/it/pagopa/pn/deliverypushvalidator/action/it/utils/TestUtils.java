package it.pagopa.pn.deliverypushvalidator.action.it.utils;

import it.pagopa.pn.deliverypushvalidator.action.it.mockbean.*;
import it.pagopa.pn.deliverypushvalidator.dto.address.LegalDigitalAddressInt;
import it.pagopa.pn.deliverypushvalidator.dto.cost.PaymentsInfoForRecipientInt;
import it.pagopa.pn.deliverypushvalidator.dto.ext.delivery.notification.*;
import it.pagopa.pn.deliverypushvalidator.dto.ext.safestorage.FileCreationWithContentRequest;
import it.pagopa.pn.deliverypushvalidator.dto.legalfacts.LegalFactCategoryInt;
import it.pagopa.pn.deliverypushvalidator.dto.legalfacts.LegalFactsIdInt;
import it.pagopa.pn.deliverypushvalidator.dto.timeline.EventId;
import it.pagopa.pn.deliverypushvalidator.dto.timeline.TimelineElementInternal;
import it.pagopa.pn.deliverypushvalidator.dto.timeline.TimelineEventId;
import it.pagopa.pn.deliverypushvalidator.dto.timeline.details.DeliveryModeInt;
import it.pagopa.pn.deliverypushvalidator.generated.openapi.msclient.pnsafestorage.model.FileCreationResponse;
import it.pagopa.pn.deliverypushvalidator.legalfact.LegalFactGenerator;
import it.pagopa.pn.deliverypushvalidator.logtest.ConsoleAppenderCustom;
import it.pagopa.pn.deliverypushvalidator.service.TimelineService;
import it.pagopa.pn.deliverypushvalidator.utils.ThreadPool;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.mockito.Mockito;

import java.io.IOException;
import java.util.*;

@Slf4j
public class TestUtils {
    public static final String PN_NOTIFICATION_ATTACHMENT = "PN_NOTIFICATION_ATTACHMENT";
    public static final String TOO_BIG = "TOO_BIG";
    public static final String NOT_A_PDF = "NOT_A_PDF";


    public static boolean checkIsPresentNationalRegistryValidationCall(String iun, TimelineService timelineService) {
        Optional<TimelineElementInternal> timelineElementOpt = timelineService.getTimelineElement(
                iun,
                buildTimelineEventIdNationalRegistryValidationCall(iun)
        );

        return timelineElementOpt.isPresent();
    }

    public static String buildTimelineEventIdNationalRegistryValidationCall(String iun) {
        return TimelineEventId.NATIONAL_REGISTRY_VALIDATION_CALL.buildEventId(
                EventId.builder()
                        .iun(iun)
                        .deliveryMode(DeliveryModeInt.ANALOG)
                        .build()
        );
    }

    public static boolean checkIsPresentNationalRegistryValidationResponse(String correlationId, String iun, Integer recIndex, TimelineService timelineService) {
        Optional<TimelineElementInternal> timelineElementOpt = timelineService.getTimelineElement(
                iun,
                TimelineEventId.NATIONAL_REGISTRY_VALIDATION_RESPONSE.buildEventId(
                        EventId.builder()
                                .relatedTimelineId(correlationId)
                                .recIndex(recIndex)
                                .build()
                )
        );

        return timelineElementOpt.isPresent();
    }

    public static boolean checkIsPresentRequestAccepted(String iun, TimelineService timelineService) {
        Optional<TimelineElementInternal> timelineElementOpt = timelineService.getTimelineElement(
                iun,
                TimelineEventId.REQUEST_ACCEPTED.buildEventId(
                        EventId.builder()
                                .iun(iun)
                                .build())
        );

        return timelineElementOpt.isPresent();
    }

    public static boolean checkIsPresentNotificationRejected(String iun, TimelineService timelineService) {
        return getNotificationRejected(iun, timelineService).isPresent();
    }

    public static Optional<TimelineElementInternal> getNotificationRejected(String iun, TimelineService timelineService) {
        return timelineService.getTimelineElement(
                iun,
                TimelineEventId.REQUEST_REFUSED.buildEventId(
                        EventId.builder()
                                .iun(iun)
                                .build()
                )
        );
    }


    public static List<NotificationDocumentInt> firstFileUploadFromNotification(
            List<DocumentWithContent> documentWithContentList,
            List<NotificationDocumentInt> listNotificationDocument,
            SafeStorageClientMock safeStorageClientMock)
    {
        for (DocumentWithContent documentWithContent : documentWithContentList) {
            FileCreationWithContentRequest fileCreationWithContentRequest = new FileCreationWithContentRequest();
            fileCreationWithContentRequest.setContentType("application/pdf");
            fileCreationWithContentRequest.setDocumentType(PN_NOTIFICATION_ATTACHMENT);
            fileCreationWithContentRequest.setContent(documentWithContent.getContent().getBytes());

            listNotificationDocument = createFileAndGetDocumentList(listNotificationDocument, safeStorageClientMock, documentWithContent.getDocument(), fileCreationWithContentRequest);
        }
        return listNotificationDocument;
    }

    private static @NotNull List<NotificationDocumentInt> createFileAndGetDocumentList(List<NotificationDocumentInt> listNotificationDocument,
                                                                                       SafeStorageClientMock safeStorageClientMock,
                                                                                       NotificationDocumentInt documentToUpload,
                                                                                       FileCreationWithContentRequest fileCreationWithContentRequest) {
        
        FileCreationResponse response = safeStorageClientMock.createFile(fileCreationWithContentRequest, documentToUpload.getDigests().getSha256()).block();
        listNotificationDocument = listNotificationDocument.stream().filter(doc -> doc.equals(documentToUpload))
                        .map(doc -> {
                                    NotificationDocumentInt.Ref actualRefWithoutKey = doc.getRef();
                                    return doc.toBuilder()
                                            .ref(actualRefWithoutKey.toBuilder()
                                                    .key(response.getKey())
                                                    .build())
                                            .build();
                        }).toList();
        return listNotificationDocument;
    }

    public static List<NotificationDocumentInt> firstFileUploadFromNotificationTooBig(List<DocumentWithContent> documentWithContentList,
                                                             List<NotificationDocumentInt> listNotificationDocument,
                                                             SafeStorageClientMock safeStorageClientMock) {
        for (DocumentWithContent documentWithContent : documentWithContentList) {
            FileCreationWithContentRequest fileCreationWithContentRequest = new FileCreationWithContentRequest();
            fileCreationWithContentRequest.setContentType("application/pdf" + TOO_BIG);
            fileCreationWithContentRequest.setDocumentType(PN_NOTIFICATION_ATTACHMENT);
            fileCreationWithContentRequest.setContent(documentWithContent.getContent().getBytes());
            safeStorageClientMock.createFile(fileCreationWithContentRequest, documentWithContent.getDocument().getDigests().getSha256());
            listNotificationDocument = createFileAndGetDocumentList(listNotificationDocument, safeStorageClientMock, documentWithContent.getDocument(), fileCreationWithContentRequest);
        }
        return listNotificationDocument;
    }


    public static List<NotificationDocumentInt> firstFileUploadFromNotificationNotAPDF(List<DocumentWithContent> documentWithContentList,
                                                              List<NotificationDocumentInt> listNotificationDocument,
                                                              SafeStorageClientMock safeStorageClientMock) {
        for (DocumentWithContent documentWithContent : documentWithContentList) {
            FileCreationWithContentRequest fileCreationWithContentRequest = new FileCreationWithContentRequest();
            fileCreationWithContentRequest.setContentType("application/pdf" + NOT_A_PDF);
            fileCreationWithContentRequest.setDocumentType(PN_NOTIFICATION_ATTACHMENT);
            fileCreationWithContentRequest.setContent(documentWithContent.getContent().getBytes());
            listNotificationDocument = createFileAndGetDocumentList(listNotificationDocument, safeStorageClientMock, documentWithContent.getDocument(), fileCreationWithContentRequest);
        }
        
        return listNotificationDocument;
    }

    public static NotificationInt firstFileUploadFromNotificationError(NotificationInt notification, SafeStorageClientMock safeStorageClientMock, byte[] differentFileContent) {
        List<NotificationDocumentInt> listNotificationDocument = notification.getDocuments();

        for (NotificationDocumentInt attachment : notification.getDocuments()) {
            FileCreationWithContentRequest fileCreationWithContentRequest = new FileCreationWithContentRequest();
            fileCreationWithContentRequest.setContentType("application/pdf");
            fileCreationWithContentRequest.setDocumentType(PN_NOTIFICATION_ATTACHMENT);
            fileCreationWithContentRequest.setContent(differentFileContent);
            safeStorageClientMock.createFile(fileCreationWithContentRequest, attachment.getDigests().getSha256());
            listNotificationDocument = createFileAndGetDocumentList(listNotificationDocument, safeStorageClientMock, attachment, fileCreationWithContentRequest);
        }
        return notification.toBuilder()
                .documents(listNotificationDocument)
                .build();
    }

    public static List<DocumentWithContent> getDocumentWithContents(String fileDoc, List<NotificationDocumentInt> notificationDocumentList) {
        DocumentWithContent documentWithContent = DocumentWithContent.builder()
                .content(fileDoc)
                .document(notificationDocumentList.get(0))
                .build();
        return Collections.singletonList(documentWithContent);
    }

    public static List<NotificationDocumentInt> getDocumentList(String fileDoc) {

        return List.of(
                NotificationDocumentInt.builder()
                        .ref(NotificationDocumentInt.Ref.builder()
                                .key(null) //Nota la file key è null, in questa fase non è dato saperla dovrà essere valorizzata da safeStorage e aggiornata nel test
                                .versionToken("v01_doc00")
                                .build()
                        )
                        .digests(NotificationDocumentInt.Digests.builder()
                                .sha256(Base64.getEncoder().encodeToString(fileDoc.getBytes()))
                                .build()
                        )
                        .build()
        );
    }

    public static List<NotificationPaymentInfoInt> getPaymentWithF24(NotificationDocumentInt paymentDocumentInt) {
        return List.of(
                NotificationPaymentInfoInt.builder()
                        .f24(F24Int.builder()
                                .applyCost(true)
                                .title("payment_f24_1")
                                .metadataAttachment(paymentDocumentInt)
                                .build()
                        )
                        .pagoPA(null)
                        .build()
        );
    }

    public static void writeAllGeneratedLegalFacts(String iun, String className, TimelineService timelineService, SafeStorageClientMock safeStorageClientMock) {
        writeAllGeneratedLegalFacts(iun, className, timelineService, safeStorageClientMock, 3);
    }

    public static void writeAllGeneratedLegalFacts(String iun, String className, TimelineService timelineService, SafeStorageClientMock safeStorageClientMock, int depth) {
        String testName = className + "-" + getMethodName(depth);

        timelineService.getTimeline(iun, true).forEach(
                elem -> {
                    if (!elem.getLegalFactsIds().isEmpty()) {
                        LegalFactsIdInt legalFactsId = elem.getLegalFactsIds().get(0);
                        if (!LegalFactCategoryInt.PEC_RECEIPT.equals(legalFactsId.getCategory()) && !LegalFactCategoryInt.ANALOG_DELIVERY.equals(legalFactsId.getCategory())) {
                            String key = legalFactsId.getKey().replace("safestorage://", "");
                            log.info("[TEST] writing safestoragemock key={} testName={} cat={}", key, testName, legalFactsId.getCategory());
                            safeStorageClientMock.writeFile(key, legalFactsId.getCategory(), testName);
                        }
                    }
                }
        );
    }

    public static void checkGeneratedLegalFacts(NotificationInt notification,
                                                GeneratedLegalFactsInfo generatedLegalFactsInfo,
                                                LegalFactGenerator legalFactGenerator
    ) {
        TestUtils.checkNotificationReceivedLegalFactGeneration(
                notification,
                legalFactGenerator,
                generatedLegalFactsInfo.isNotificationReceivedLegalFactGenerated()
        );
    }

    private static int getTimes(boolean itWasGenerated) {
        return itWasGenerated ? 1 : 0;
    }

    private static void checkNotificationReceivedLegalFactGeneration(NotificationInt notification,
                                                                     LegalFactGenerator legalFactGenerator,
                                                                     boolean itWasGenerated) {
        int times = getTimes(itWasGenerated);

        try {
            Mockito.verify(legalFactGenerator, Mockito.times(times)).generateNotificationReceivedLegalFact(notification);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public static NotificationInt getNotification() {
        return NotificationInt.builder()
                .iun("IUN_01")
                .paProtocolNumber("protocol_01")
                .sender(NotificationSenderInt.builder()
                        .paId(" pa_02")
                        .build()
                )
                .recipients(Collections.singletonList(
                        NotificationRecipientInt.builder()
                                .taxId("testIdRecipient")
                                .internalId("test")
                                .denomination("Nome Cognome/Ragione Sociale")
                                .digitalDomicile(LegalDigitalAddressInt.builder()
                                        .type(LegalDigitalAddressInt.LEGAL_DIGITAL_ADDRESS_TYPE.PEC)
                                        .address("account@dominio.it")
                                        .build())
                                .build()
                ))
                .build();
    }

    public static NotificationInt getNotificationV2() {
        return getNotificationV2(null);
    }

    public static NotificationInt getNotificationV2(UsedServicesInt usedServices) {
        return NotificationInt.builder()
                .iun("IUN_01")
                .paProtocolNumber("protocol_01")
                .sender(NotificationSenderInt.builder()
                        .paId(" pa_02")
                        .build()
                )
                .documents(List.of(NotificationDocumentInt.builder()
                        .digests(NotificationDocumentInt.Digests.builder()
                                .sha256("sha256").build())
                        .ref(NotificationDocumentInt.Ref.builder().build())
                        .build()))
                .recipients(Collections.singletonList(
                        NotificationRecipientInt.builder()
                                .taxId("testIdRecipient")
                                .internalId("test")
                                .denomination("Nome Cognome/Ragione Sociale")
                                .digitalDomicile(LegalDigitalAddressInt.builder()
                                        .type(LegalDigitalAddressInt.LEGAL_DIGITAL_ADDRESS_TYPE.PEC)
                                        .address("account@dominio.it")
                                        .build())
                                .payments(List.of(NotificationPaymentInfoInt.builder()
                                        .pagoPA(PagoPaInt.builder()
                                                .noticeCode("noticeCode")
                                                .creditorTaxId("taxId")
                                                .attachment(NotificationDocumentInt.builder()
                                                        .ref(NotificationDocumentInt.Ref.builder().build())
                                                        .digests(NotificationDocumentInt.Digests.builder()
                                                                .sha256("sha256").build())
                                                        .build())
                                                .build())
                                        .build()))
                                .build()
                ))
                .usedServices(usedServices)
                .build();
    }

    public static NotificationInt getNotificationV2WithDocument() {
        return NotificationInt.builder()
                .iun("IUN_01")
                .paProtocolNumber("protocol_01")
                .sender(NotificationSenderInt.builder()
                        .paId(" pa_02")
                        .build()
                )
                .documents(List.of(NotificationDocumentInt.builder()
                        .digests(NotificationDocumentInt.Digests.builder()
                                .sha256("sha256").build())
                        .ref(NotificationDocumentInt.Ref.builder().key("test").versionToken("1").build())
                        .build()))
                .recipients(Collections.singletonList(
                        NotificationRecipientInt.builder()
                                .taxId("testIdRecipient")
                                .internalId("test")
                                .denomination("Nome Cognome/Ragione Sociale")
                                .digitalDomicile(LegalDigitalAddressInt.builder()
                                        .type(LegalDigitalAddressInt.LEGAL_DIGITAL_ADDRESS_TYPE.PEC)
                                        .address("account@dominio.it")
                                        .build())
                                .payments(List.of(NotificationPaymentInfoInt.builder()
                                        .pagoPA(PagoPaInt.builder()
                                                .noticeCode("noticeCode")
                                                .creditorTaxId("taxId")
                                                .attachment(NotificationDocumentInt.builder()
                                                        .ref(NotificationDocumentInt.Ref.builder().key("paymentAttach").versionToken("1").build())
                                                        .digests(NotificationDocumentInt.Digests.builder()
                                                                .sha256("sha256").build())
                                                        .build())
                                                .build())
                                        .build()))
                                .build()
                ))
                .build();
    }

    public static NotificationInt getNotificationV2WithDocument(LegalDigitalAddressInt.LEGAL_DIGITAL_ADDRESS_TYPE channelType, String address) {
        return NotificationInt.builder()
                .iun("IUN_01")
                .paProtocolNumber("protocol_01")
                .sender(NotificationSenderInt.builder()
                        .paId(" pa_02")
                        .build()
                )
                .documents(List.of(NotificationDocumentInt.builder()
                        .digests(NotificationDocumentInt.Digests.builder()
                                .sha256("sha256").build())
                        .ref(NotificationDocumentInt.Ref.builder().key("test").versionToken("1").build())
                        .build()))
                .recipients(Collections.singletonList(
                        NotificationRecipientInt.builder()
                                .taxId("testIdRecipient")
                                .internalId("test")
                                .denomination("Nome Cognome/Ragione Sociale")
                                .digitalDomicile(LegalDigitalAddressInt.builder()
                                        .type(channelType)
                                        .address(address)
                                        .build())
                                .payments(List.of(NotificationPaymentInfoInt.builder()
                                        .pagoPA(PagoPaInt.builder()
                                                .noticeCode("noticeCode")
                                                .creditorTaxId("taxId")
                                                .attachment(NotificationDocumentInt.builder()
                                                        .ref(NotificationDocumentInt.Ref.builder().key("paymentAttach").versionToken("1").build())
                                                        .digests(NotificationDocumentInt.Digests.builder()
                                                                .sha256("sha256").build())
                                                        .build())
                                                .build())
                                        .build()))
                                .build()
                ))
                .build();
    }

    public static NotificationInt getNotificationV2WithoutPayments() {
        return NotificationInt.builder()
                .iun("IUN_01")
                .paProtocolNumber("protocol_01")
                .sender(NotificationSenderInt.builder()
                        .paId(" pa_02")
                        .build()
                )
                .documents(List.of(NotificationDocumentInt.builder()
                        .digests(NotificationDocumentInt.Digests.builder()
                                .sha256("sha256").build())
                        .ref(NotificationDocumentInt.Ref.builder().key("test").versionToken("1").build())
                        .build()))
                .recipients(Collections.singletonList(
                        NotificationRecipientInt.builder()
                                .taxId("testIdRecipient")
                                .internalId("test")
                                .denomination("Nome Cognome/Ragione Sociale")
                                .digitalDomicile(LegalDigitalAddressInt.builder()
                                        .type(LegalDigitalAddressInt.LEGAL_DIGITAL_ADDRESS_TYPE.PEC)
                                        .address("account@dominio.it")
                                        .build())
                                .build()
                ))
                .build();
    }

    public static NotificationInt getNotificationV2WithF24() {
        return getNotificationV2WithF24(null);
    }

    public static NotificationInt getNotificationV2WithF24(UsedServicesInt usedServices) {
        return NotificationInt.builder()
                .iun("IUN_01")
                .paProtocolNumber("protocol_01")
                .vat(23)
                .sender(NotificationSenderInt.builder()
                        .paId(" pa_02")
                        .build()
                )
                .documents(List.of(NotificationDocumentInt.builder()
                        .digests(NotificationDocumentInt.Digests.builder()
                                .sha256("sha256").build())
                        .ref(NotificationDocumentInt.Ref.builder().key("test").versionToken("1").build())
                        .build()))
                .recipients(Collections.singletonList(
                        NotificationRecipientInt.builder()
                                .taxId("testIdRecipient")
                                .internalId("test")
                                .denomination("Nome Cognome/Ragione Sociale")
                                .digitalDomicile(LegalDigitalAddressInt.builder()
                                        .type(LegalDigitalAddressInt.LEGAL_DIGITAL_ADDRESS_TYPE.PEC)
                                        .address("account@dominio.it")
                                        .build())
                                .payments(List.of(NotificationPaymentInfoInt.builder()
                                        .pagoPA(PagoPaInt.builder()
                                                .noticeCode("noticeCode")
                                                .creditorTaxId("taxId")
                                                .attachment(NotificationDocumentInt.builder()
                                                        .ref(NotificationDocumentInt.Ref.builder().key("paymentAttach").versionToken("1").build())
                                                        .digests(NotificationDocumentInt.Digests.builder()
                                                                .sha256("sha256").build())
                                                        .build())
                                                .build())
                                        .f24(F24Int.builder()
                                                .title("title")
                                                .applyCost(true)
                                                .metadataAttachment(NotificationDocumentInt.builder()
                                                        .ref(NotificationDocumentInt.Ref.builder().key("paymentAttach").versionToken("2").build())
                                                        .digests(NotificationDocumentInt.Digests.builder()
                                                                .sha256("sha256").build())
                                                        .build())
                                                .build())
                                        .build()))
                                .build()
                ))
                .usedServices(usedServices)
                .build();
    }


    public static NotificationInt getNotificationMultiRecipient() {
        return NotificationInt.builder()
                .iun("IUN_01")
                .paProtocolNumber("protocol_01")
                .sender(NotificationSenderInt.builder()
                        .paId(" pa_02")
                        .build()
                )
                .recipients(Arrays.asList(
                        NotificationRecipientInt.builder()
                                .taxId("testIdRecipient")
                                .internalId("test")
                                .denomination("Nome Cognome/Ragione Sociale")
                                .digitalDomicile(LegalDigitalAddressInt.builder()
                                        .type(LegalDigitalAddressInt.LEGAL_DIGITAL_ADDRESS_TYPE.PEC)
                                        .address("account@dominio.it")
                                        .build())
                                .build(),
                        NotificationRecipientInt.builder()
                                .taxId("testIdRecipient")
                                .internalId("test")
                                .denomination("Nome Cognome/Ragione Sociale")
                                .digitalDomicile(LegalDigitalAddressInt.builder()
                                        .type(LegalDigitalAddressInt.LEGAL_DIGITAL_ADDRESS_TYPE.PEC)
                                        .address("account@dominio.it")
                                        .build())
                                .build()
                ))
                .build();
    }

    public static String getMethodName(final int depth) {
        final StackTraceElement[] ste = Thread.currentThread().getStackTrace();
        return ste[depth].getMethodName();
    }

    public static String getMethodNameAndClassName(final int depth) {
        final StackTraceElement[] ste = Thread.currentThread().getStackTrace();
        return ste[depth].getClassName()+"."+ste[depth].getMethodName();
    }

    public static String getRandomIun(int level) {
        String callerMethod = getMethodName(level);
        return getIun(callerMethod);
    }

    public static String getRandomIun() {
        String callerMethod = getMethodName(3);
        return getIun(callerMethod);
    }
    
    @NotNull
    private static String getIun(String callerMethod) {
        Random rand = new Random();
        int upperbound = 10000;
        int intRandom = rand.nextInt(upperbound);
        return "iun-" + callerMethod + "_" + intRandom;
    }


    public static void initializeAllMockClient(SafeStorageClientMock safeStorageClientMock,
                                               PnDeliveryClientMock pnDeliveryClientMock,
                                               NationalRegistriesClientMock nationalRegistriesClientMock,
                                               PnDataVaultClientReactiveMock pnDataVaultClientReactiveMock,
                                               DocumentCreationRequestDaoMock documentCreationRequestDaoMock,
                                               AddressManagerClientMock addressManagerClientMock,
                                               ActionPoolMock actionPoolMock
    ) {

        log.info("CLEARING MOCKS");

        ThreadPool.killThreads();

        safeStorageClientMock.clear();
        pnDeliveryClientMock.clear();
        nationalRegistriesClientMock.clear();
        pnDataVaultClientReactiveMock.clear();
        documentCreationRequestDaoMock.clear();
        addressManagerClientMock.clear();
        actionPoolMock.clear();
        
        ConsoleAppenderCustom.initializeLog();
    }

    public static void verifyPaymentInfo(NotificationInt notification, int recIndex, List<PaymentsInfoForRecipientInt> paymentsInfoForRecipientsCaptured) {
        notification.getRecipients().forEach(rec ->
                rec.getPayments().forEach(payment -> {
                    final PagoPaInt paymentPagoPA = payment.getPagoPA();
                    if(paymentPagoPA != null && paymentPagoPA.getApplyCost()){
                        Optional<PaymentsInfoForRecipientInt> paymentsInfoForRecipient = paymentsInfoForRecipientsCaptured.stream()
                                .filter(x -> x.getCreditorTaxId().equals(paymentPagoPA.getCreditorTaxId()) &&
                                        x.getNoticeCode().equals(paymentPagoPA.getNoticeCode()) &&
                                        x.getRecIndex().equals(recIndex)).findFirst();

                        Assertions.assertTrue(paymentsInfoForRecipient.isPresent());
                    }
                })
        );
    }


    public static NotificationRecipientInt getNotificationRecipientInt() {
        return NotificationRecipientInt.builder()
                .taxId("testIdRecipient")
                .internalId("test")
                .denomination("Nome Cognome/Ragione Sociale")
                .digitalDomicile(LegalDigitalAddressInt.builder()
                        .type(LegalDigitalAddressInt.LEGAL_DIGITAL_ADDRESS_TYPE.PEC)
                        .address("account@dominio.it")
                        .build())
                .payments(List.of(NotificationPaymentInfoInt.builder()
                        .pagoPA(PagoPaInt.builder()
                                .noticeCode("noticeCode")
                                .creditorTaxId("taxId")
                                .attachment(NotificationDocumentInt.builder()
                                        .ref(NotificationDocumentInt.Ref.builder().build())
                                        .digests(NotificationDocumentInt.Digests.builder()
                                                .sha256("sha256").build())
                                        .build())
                                .build())
                        .f24(F24Int.builder()
                                .title("title")
                                .applyCost(true)
                                .metadataAttachment(NotificationDocumentInt.builder()
                                        .ref(NotificationDocumentInt.Ref.builder().build())
                                        .digests(NotificationDocumentInt.Digests.builder()
                                                .sha256("sha256").build())
                                        .build())
                                .build())
                        .build()))
                .build();
    }

    @Builder
    @Getter
    public static class GeneratedLegalFactsInfo {
        boolean notificationReceivedLegalFactGenerated;
        boolean notificationAARGenerated;
        boolean notificationViewedLegalFactGenerated;
        boolean pecDeliveryWorkflowLegalFactsGenerated;
        boolean notificationCompletelyUnreachableLegalFactGenerated;
        boolean notificationCancelled;
    }

    @Builder
    @Getter
    public static class DocumentWithContent {
        String content;
        NotificationDocumentInt document;
    }
}
