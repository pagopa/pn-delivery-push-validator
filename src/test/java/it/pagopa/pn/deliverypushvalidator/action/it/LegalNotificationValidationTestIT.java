package it.pagopa.pn.deliverypushvalidator.action.it;

import it.pagopa.pn.commons.exceptions.PnIdConflictException;
import it.pagopa.pn.deliverypushvalidator.action.it.mockbean.*;
import it.pagopa.pn.deliverypushvalidator.action.it.utils.NotificationRecipientTestBuilder;
import it.pagopa.pn.deliverypushvalidator.action.it.utils.NotificationTestBuilder;
import it.pagopa.pn.deliverypushvalidator.action.it.utils.PhysicalAddressBuilder;
import it.pagopa.pn.deliverypushvalidator.action.it.utils.TestUtils;
import it.pagopa.pn.deliverypushvalidator.action.startworkflow.StartWorkflowHandler;
import it.pagopa.pn.deliverypushvalidator.action.utils.NotificationUtils;
import it.pagopa.pn.deliverypushvalidator.dto.address.LegalDigitalAddressInt;
import it.pagopa.pn.deliverypushvalidator.dto.address.PhysicalAddressInt;
import it.pagopa.pn.deliverypushvalidator.dto.cost.RefusalCostMode;
import it.pagopa.pn.deliverypushvalidator.dto.ext.datavault.RecipientTypeInt;
import it.pagopa.pn.deliverypushvalidator.dto.ext.delivery.notification.*;
import it.pagopa.pn.deliverypushvalidator.dto.timeline.EventId;
import it.pagopa.pn.deliverypushvalidator.dto.timeline.TimelineEventId;
import it.pagopa.pn.deliverypushvalidator.exception.PnDeliveryPushValidatorExceptionCodes;
import it.pagopa.pn.deliverypushvalidator.generated.openapi.msclient.delivery.model.NotificationFeePolicy;
import it.pagopa.pn.deliverypushvalidator.legalfact.LegalFactGenerator;
import it.pagopa.pn.deliverypushvalidator.logtest.ConsoleAppenderCustom;
import it.pagopa.pn.deliverypushvalidator.service.TimelineService;
import it.pagopa.pn.deliverypushvalidator.utils.PnTechnicalRefusalCostMode;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static it.pagopa.pn.deliverypushvalidator.action.it.mockbean.F24ClientMock.F24_VALIDATION_FAIL;
import static it.pagopa.pn.deliverypushvalidator.action.it.mockbean.NationalRegistriesClientMock.PHYS_ADDR_ERROR;
import static it.pagopa.pn.deliverypushvalidator.action.it.mockbean.NationalRegistriesClientMock.PHYS_ADDR_NOT_FOUND;
import static org.awaitility.Awaitility.await;

class LegalNotificationValidationTestIT extends CommonNotificationValidationTestIT {

    //digital
    public static final String EXT_CHANNEL_SEND_FAIL_BOTH = "fail-both";

    //ANALOG
    public static final String EXTCHANNEL_SEND_SUCCESS = "OK"; //Invio notifica ok
    public static final String EXT_CHANNEL_SEND_NEW_ADDR = "NEW_ADDR:"; //Invio notifica fallita con nuovo indirizzo da investigazione

    @MockitoSpyBean
    LegalFactGenerator legalFactGenerator;
    @Autowired
    StartWorkflowHandler startWorkflowHandler;
    @Autowired
    TimelineService timelineService;
    @Autowired
    SafeStorageClientMock safeStorageClientMock;
    @Autowired
    PnDeliveryClientMock pnDeliveryClientMock;
    @MockitoBean
    PnTechnicalRefusalCostMode pnTechnicalRefusalCostMode;

    @Test
    void taxIdNotValidTest() throws PnIdConflictException {
        //Special address is present and all sending attempts fail
        LegalDigitalAddressInt digitalDomicile = LegalDigitalAddressInt.builder()
                .address("digitalDomicile@" + EXT_CHANNEL_SEND_FAIL_BOTH) //aggiungere stringa presente su delivery-push
                .type(LegalDigitalAddressInt.LEGAL_DIGITAL_ADDRESS_TYPE.PEC)
                .build();

        NotificationRecipientInt recipient = NotificationRecipientTestBuilder.builder()
                .withTaxId("TAXID01_" + NationalRegistriesClientMock.NOT_VALID)
                .withDigitalDomicile(digitalDomicile)
                .build();

        String fileDoc = "sha256_doc00";
        List<NotificationDocumentInt> notificationDocumentList = TestUtils.getDocumentList(fileDoc);
        List<TestUtils.DocumentWithContent> listDocumentWithContent = TestUtils.getDocumentWithContents(fileDoc, notificationDocumentList);
        notificationDocumentList = TestUtils.firstFileUploadFromNotification(listDocumentWithContent, notificationDocumentList, safeStorageClientMock);

        NotificationInt notification = NotificationTestBuilder.builder()
                .withNotificationDocuments(notificationDocumentList)
                .withPaId("paId01")
                .withNotificationRecipient(recipient)
                .build();

        byte[] differentFileSha = "error".getBytes();
        notification = TestUtils.firstFileUploadFromNotificationError(notification, safeStorageClientMock, differentFileSha);


        pnDeliveryClientMock.addNotification(notification);
        String iun = notification.getIun();

        //WHEN the workflow start
        startWorkflowHandler.startWorkflow(iun, notification.getCommunicationType());

        //THEN
        await().untilAsserted(() ->
                //Check worfklow is failed
                Assertions.assertTrue(timelineService.getTimelineElement(
                        iun,
                        TimelineEventId.REQUEST_REFUSED.buildEventId(
                                EventId.builder()
                                        .iun(iun)
                                        .build())).isPresent()
                )
        );
        ConsoleAppenderCustom.checkLogs();
    }


    @Test
    void addressNotValidTest() {
        NotificationRecipientInt recipient = NotificationRecipientTestBuilder.builder()
                .withTaxId("TAXID01")
                .withPhysicalAddress(PhysicalAddressBuilder.builder()
                        .withAddress("Via Nuova_" + AddressManagerClientMock.ADDRESS_MANAGER_NOT_VALID_ADDRESS)
                        .build())
                .build();

        String fileDoc = "sha256_doc00";
        List<NotificationDocumentInt> notificationDocumentList = TestUtils.getDocumentList(fileDoc);
        List<TestUtils.DocumentWithContent> listDocumentWithContent = TestUtils.getDocumentWithContents(fileDoc, notificationDocumentList);
        notificationDocumentList = TestUtils.firstFileUploadFromNotification(listDocumentWithContent, notificationDocumentList, safeStorageClientMock);

        NotificationInt notification = NotificationTestBuilder.builder()
                .withNotificationDocuments(notificationDocumentList)
                .withPaId("paId01")
                .withNotificationRecipient(recipient)
                .build();


        pnDeliveryClientMock.addNotification(notification);
        
        String iun = notification.getIun();

        //WHEN the workflow start
        startWorkflowHandler.startWorkflow(iun, notification.getCommunicationType());

        //THEN
        await().untilAsserted(() ->
                //Check worfklow is failed
                Assertions.assertTrue(timelineService.getTimelineElement(
                        iun,
                        TimelineEventId.REQUEST_REFUSED.buildEventId(
                                EventId.builder()
                                        .iun(iun)
                                        .build())).isPresent()
                )
        );
        ConsoleAppenderCustom.checkLogs();
    }

    @Test
    void f24ValidationKo() {
        // GIVEN
        PhysicalAddressInt paPhysicalAddress1 = PhysicalAddressBuilder.builder()
                .withAddress(EXT_CHANNEL_SEND_NEW_ADDR + EXTCHANNEL_SEND_SUCCESS + " Via Nuova")
                .build();

        String paymentDocName = "metadata_0_0";
        NotificationDocumentInt paymentDoc = TestUtils.getDocumentList(paymentDocName).getFirst();
        List<NotificationDocumentInt> listPaymentDoc = List.of(paymentDoc);
        List<TestUtils.DocumentWithContent> listDocumentWithContentForPayments = TestUtils.getDocumentWithContents(paymentDocName, listPaymentDoc );
        listPaymentDoc = TestUtils.firstFileUploadFromNotification(listDocumentWithContentForPayments, listPaymentDoc, safeStorageClientMock);

        final List<NotificationPaymentInfoInt> paymentWithF24 = TestUtils.getPaymentWithF24(listPaymentDoc.getFirst());
        NotificationRecipientInt recipient = NotificationRecipientTestBuilder.builder()
                .withTaxId("TAXID01")
                .withPhysicalAddress(paPhysicalAddress1)
                .withPayments(paymentWithF24)
                .build();

        String fileDoc = "sha256_doc00";
        List<NotificationDocumentInt> notificationDocumentList = TestUtils.getDocumentList(fileDoc);
        List<TestUtils.DocumentWithContent> listDocumentWithContent = TestUtils.getDocumentWithContents(fileDoc, notificationDocumentList);
        notificationDocumentList = TestUtils.firstFileUploadFromNotification(listDocumentWithContent, notificationDocumentList, safeStorageClientMock);


        NotificationInt notification = NotificationTestBuilder.builder()
                .withIun(TestUtils.getRandomIun() + F24_VALIDATION_FAIL)
                .withNotificationDocuments(notificationDocumentList)
                .withPaId("paId01")
                .withNotificationRecipient(recipient)
                .build();


        pnDeliveryClientMock.addNotification(notification);

        String iun = notification.getIun();

        //WHEN the workflow start
        startWorkflowHandler.startWorkflow(iun, notification.getCommunicationType());

        //THEN
        await().untilAsserted(() ->
                //Check worfklow is failed
                Assertions.assertTrue(timelineService.getTimelineElement(
                        iun,
                        TimelineEventId.REQUEST_REFUSED.buildEventId(
                                EventId.builder()
                                        .iun(iun)
                                        .build())).isPresent()
                )
        );
        ConsoleAppenderCustom.checkLogs();
    }

    @Test
    void validationPaymentInfoKO() {
        // GIVEN
        NotificationRecipientInt recipient = NotificationRecipientTestBuilder.builder()
                .withPayments(Collections.singletonList(
                        NotificationPaymentInfoInt.builder()
                                .pagoPA(PagoPaInt.builder()
                                        .creditorTaxId("creditorTaxId_"+ PnExternalRegistriesClientReactiveMock.TO_FAIL+UUID.randomUUID())
                                        .noticeCode("noticeCode_"+UUID.randomUUID())
                                        .applyCost(true)
                                        .attachment(NotificationDocumentInt.builder()
                                                .ref(NotificationDocumentInt.Ref.builder()
                                                        .key("keyPagoPaForm")
                                                        .build())
                                                .digests(NotificationDocumentInt.Digests.builder()
                                                        .sha256(Base64.getEncoder().encodeToString("keyPagoPaForm".getBytes()))
                                                        .build())
                                                .build())
                                        .build())
                                .build()
                ))
                .build();


        String fileDoc = "sha256_doc00";
        List<NotificationDocumentInt> notificationDocumentList = TestUtils.getDocumentList(fileDoc);
        List<TestUtils.DocumentWithContent> listDocumentWithContent = TestUtils.getDocumentWithContents(fileDoc, notificationDocumentList);
        notificationDocumentList = TestUtils.firstFileUploadFromNotification(listDocumentWithContent, notificationDocumentList, safeStorageClientMock);

        NotificationInt notification = NotificationTestBuilder.builder()
                .withNotificationDocuments(notificationDocumentList)
                .withNotificationRecipient(recipient)
                .withNotificationFeePolicy(NotificationFeePolicy.DELIVERY_MODE)
                .withPagoPaIntMode(PagoPaIntMode.ASYNC)
                .withPaFee(100)
                .build();


        pnDeliveryClientMock.addNotification(notification);

        String iun = notification.getIun();

        //WHEN the workflow start
        startWorkflowHandler.startWorkflow(iun, notification.getCommunicationType());

        //THEN
        await().untilAsserted(() ->
                //Check worfklow is failed
                Assertions.assertTrue(timelineService.getTimelineElement(
                        iun,
                        TimelineEventId.REQUEST_REFUSED.buildEventId(
                                EventId.builder()
                                        .iun(iun)
                                        .build())).isPresent()
                )
        );
        ConsoleAppenderCustom.checkLogs("Payment information is not valid");
    }
    
    // INIZIO TEST LOOKUP ADDRESS
    @Test
    void testNotificationAddressLookup_MonoRecipient_AcceptedWithAddressFound() {
        /*
            Scenario 1:
            Notifica monodestinatario con lookupAddress attivo che trova il physicalAddress su National Registries.
         */
        String fileDoc = "sha256_doc00";
        List<NotificationDocumentInt> notificationDocumentList = TestUtils.getDocumentList(fileDoc);
        List<TestUtils.DocumentWithContent> listDocumentWithContent = TestUtils.getDocumentWithContents(fileDoc, notificationDocumentList);
        notificationDocumentList = TestUtils.firstFileUploadFromNotification(listDocumentWithContent, notificationDocumentList, safeStorageClientMock);

        NotificationRecipientInt recipient = getNotificationRecipientInt("test_tax_id", null);

        UsedServicesInt usedServices = new UsedServicesInt().toBuilder()
                .physicalAddressLookUp(true)
                .build();

        NotificationInt notification = NotificationTestBuilder.builder()
                .withNotificationDocuments(notificationDocumentList)
                .withPaId("paId01")
                .withNotificationFeePolicy(NotificationFeePolicy.DELIVERY_MODE)
                .withNotificationRecipients(List.of(recipient))
                .withUsedServices(usedServices)
                .build();

        String iun = notification.getIun();
        Integer recIndex = NotificationUtils.getRecipientIndexFromTaxId(notification, recipient.getTaxId());
        pnDeliveryClientMock.addNotification(notification);

        //Start del workflow
        startWorkflowHandler.startWorkflow(iun, notification.getCommunicationType());

        checkRecIndexInNationalRegistryValidationCall(iun, List.of(recIndex));
        String expectedValidationCallTimelineId = TestUtils.buildTimelineEventIdNationalRegistryValidationCall(iun);

        await().untilAsserted(() ->
                Assertions.assertTrue(
                        TestUtils.checkIsPresentNationalRegistryValidationResponse(expectedValidationCallTimelineId, iun, recIndex, timelineService)
                )
        );

        await().untilAsserted(() ->
                Assertions.assertTrue(
                        TestUtils.checkIsPresentNotificationCostValidationResponse(iun, timelineService)
                )
        );

        await().untilAsserted(() ->
                Assertions.assertTrue(
                        TestUtils.checkIsPresentRequestAccepted(iun, timelineService)
                )
        );
    }

    @Test
    void testNotificationAddressLookup_MonoRecipient_RefusedForAddressNotFound() {
        /*
            Scenario 2:
            Notifica monodestinatario con lookupAddress attivo che non trova il physicalAddress su National Registries.
         */
        String fileDoc = "sha256_doc00";
        List<NotificationDocumentInt> notificationDocumentList = TestUtils.getDocumentList(fileDoc);
        List<TestUtils.DocumentWithContent> listDocumentWithContent = TestUtils.getDocumentWithContents(fileDoc, notificationDocumentList);
        notificationDocumentList = TestUtils.firstFileUploadFromNotification(listDocumentWithContent, notificationDocumentList, safeStorageClientMock);

        NotificationRecipientInt recipient = getNotificationRecipientInt(PHYS_ADDR_NOT_FOUND, null);

        UsedServicesInt usedServices = new UsedServicesInt().toBuilder()
                .physicalAddressLookUp(true)
                .build();

        NotificationInt notification = NotificationTestBuilder.builder()
                .withNotificationDocuments(notificationDocumentList)
                .withPaId("paId01")
                .withNotificationFeePolicy(NotificationFeePolicy.DELIVERY_MODE)
                .withNotificationRecipients(List.of(recipient))
                .withUsedServices(usedServices)
                .build();

        String iun = notification.getIun();
        Integer recIndex = NotificationUtils.getRecipientIndexFromTaxId(notification, recipient.getTaxId());
        pnDeliveryClientMock.addNotification(notification);

        //Start del workflow
        startWorkflowHandler.startWorkflow(iun, notification.getCommunicationType());

        checkRecIndexInNationalRegistryValidationCall(iun, List.of(recIndex));

        await().untilAsserted(() ->
                Assertions.assertTrue(
                        TestUtils.checkIsPresentNotificationRejected(iun, timelineService)
                )
        );

        String expectedValidationCallTimelineId = TestUtils.buildTimelineEventIdNationalRegistryValidationCall(iun);

        Assertions.assertFalse(
                TestUtils.checkIsPresentNationalRegistryValidationResponse(expectedValidationCallTimelineId, iun, recIndex, timelineService)
        );

        List<RefusalReason> expectedRefusalReasons = List.of(
                RefusalReason.builder()
                        .errorCode(PnDeliveryPushValidatorExceptionCodes.NotificationRefusedErrorCodeInt.ADDRESS_NOT_FOUND.getValue())
                        .recIndex(recIndex)
                        .build()
        );
        verifyNotificationRejection(iun, expectedRefusalReasons);
    }

    @Test
    void testNotificationAddressLookup_MultiRecipients_RefusedWithAddressNotFoundJustForOneRecipient() {
        /*
            Scenario 3:
            Notifica con lookupAddress attivo e con 2 destinatari da ricercare sui registri.
            National Registries non trova il physicalAddress per il primo destinatario.
            Ci aspettiamo la notifica sia rifiutata e nelle refusalReasons ci sia solo il primo destinatario.
         */
        String fileDoc = "sha256_doc00";
        List<NotificationDocumentInt> notificationDocumentList = TestUtils.getDocumentList(fileDoc);
        List<TestUtils.DocumentWithContent> listDocumentWithContent = TestUtils.getDocumentWithContents(fileDoc, notificationDocumentList);
        notificationDocumentList = TestUtils.firstFileUploadFromNotification(listDocumentWithContent, notificationDocumentList, safeStorageClientMock);

        NotificationRecipientInt recipient1 = getNotificationRecipientInt(PHYS_ADDR_NOT_FOUND, null);
        NotificationRecipientInt recipient2 = getNotificationRecipientInt("test_tax_id2", null);

        UsedServicesInt usedServices = new UsedServicesInt().toBuilder()
                .physicalAddressLookUp(true) // lookupAddress attivo per entrambi i destinatari
                .build();

        NotificationInt notification = NotificationTestBuilder.builder()
                .withNotificationDocuments(notificationDocumentList)
                .withPaId("paId01")
                .withNotificationFeePolicy(NotificationFeePolicy.DELIVERY_MODE)
                .withNotificationRecipients(List.of(recipient1, recipient2))
                .withUsedServices(usedServices)
                .build();

        String iun = notification.getIun();
        Integer recIndex1 = NotificationUtils.getRecipientIndexFromTaxId(notification, recipient1.getTaxId());
        Integer recIndex2 = NotificationUtils.getRecipientIndexFromTaxId(notification, recipient2.getTaxId());
        pnDeliveryClientMock.addNotification(notification);

        //Start del workflow
        startWorkflowHandler.startWorkflow(iun, notification.getCommunicationType());

        checkRecIndexInNationalRegistryValidationCall(iun, List.of(recIndex1, recIndex2));

        await().untilAsserted(() ->
                Assertions.assertTrue(
                        TestUtils.checkIsPresentNotificationRejected(iun, timelineService)
                )
        );

        String expectedValidationCallTimelineId = TestUtils.buildTimelineEventIdNationalRegistryValidationCall(iun);

        Assertions.assertFalse(
                TestUtils.checkIsPresentNationalRegistryValidationResponse(expectedValidationCallTimelineId, iun, recIndex1, timelineService)
        );

        Assertions.assertFalse(
                TestUtils.checkIsPresentNationalRegistryValidationResponse(expectedValidationCallTimelineId, iun, recIndex2, timelineService)
        );

        List<RefusalReason> expectedRefusalReasons = List.of(
                RefusalReason.builder()
                        .errorCode(PnDeliveryPushValidatorExceptionCodes.NotificationRefusedErrorCodeInt.ADDRESS_NOT_FOUND.getValue())
                        .recIndex(recIndex1)
                        .build()
        );
        verifyNotificationRejection(iun, expectedRefusalReasons);
    }

    @Test
    void testNotificationAddressLookup_MultiRecipients_AcceptedPhysicalAddressJustForOneRecipient() {
        /*
            Scenario 4:
            Notifica con lookupAddress attivo e con 2 destinatari.
            Il primo destinatario non ha il physicalAddress dichiarato, mentre il secondo destinatario si.
            National Registries trova il physicalAddress per il primo destinatario.
         */
        String fileDoc = "sha256_doc00";
        List<NotificationDocumentInt> notificationDocumentList = TestUtils.getDocumentList(fileDoc);
        List<TestUtils.DocumentWithContent> listDocumentWithContent = TestUtils.getDocumentWithContents(fileDoc, notificationDocumentList);
        notificationDocumentList = TestUtils.firstFileUploadFromNotification(listDocumentWithContent, notificationDocumentList, safeStorageClientMock);

        String taxId1 = "TAXID_0001";
        String taxId2 = "TAXID_0002";
        NotificationRecipientInt recipient1 = getNotificationRecipientInt(taxId1, null);
        NotificationRecipientInt recipient2 = getNotificationRecipientInt(taxId2, defaultPhysicalAddress());

        UsedServicesInt usedServices = new UsedServicesInt().toBuilder()
                .physicalAddressLookUp(true) // lookupAddress attivo per entrambi i destinatari
                .build();

        NotificationInt notification = NotificationTestBuilder.builder()
                .withNotificationDocuments(notificationDocumentList)
                .withPaId("paId01")
                .withNotificationFeePolicy(NotificationFeePolicy.DELIVERY_MODE)
                .withNotificationRecipients(List.of(recipient1, recipient2))
                .withUsedServices(usedServices)
                .build();

        String iun = notification.getIun();
        Integer recIndex1 = NotificationUtils.getRecipientIndexFromTaxId(notification, recipient1.getTaxId());
        pnDeliveryClientMock.addNotification(notification);

        //Start del workflow
        startWorkflowHandler.startWorkflow(iun, notification.getCommunicationType());

        checkRecIndexInNationalRegistryValidationCall(iun, List.of(recIndex1));

        String expectedValidationCallTimelineId = TestUtils.buildTimelineEventIdNationalRegistryValidationCall(iun);


        await().untilAsserted(() ->
                Assertions.assertTrue(
                        TestUtils.checkIsPresentNationalRegistryValidationResponse(expectedValidationCallTimelineId, iun, recIndex1, timelineService)
                )
        );

        await().untilAsserted(() ->
                Assertions.assertTrue(
                        TestUtils.checkIsPresentNotificationCostValidationResponse(iun, timelineService)
                )
        );

        await().untilAsserted(() ->
                Assertions.assertTrue(
                        TestUtils.checkIsPresentRequestAccepted(iun, timelineService)
                )
        );
    }

    @Test
    void testNotificationAddressLookup_MonoRecipient_AddressFoundWithoutLookupAddress() {
        /*
            Scenario 5:
            Notifica monodestinatario con lookupAddress attivo. La notifica dovrebbe seguire il flusso senza ricercare il physicalAddress su National Registries.
         */
        String fileDoc = "sha256_doc00";
        List<NotificationDocumentInt> notificationDocumentList = TestUtils.getDocumentList(fileDoc);
        List<TestUtils.DocumentWithContent> listDocumentWithContent = TestUtils.getDocumentWithContents(fileDoc, notificationDocumentList);
        notificationDocumentList = TestUtils.firstFileUploadFromNotification(listDocumentWithContent, notificationDocumentList, safeStorageClientMock);

        String taxId = "test_tax_id";
        NotificationRecipientInt recipient = getNotificationRecipientInt(taxId, defaultPhysicalAddress());

        UsedServicesInt usedServices = new UsedServicesInt().toBuilder()
                .physicalAddressLookUp(false)
                .build();

        NotificationInt notification = NotificationTestBuilder.builder()
                .withNotificationDocuments(notificationDocumentList)
                .withPaId("paId01")
                .withNotificationFeePolicy(NotificationFeePolicy.DELIVERY_MODE)
                .withNotificationRecipients(List.of(recipient))
                .withUsedServices(usedServices)
                .build();

        String iun = notification.getIun();
        Integer recIndex = NotificationUtils.getRecipientIndexFromTaxId(notification, recipient.getTaxId());
        pnDeliveryClientMock.addNotification(notification);

        //Start del workflow
        startWorkflowHandler.startWorkflow(iun, notification.getCommunicationType());

        await().untilAsserted(() ->
                Assertions.assertTrue(
                        TestUtils.checkIsPresentNotificationCostValidationResponse(iun, timelineService)
                )
        );

        await().untilAsserted(() ->
                Assertions.assertTrue(
                        TestUtils.checkIsPresentRequestAccepted(iun, timelineService)
                )
        );

        Assertions.assertFalse(
                TestUtils.checkIsPresentNationalRegistryValidationCall(iun, timelineService)
        );

        String expectedValidationCallTimelineId = TestUtils.buildTimelineEventIdNationalRegistryValidationCall(iun);

        Assertions.assertFalse(
                TestUtils.checkIsPresentNationalRegistryValidationResponse(expectedValidationCallTimelineId, iun, recIndex, timelineService)
        );
    }

    @Test
    void testNotificationAddressLookup_MonoRecipient_RefusedForAddressSearchFailed() {
        /*
            Scenario 6:
            Notifica monodestinatario con lookupAddress attivo che riscontra errore tecnico in fase di ricerca del physicalAddress su National Registries.
         */
        Mockito.when(pnTechnicalRefusalCostMode.getMode()).thenReturn(RefusalCostMode.RECIPIENT_BASED);
        Mockito.when(pnTechnicalRefusalCostMode.getCost()).thenReturn(100);
        String fileDoc = "sha256_doc00";
        List<NotificationDocumentInt> notificationDocumentList = TestUtils.getDocumentList(fileDoc);
        List<TestUtils.DocumentWithContent> listDocumentWithContent = TestUtils.getDocumentWithContents(fileDoc, notificationDocumentList);
        notificationDocumentList = TestUtils.firstFileUploadFromNotification(listDocumentWithContent, notificationDocumentList, safeStorageClientMock);

        NotificationRecipientInt recipient = getNotificationRecipientInt(PHYS_ADDR_ERROR, null);

        UsedServicesInt usedServices = new UsedServicesInt().toBuilder()
                .physicalAddressLookUp(true)
                .build();

        NotificationInt notification = NotificationTestBuilder.builder()
                .withNotificationDocuments(notificationDocumentList)
                .withPaId("paId01")
                .withNotificationFeePolicy(NotificationFeePolicy.DELIVERY_MODE)
                .withNotificationRecipients(List.of(recipient))
                .withUsedServices(usedServices)
                .build();

        String iun = notification.getIun();
        Integer recIndex = NotificationUtils.getRecipientIndexFromTaxId(notification, recipient.getTaxId());
        pnDeliveryClientMock.addNotification(notification);

        //Start del workflow
        startWorkflowHandler.startWorkflow(iun, notification.getCommunicationType());

        checkRecIndexInNationalRegistryValidationCall(iun, List.of(recIndex));

        await().untilAsserted(() ->
                Assertions.assertTrue(
                        TestUtils.checkIsPresentNotificationRejected(iun, timelineService)
                )
        );

        String expectedValidationCallTimelineId = TestUtils.buildTimelineEventIdNationalRegistryValidationCall(iun);

        Assertions.assertFalse(
                TestUtils.checkIsPresentNationalRegistryValidationResponse(expectedValidationCallTimelineId, iun, recIndex, timelineService)
        );

        List<RefusalReason> expectedRefusalReasons = List.of(
                RefusalReason.builder()
                        .errorCode(PnDeliveryPushValidatorExceptionCodes.NotificationRefusedErrorCodeInt.ADDRESS_SEARCH_FAILED.getValue())
                        .recIndex(recIndex)
                        .build()
        );
        verifyNotificationRejection(iun, expectedRefusalReasons);
    }

    @ParameterizedTest()
    @CsvSource(value = {
            "RECIPIENT_BASED, 100, 300", // Simula la configurazione di default
            "UNIFORM, 0, 0",
            "RECIPIENT_BASED, 50, 250",
    })
    void testNotificationAddressLookup_MultiRecipients_RefusedForVariousErrorsAndVerifyCost(RefusalCostMode refusalCostMode, Integer refusalCost, Integer expectedCost) {
        /*
            Scenari 7,8,9:
            Notifica con lookupAddress attivo e con 3 destinatari da ricercare sui registri.
            Primo destinatario: indirizzo trovato su National Registries.
            Secondo destinatario: indirizzo non trovato su National Registries.
            Terzo destinatario: errore tecnico in fase di ricerca su National Registries.
            Ci aspettiamo la notifica sia rifiutata e nelle refusalReasons ci siano solo il secondo e terzo destinatario.
         */
        Mockito.when(pnTechnicalRefusalCostMode.getMode()).thenReturn(refusalCostMode);
        Mockito.when(pnTechnicalRefusalCostMode.getCost()).thenReturn(refusalCost);
        String fileDoc = "sha256_doc00";
        List<NotificationDocumentInt> notificationDocumentList = TestUtils.getDocumentList(fileDoc);
        List<TestUtils.DocumentWithContent> listDocumentWithContent = TestUtils.getDocumentWithContents(fileDoc, notificationDocumentList);
        notificationDocumentList = TestUtils.firstFileUploadFromNotification(listDocumentWithContent, notificationDocumentList, safeStorageClientMock);

        NotificationRecipientInt recipient1 = getNotificationRecipientInt("test_tax_id", null);
        NotificationRecipientInt recipient2 = getNotificationRecipientInt(PHYS_ADDR_NOT_FOUND, null);
        NotificationRecipientInt recipient3 = getNotificationRecipientInt(PHYS_ADDR_ERROR, null);

        UsedServicesInt usedServices = new UsedServicesInt().toBuilder()
                .physicalAddressLookUp(true) // lookupAddress attivo per entrambi i destinatari
                .build();

        NotificationInt notification = NotificationTestBuilder.builder()
                .withNotificationDocuments(notificationDocumentList)
                .withPaId("paId01")
                .withNotificationFeePolicy(NotificationFeePolicy.DELIVERY_MODE)
                .withNotificationRecipients(List.of(recipient1, recipient2, recipient3))
                .withUsedServices(usedServices)
                .build();

        String iun = notification.getIun();
        Integer recIndex1 = NotificationUtils.getRecipientIndexFromTaxId(notification, recipient1.getTaxId());
        Integer recIndex2 = NotificationUtils.getRecipientIndexFromTaxId(notification, recipient2.getTaxId());
        Integer recIndex3 = NotificationUtils.getRecipientIndexFromTaxId(notification, recipient3.getTaxId());
        pnDeliveryClientMock.addNotification(notification);

        //Start del workflow
        startWorkflowHandler.startWorkflow(iun, notification.getCommunicationType());

        checkRecIndexInNationalRegistryValidationCall(iun, List.of(recIndex1, recIndex2, recIndex3));

        await().untilAsserted(() ->
                Assertions.assertTrue(
                        TestUtils.checkIsPresentNotificationRejected(iun, timelineService)
                )
        );

        String expectedValidationCallTimelineId = TestUtils.buildTimelineEventIdNationalRegistryValidationCall(iun);

        Assertions.assertFalse(
                TestUtils.checkIsPresentNationalRegistryValidationResponse(expectedValidationCallTimelineId, iun, recIndex1, timelineService)
        );

        Assertions.assertFalse(
                TestUtils.checkIsPresentNationalRegistryValidationResponse(expectedValidationCallTimelineId, iun, recIndex2, timelineService)
        );

        Assertions.assertFalse(
                TestUtils.checkIsPresentNationalRegistryValidationResponse(expectedValidationCallTimelineId, iun, recIndex3, timelineService)
        );

        List<RefusalReason> expectedRefusalReasons = List.of(
                RefusalReason.builder()
                        .errorCode(PnDeliveryPushValidatorExceptionCodes.NotificationRefusedErrorCodeInt.ADDRESS_NOT_FOUND.getValue())
                        .recIndex(recIndex2)
                        .build(),
                RefusalReason.builder()
                        .errorCode(PnDeliveryPushValidatorExceptionCodes.NotificationRefusedErrorCodeInt.ADDRESS_SEARCH_FAILED.getValue())
                        .recIndex(recIndex3)
                        .build()
        );
        verifyNotificationRejection(iun, expectedRefusalReasons, expectedCost);
    }

    
    
    
    // FINE TEST LOOKUP ADDRESS

    @Test
    void validationPaymentInfoOK() {
        String iun = TestUtils.getRandomIun();

        String fileDocPayment = "keyPagoPaForm_doc00";
        List<NotificationDocumentInt> paymentDocuments = TestUtils.getDocumentList(fileDocPayment);
        List<TestUtils.DocumentWithContent> listPaymentDocumentWithContent = TestUtils.getDocumentWithContents(fileDocPayment, paymentDocuments);
        String fileDoc = "sha256_doc00";
        List<NotificationDocumentInt> notificationDocumentList = TestUtils.getDocumentList(fileDoc);
        List<TestUtils.DocumentWithContent> listDocumentWithContent = TestUtils.getDocumentWithContents(fileDoc, notificationDocumentList);
        notificationDocumentList = TestUtils.firstFileUploadFromNotification(listDocumentWithContent,notificationDocumentList, safeStorageClientMock);
        paymentDocuments = TestUtils.firstFileUploadFromNotification(listPaymentDocumentWithContent, paymentDocuments, safeStorageClientMock);

        NotificationRecipientInt recipient = NotificationRecipientTestBuilder.builder()
                .withPhysicalAddress(
                        PhysicalAddressBuilder.builder()
                                .withAddress(EXTCHANNEL_SEND_SUCCESS + "_Via Nuova")
                                .build()
                )
                .withPayments(Collections.singletonList(
                        NotificationPaymentInfoInt.builder()
                                .pagoPA(PagoPaInt.builder()
                                        .creditorTaxId("creditorTaxId_"+ UUID.randomUUID())
                                        .noticeCode("noticeCode_"+UUID.randomUUID())
                                        .applyCost(true)
                                        .attachment(paymentDocuments.getFirst())
                                        .build())
                                .build()
                ))
                .build();


        NotificationInt notification = NotificationTestBuilder.builder()
                .withNotificationDocuments(notificationDocumentList)
                .withIun(iun)
                .withPaId("paId01")
                .withNotificationFeePolicy(NotificationFeePolicy.DELIVERY_MODE)
                .withPagoPaIntMode(PagoPaIntMode.ASYNC)
                .withPaFee(100)
                .withNotificationRecipient(recipient)
                .build();

        
        pnDeliveryClientMock.addNotification(notification);
        
        //Start del workflow
        startWorkflowHandler.startWorkflow(iun, notification.getCommunicationType());

        await().untilAsserted(() ->
                Assertions.assertTrue(
                        TestUtils.checkIsPresentNotificationCostValidationResponse(iun, timelineService)
                )
        );

        String timelineId = TimelineEventId.REQUEST_ACCEPTED.buildEventId(
                EventId.builder()
                        .iun(iun)
                        .build()
        );

        await().untilAsserted(() ->
                Assertions.assertTrue(timelineService.getTimelineElement(iun, timelineId).isPresent())
        );

        TestUtils.GeneratedLegalFactsInfo generatedLegalFactsInfo = TestUtils.GeneratedLegalFactsInfo.builder()
                .notificationReceivedLegalFactGenerated(true)
                .build();


        TestUtils.checkGeneratedLegalFacts(notification,generatedLegalFactsInfo,legalFactGenerator);
    }

    @Test
    void testNotificationValidationComplete() throws PnIdConflictException {
        String iun = TestUtils.getRandomIun();

        String fileDocPayment = "keyPagoPaForm_doc00";
        List<NotificationDocumentInt> paymentDocuments = TestUtils.getDocumentList(fileDocPayment);
        List<TestUtils.DocumentWithContent> listPaymentDocumentWithContent = TestUtils.getDocumentWithContents(fileDocPayment, paymentDocuments);
        String fileDoc = "sha256_doc00";
        List<NotificationDocumentInt> notificationDocumentList = TestUtils.getDocumentList(fileDoc);
        List<TestUtils.DocumentWithContent> listDocumentWithContent = TestUtils.getDocumentWithContents(fileDoc, notificationDocumentList);
        notificationDocumentList = TestUtils.firstFileUploadFromNotification(listDocumentWithContent,notificationDocumentList, safeStorageClientMock);
        paymentDocuments = TestUtils.firstFileUploadFromNotification(listPaymentDocumentWithContent, paymentDocuments, safeStorageClientMock);

        NotificationRecipientInt recipient = NotificationRecipientTestBuilder.builder()
                .withPhysicalAddress(
                        PhysicalAddressBuilder.builder()
                                .withAddress(EXTCHANNEL_SEND_SUCCESS + "_Via Nuova")
                                .build()
                )
                .withPayments(Collections.singletonList(
                        NotificationPaymentInfoInt.builder()
                                .pagoPA(PagoPaInt.builder()
                                        .creditorTaxId("creditorTaxId_"+ UUID.randomUUID())
                                        .noticeCode("noticeCode_"+UUID.randomUUID())
                                        .applyCost(true)
                                        .attachment(paymentDocuments.getFirst())
                                        .build())
                                .build()
                ))
                .build();


        NotificationInt notification = NotificationTestBuilder.builder()
                .withNotificationDocuments(notificationDocumentList)
                .withIun(iun)
                .withPaId("paId01")
                .withNotificationFeePolicy(NotificationFeePolicy.DELIVERY_MODE)
                .withPagoPaIntMode(PagoPaIntMode.ASYNC)
                .withPaFee(100)
                .withNotificationRecipient(recipient)
                .build();


        pnDeliveryClientMock.addNotification(notification);

        //Start del workflow
        startWorkflowHandler.startWorkflow(iun, notification.getCommunicationType());

        await().untilAsserted(() ->
                Assertions.assertTrue(
                        TestUtils.checkIsPresentNotificationCostValidationResponse(iun, timelineService)
                )
        );

        String timelineId = TimelineEventId.REQUEST_ACCEPTED.buildEventId(
                EventId.builder()
                        .iun(iun)
                        .build()
        );

        await().untilAsserted(() ->
                Assertions.assertTrue(timelineService.getTimelineElement(iun, timelineId).isPresent())
        );

        await().untilAsserted(() ->
                Assertions.assertTrue(
                        TestUtils.checkIsPresentRequestAccepted(iun, timelineService)
                )
        );
    }


    @Test
    void notificationDeliveryModeAsyncWithoutPayment() throws PnIdConflictException {
        // GIVEN
        NotificationRecipientInt recipient = NotificationRecipientTestBuilder.builder()
                .withTaxId("TAXID01")
                .withPayments(null)
                .build();

        String fileDoc = "sha256_doc00";
        List<NotificationDocumentInt> notificationDocumentList = TestUtils.getDocumentList(fileDoc);
        List<TestUtils.DocumentWithContent> listDocumentWithContent = TestUtils.getDocumentWithContents(fileDoc, notificationDocumentList);
        TestUtils.firstFileUploadFromNotification(listDocumentWithContent, notificationDocumentList, safeStorageClientMock);

        NotificationInt notification = NotificationTestBuilder.builder()
                .withPaId("paId01")
                .withNotificationFeePolicy(NotificationFeePolicy.DELIVERY_MODE)
                .withPagoPaIntMode(PagoPaIntMode.ASYNC)
                .withNotificationRecipient(recipient)
                .build();
        
        pnDeliveryClientMock.addNotification(notification);

        String iun = notification.getIun();
        Integer recIndex = NotificationUtils.getRecipientIndexFromTaxId(notification, recipient.getTaxId());

        //WHEN the workflow start
        startWorkflowHandler.startWorkflow(iun, notification.getCommunicationType());

        //THEN
        await().untilAsserted(() ->
                //Check worfklow is failed
                Assertions.assertTrue(timelineService.getTimelineElement(
                        iun,
                        TimelineEventId.REQUEST_REFUSED.buildEventId(
                                EventId.builder()
                                        .iun(iun)
                                        .recIndex(recIndex)
                                        .build())).isPresent()
                )
        );
        ConsoleAppenderCustom.checkLogs();
    }

    private NotificationRecipientInt getNotificationRecipientInt(String taxId, PhysicalAddressInt physicalAddress) {
        return NotificationRecipientInt.builder()
                .taxId(taxId)
                .denomination("denomination")
                .physicalAddress(physicalAddress)
                .internalId("internalIdTest")
                .recipientType(RecipientTypeInt.PF)
                .build();
    }

    private PhysicalAddressInt defaultPhysicalAddress() {
        return PhysicalAddressInt.builder()
                .address("Test address")
                .at("At")
                .zip("00133")
                .municipality("Test municipality")
                .province("TS")
                .build();
    }
}