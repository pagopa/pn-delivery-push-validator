package it.pagopa.pn.deliverypushvalidator.action.it;

import it.pagopa.pn.commons.exceptions.PnIdConflictException;
import it.pagopa.pn.deliverypushvalidator.action.it.mockbean.AddressManagerClientMock;
import it.pagopa.pn.deliverypushvalidator.action.it.mockbean.PnDeliveryClientMock;
import it.pagopa.pn.deliverypushvalidator.action.it.mockbean.SafeStorageClientMock;
import it.pagopa.pn.deliverypushvalidator.action.it.utils.NotificationRecipientTestBuilder;
import it.pagopa.pn.deliverypushvalidator.action.it.utils.NotificationTestBuilder;
import it.pagopa.pn.deliverypushvalidator.action.it.utils.PhysicalAddressBuilder;
import it.pagopa.pn.deliverypushvalidator.action.it.utils.TestUtils;
import it.pagopa.pn.deliverypushvalidator.action.startworkflow.StartWorkflowHandler;
import it.pagopa.pn.deliverypushvalidator.action.utils.NotificationUtils;
import it.pagopa.pn.deliverypushvalidator.dto.address.LegalDigitalAddressInt;
import it.pagopa.pn.deliverypushvalidator.dto.ext.delivery.notification.CommunicationType;
import it.pagopa.pn.deliverypushvalidator.dto.ext.delivery.notification.NotificationDocumentInt;
import it.pagopa.pn.deliverypushvalidator.dto.ext.delivery.notification.NotificationInt;
import it.pagopa.pn.deliverypushvalidator.dto.ext.delivery.notification.NotificationRecipientInt;
import it.pagopa.pn.deliverypushvalidator.dto.timeline.EventId;
import it.pagopa.pn.deliverypushvalidator.dto.timeline.NotificationRefusedErrorInt;
import it.pagopa.pn.deliverypushvalidator.dto.timeline.TimelineElementInternal;
import it.pagopa.pn.deliverypushvalidator.dto.timeline.TimelineEventId;
import it.pagopa.pn.deliverypushvalidator.dto.timeline.details.PublicRegistryValidationCallDetailsInt;
import it.pagopa.pn.deliverypushvalidator.dto.timeline.details.RequestRefusedDetailsInt;
import it.pagopa.pn.deliverypushvalidator.logtest.ConsoleAppenderCustom;
import it.pagopa.pn.deliverypushvalidator.service.TimelineService;
import lombok.Builder;
import lombok.Getter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.awaitility.Awaitility.await;

class CommonNotificationValidationTestIT extends CommonTestConfiguration{


    @Autowired
    StartWorkflowHandler startWorkflowHandler;
    @Autowired
    TimelineService timelineService;
    @Autowired
    SafeStorageClientMock safeStorageClientMock;
    @Autowired
    PnDeliveryClientMock pnDeliveryClientMock;

    // ------ INIZIO VALIDAZIONI ATTACHMENTS E DOCUMENTS ------
    @ParameterizedTest
    @CsvSource({
            "INFORMAL",
            "LEGAL"
    })
    void differentShaRefusedTest(CommunicationType communicationType) throws PnIdConflictException {

        //Special address is present and all sending attempts fail
        LegalDigitalAddressInt digitalDomicile = LegalDigitalAddressInt.builder()
                .address("digitalDomicile@test.it")
                .type(LegalDigitalAddressInt.LEGAL_DIGITAL_ADDRESS_TYPE.PEC)
                .build();

        NotificationRecipientInt recipient = NotificationRecipientTestBuilder.builder()
                .withTaxId("TAXID01")
                .withDigitalDomicile(digitalDomicile)
                .build();

        NotificationInt notification = NotificationTestBuilder.builder()
                .withPaId("paId01")
                .withNotificationRecipient(recipient)
                .withCommunicationType(communicationType)
                .build();

        byte[] differentFileSha = "error".getBytes();
        notification = TestUtils.firstFileUploadFromNotificationError(notification, safeStorageClientMock, differentFileSha);
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

    @ParameterizedTest
    @CsvSource({
            "INFORMAL",
            "LEGAL"
    })
    void fileTooBig(CommunicationType communicationType) {
        // GIVEN

        NotificationRecipientInt recipient = NotificationRecipientTestBuilder.builder()
                .withTaxId("TAXID01")
                .withPhysicalAddress(PhysicalAddressBuilder.builder()
                        .withAddress("Via Nuova_" + AddressManagerClientMock.ADDRESS_MANAGER_TO_NORMALIZE)
                        .build())
                .build();

        String fileDoc = "sha256_doc00";
        List<NotificationDocumentInt> notificationDocumentList = TestUtils.getDocumentList(fileDoc);
        List<TestUtils.DocumentWithContent> listDocumentWithContent = TestUtils.getDocumentWithContents(fileDoc, notificationDocumentList);
        notificationDocumentList = TestUtils.firstFileUploadFromNotificationTooBig(listDocumentWithContent, notificationDocumentList, safeStorageClientMock);

        NotificationInt notification = NotificationTestBuilder.builder()
                .withNotificationDocuments(notificationDocumentList)
                .withPaId("paId01")
                .withNotificationRecipient(recipient)
                .withCommunicationType(communicationType)
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

    @ParameterizedTest
    @CsvSource({
            "INFORMAL",
            "LEGAL"
    })
    void fileNotValidPDF(CommunicationType communicationType) {
        // GIVEN

        NotificationRecipientInt recipient = NotificationRecipientTestBuilder.builder()
                .withTaxId("TAXID01")
                .withPhysicalAddress(PhysicalAddressBuilder.builder()
                        .withAddress("Via Nuova_" + AddressManagerClientMock.ADDRESS_MANAGER_TO_NORMALIZE)
                        .build())
                .build();

        String fileDoc = "sha256_doc00";
        List<NotificationDocumentInt> notificationDocumentList = TestUtils.getDocumentList(fileDoc);
        List<TestUtils.DocumentWithContent> listDocumentWithContent = TestUtils.getDocumentWithContents(fileDoc, notificationDocumentList);
        notificationDocumentList = TestUtils.firstFileUploadFromNotificationNotAPDF(listDocumentWithContent, notificationDocumentList, safeStorageClientMock);

        NotificationInt notification = NotificationTestBuilder.builder()
                .withNotificationDocuments(notificationDocumentList)
                .withPaId("paId01")
                .withNotificationRecipient(recipient)
                .withCommunicationType(communicationType)
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
    // ------ FINE VALIDAZIONI ATTACHMENTS E DOCUMENTS ------

    @Builder
    @Getter
    protected static class RefusalReason {
        private String errorCode;
        private Integer recIndex;
    }

    protected void verifyNotificationRejection(String iun, List<RefusalReason> expectedRefusalReasons) {
        verifyNotificationRejection(iun, expectedRefusalReasons, null);
    }

    protected void verifyNotificationRejection(String iun, List<RefusalReason> expectedRefusalReasons, Integer expectedCost) {
        TimelineElementInternal timelineElementInternal = TestUtils.getNotificationRejected(iun, timelineService).get();
        RequestRefusedDetailsInt details = (RequestRefusedDetailsInt) timelineElementInternal.getDetails();
        Assertions.assertEquals(expectedRefusalReasons.size(), details.getRefusalReasons().size());
        List<NotificationRefusedErrorInt> actualRefusalReasons = details.getRefusalReasons();
        for(RefusalReason expectedRefusalReason : expectedRefusalReasons) {
            Assertions.assertTrue(checkRefusalReason(expectedRefusalReason, actualRefusalReasons),
                    "Refusal reason not found in the list of refusal reasons for IUN: " + iun);
        }
        if(expectedCost != null) {
            Assertions.assertEquals(expectedCost, details.getNotificationCost());
        }
    }

    private boolean checkRefusalReason(RefusalReason expectedRefusalReason, List<NotificationRefusedErrorInt> actualRefusalReasons) {
        return actualRefusalReasons.stream()
                .anyMatch(r -> r.getErrorCode().equals(expectedRefusalReason.getErrorCode()) &&
                        ((r.getRecIndex() == null && expectedRefusalReason.getRecIndex() == null) ||
                                r.getRecIndex() != null && expectedRefusalReason.getRecIndex() != null && r.getRecIndex().equals(expectedRefusalReason.getRecIndex()))
                );
    }

    protected void checkRecIndexInNationalRegistryValidationCall(String iun, List<Integer> recIndexesInput) {
        // Attendo sia presente l'elemento di timeline di ricerca in fase di validazione sui registri nazionali
        await().untilAsserted(() ->
                Assertions.assertTrue(
                        TestUtils.checkIsPresentNationalRegistryValidationCall(iun, timelineService)
                )
        );
        Optional<TimelineElementInternal> timelineElementOpt = timelineService.getTimelineElement(
                iun,
                TestUtils.buildTimelineEventIdNationalRegistryValidationCall(iun)
        );

        if (timelineElementOpt.isPresent()) {
            TimelineElementInternal timelineElement = timelineElementOpt.get();
            PublicRegistryValidationCallDetailsInt details =
                    (PublicRegistryValidationCallDetailsInt) timelineElement.getDetails();
            List<Integer> recIndexesDetails = details.getRecIndexes();
            Assertions.assertEquals(recIndexesDetails.size(), details.getRecIndexes().size());
            Assertions.assertTrue(compareRecIndexesLists(recIndexesDetails, recIndexesInput));
        } else {
            Assertions.fail("Timeline element with category NATIONAL_REGISTRY_VALIDATION_CALL not found for IUN: " + iun);
        }
    }

    public static boolean compareRecIndexesLists(List<Integer> recIndexesDetails, List<Integer> recIndexesInput) {
        if (recIndexesDetails == null && recIndexesInput == null) {
            return true;
        }

        if (recIndexesDetails == null || recIndexesInput == null) {
            return false;
        }

        Set<Integer> integerList1 = new HashSet<>(recIndexesDetails);
        Set<Integer> integerList2 = new HashSet<>(recIndexesInput);

        return integerList1.equals(integerList2);
    }
}