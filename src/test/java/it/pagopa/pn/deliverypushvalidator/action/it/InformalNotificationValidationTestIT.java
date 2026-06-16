package it.pagopa.pn.deliverypushvalidator.action.it;

import it.pagopa.pn.commons.exceptions.PnIdConflictException;
import it.pagopa.pn.deliverypushvalidator.action.it.mockbean.AddressManagerClientMock;
import it.pagopa.pn.deliverypushvalidator.action.it.mockbean.PnDataVaultClientReactiveMock;
import it.pagopa.pn.deliverypushvalidator.action.it.mockbean.PnDeliveryClientMock;
import it.pagopa.pn.deliverypushvalidator.action.it.utils.NotificationRecipientTestBuilder;
import it.pagopa.pn.deliverypushvalidator.action.it.utils.NotificationTestBuilder;
import it.pagopa.pn.deliverypushvalidator.action.it.utils.PhysicalAddressBuilder;
import it.pagopa.pn.deliverypushvalidator.action.it.utils.TestUtils;
import it.pagopa.pn.deliverypushvalidator.action.startworkflow.StartWorkflowHandler;
import it.pagopa.pn.deliverypushvalidator.dto.address.LegalDigitalAddressInt;
import it.pagopa.pn.deliverypushvalidator.dto.ext.datavault.RecipientTypeInt;
import it.pagopa.pn.deliverypushvalidator.dto.ext.delivery.notification.CommunicationType;
import it.pagopa.pn.deliverypushvalidator.dto.ext.delivery.notification.NotificationInt;
import it.pagopa.pn.deliverypushvalidator.dto.ext.delivery.notification.NotificationRecipientInt;
import it.pagopa.pn.deliverypushvalidator.dto.ext.delivery.notification.UsedServicesInt;
import it.pagopa.pn.deliverypushvalidator.dto.timeline.EventId;
import it.pagopa.pn.deliverypushvalidator.dto.timeline.TimelineEventId;
import it.pagopa.pn.deliverypushvalidator.exception.PnDeliveryPushValidatorExceptionCodes;
import it.pagopa.pn.deliverypushvalidator.generated.openapi.msclient.datavault_reactive.model.LocalizedContent;
import it.pagopa.pn.deliverypushvalidator.generated.openapi.msclient.datavault_reactive.model.MessageResponseDto;
import it.pagopa.pn.deliverypushvalidator.logtest.ConsoleAppenderCustom;
import it.pagopa.pn.deliverypushvalidator.service.TimelineService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static it.pagopa.pn.deliverypushvalidator.action.it.mockbean.AbstractCachedSsmParameterConsumerMock.*;
import static it.pagopa.pn.deliverypushvalidator.action.it.mockbean.NationalRegistriesClientMock.PHYS_ADDR_NOT_FOUND;
import static it.pagopa.pn.deliverypushvalidator.action.it.utils.TestUtils.checkIsPresentValidateNormalizeAddressRequest;
import static org.awaitility.Awaitility.await;

class InformalNotificationValidationTestIT extends CommonNotificationValidationTestIT {

    @Autowired
    StartWorkflowHandler startWorkflowHandler;
    @Autowired
    TimelineService timelineService;
    @Autowired
    PnDeliveryClientMock pnDeliveryClientMock;
    @Autowired
    PnDataVaultClientReactiveMock pnDataVaultClientReactiveMock;

    @Test
    void campaignNotFoundTest() throws PnIdConflictException {

        NotificationRecipientInt recipient = NotificationRecipientTestBuilder.builder()
                .withTaxId("TAXID01")
                .build();

        NotificationInt notification = NotificationTestBuilder.builder()
                .withPaId(DEFAULT_CAMPAIGN_SENDER_ID)
                .withNotificationRecipient(recipient)
                .withCommunicationType(CommunicationType.INFORMAL)
                .withCampaignId("testCampaignIdNotFound")
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

        verifyNotificationRejection(
                iun,
                List.of(RefusalReason.builder()
                        .errorCode(PnDeliveryPushValidatorExceptionCodes.NotificationRefusedErrorCodeInt.CAMPAIGN_NOT_FOUND.name())
                        .build()),
                null
        );
        ConsoleAppenderCustom.checkLogs();
    }

    @Test
    void campaignClosedTest() throws PnIdConflictException {

        NotificationRecipientInt recipient = NotificationRecipientTestBuilder.builder()
                .withTaxId("TAXID01")
                .build();

        NotificationInt notification = NotificationTestBuilder.builder()
                .withPaId(DEFAULT_CAMPAIGN_SENDER_ID)
                .withNotificationRecipient(recipient)
                .withCommunicationType(CommunicationType.INFORMAL)
                .withCampaignId(CAMPAIGN_ID_CLOSED)
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

        verifyNotificationRejection(
                iun,
                List.of(RefusalReason.builder()
                        .errorCode(PnDeliveryPushValidatorExceptionCodes.NotificationRefusedErrorCodeInt.CAMPAIGN_CLOSED.name())
                        .build()),
                null
        );
        ConsoleAppenderCustom.checkLogs();
    }

    @Test
    void messageMissingInRecipientTest() {
        NotificationRecipientInt recipient = NotificationRecipientTestBuilder.builder()
                .withTaxId("TAXID01")
                .build();

        NotificationInt notification = NotificationTestBuilder.builder()
                .withPaId(DEFAULT_CAMPAIGN_SENDER_ID)
                .withNotificationRecipient(recipient)
                .withCommunicationType(CommunicationType.INFORMAL)
                .withCampaignId(CAMPAIGN_ID_DIGITAL_WORKFLOW)
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

        verifyNotificationRejection(
                iun,
                List.of(RefusalReason.builder()
                        .errorCode(PnDeliveryPushValidatorExceptionCodes.NotificationRefusedErrorCodeInt.MESSAGE_NOT_FOUND.name())
                        .build()),
                null
        );
        ConsoleAppenderCustom.checkLogs();
    }

    @Test
    void messageNotFoundTest() {
        NotificationRecipientInt recipient = NotificationRecipientTestBuilder.builder()
                .withTaxId("TAXID01")
                .withMessageId(UUID.randomUUID().toString())
                .build();

        NotificationInt notification = NotificationTestBuilder.builder()
                .withPaId(DEFAULT_CAMPAIGN_SENDER_ID)
                .withNotificationRecipient(recipient)
                .withCommunicationType(CommunicationType.INFORMAL)
                .withCampaignId(CAMPAIGN_ID_DIGITAL_WORKFLOW)
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

        verifyNotificationRejection(
                iun,
                List.of(RefusalReason.builder()
                        .errorCode(PnDeliveryPushValidatorExceptionCodes.NotificationRefusedErrorCodeInt.MESSAGE_NOT_FOUND.name())
                        .build()),
                null
        );
        ConsoleAppenderCustom.checkLogs();
    }

    private static Stream<Arguments> provideMessageLanguageInconsistenciesScenarios() {
        return Stream.of(
                Arguments.of(null, List.of("DE")),
                Arguments.of(LocalizedContent.builder().language(LocalizedContent.LanguageEnum.DE).build(), List.of("FR"))
        );
    }

    @ParameterizedTest
    @MethodSource("provideMessageLanguageInconsistenciesScenarios")
    void messageLanguagesInconsistenciesTest(LocalizedContent secondaryContent, List<String> additionalLanguages) {
        UUID messageId = UUID.randomUUID();
        NotificationRecipientInt recipient = NotificationRecipientTestBuilder.builder()
                .withTaxId("TAXID01")
                .withMessageId(messageId.toString())
                .build();
        pnDataVaultClientReactiveMock.insertMessage(MessageResponseDto.builder()
                .messageId(messageId)
                .senderId(DEFAULT_CAMPAIGN_SENDER_ID)
                .secondaryContent(secondaryContent)
                .build());

        NotificationInt notification = NotificationTestBuilder.builder()
                .withPaId(DEFAULT_CAMPAIGN_SENDER_ID)
                .withNotificationRecipient(recipient)
                .withCommunicationType(CommunicationType.INFORMAL)
                .withCampaignId(CAMPAIGN_ID_DIGITAL_WORKFLOW)
                .withAdditionalLanguages(additionalLanguages)
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

        verifyNotificationRejection(
                iun,
                List.of(RefusalReason.builder()
                        .errorCode(PnDeliveryPushValidatorExceptionCodes.NotificationRefusedErrorCodeInt.MESSAGE_LANGUAGE_MISMATCH.name())
                        .build()),
                null
        );
        ConsoleAppenderCustom.checkLogs();
    }

    @Test
    void digitalAddressMissingForDigitalWorkflowTest() {
        UUID messageId = UUID.randomUUID();
        NotificationRecipientInt recipient = NotificationRecipientTestBuilder.builder()
                .withTaxId("TAXID01")
                .withMessageId(messageId.toString())
                .withRecipientType(RecipientTypeInt.PG)
                .build();

        pnDataVaultClientReactiveMock.insertMessage(MessageResponseDto.builder()
                .messageId(messageId)
                .senderId(DEFAULT_CAMPAIGN_SENDER_ID)
                .build());

        NotificationInt notification = NotificationTestBuilder.builder()
                .withPaId(DEFAULT_CAMPAIGN_SENDER_ID)
                .withNotificationRecipient(recipient)
                .withCommunicationType(CommunicationType.INFORMAL)
                .withCampaignId(CAMPAIGN_ID_DIGITAL_WORKFLOW)
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

        verifyNotificationRejection(
                iun,
                List.of(RefusalReason.builder()
                        .errorCode(PnDeliveryPushValidatorExceptionCodes.NotificationRefusedErrorCodeInt.DIGITAL_ADDRESS_MISSING.name())
                        .build()),
                null
        );
        ConsoleAppenderCustom.checkLogs();
    }

    @Test
    void lookupAddressEnabledAndAddressNotFoundTest() {
        UUID messageId = UUID.randomUUID();

        NotificationRecipientInt recipient = NotificationRecipientTestBuilder.builder()
                .withTaxId(PHYS_ADDR_NOT_FOUND) // Determina l'esito della ricerca su NR
                .withMessageId(messageId.toString())
                .build();
        recipient.setPhysicalAddress(null); // Per essere sicuri che non ci sia l'indirizzo nella notifica


        pnDataVaultClientReactiveMock.insertMessage(MessageResponseDto.builder()
                .messageId(messageId)
                .senderId(DEFAULT_CAMPAIGN_SENDER_ID)
                .build());

        UsedServicesInt usedServices = new UsedServicesInt().toBuilder()
                .physicalAddressLookUp(true)
                .build();

        NotificationInt notification = NotificationTestBuilder.builder()
                .withPaId(DEFAULT_CAMPAIGN_SENDER_ID)
                .withNotificationRecipient(recipient)
                .withCommunicationType(CommunicationType.INFORMAL)
                .withCampaignId(CAMPAIGN_ID_ANALOG_WORKFLOW)
                .withUsedServices(usedServices)
                .build();

        pnDeliveryClientMock.addNotification(notification);
        String iun = notification.getIun();

        //WHEN the workflow start
        startWorkflowHandler.startWorkflow(iun, notification.getCommunicationType());

        //THEN
        checkRecIndexInNationalRegistryValidationCall(iun, List.of(0));

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

        String expectedValidationCallTimelineId = TestUtils.buildTimelineEventIdNationalRegistryValidationCall(iun);

        Assertions.assertFalse(
                TestUtils.checkIsPresentNationalRegistryValidationResponse(expectedValidationCallTimelineId, iun, 0, timelineService)
        );

        verifyNotificationRejection(
                iun,
                List.of(RefusalReason.builder()
                        .errorCode(PnDeliveryPushValidatorExceptionCodes.NotificationRefusedErrorCodeInt.ADDRESS_NOT_FOUND.name())
                        .recIndex(0)
                        .build()),
                null
        );
        ConsoleAppenderCustom.checkLogs();
    }

    @Test
    void physicalAddressNormalizationFailedTest() {
        UUID messageId = UUID.randomUUID();
        NotificationRecipientInt recipient = NotificationRecipientTestBuilder.builder()
                .withTaxId("TAXID01")
                .withPhysicalAddress(PhysicalAddressBuilder.builder()
                        .withAddress("Via Nuova_" + AddressManagerClientMock.ADDRESS_MANAGER_NOT_VALID_ADDRESS)
                        .build())
                .withMessageId(messageId.toString())
                .build();

        pnDataVaultClientReactiveMock.insertMessage(MessageResponseDto.builder()
                .messageId(messageId)
                .senderId(DEFAULT_CAMPAIGN_SENDER_ID)
                .build());

        NotificationInt notification = NotificationTestBuilder.builder()
                .withPaId(DEFAULT_CAMPAIGN_SENDER_ID)
                .withNotificationRecipient(recipient)
                .withCommunicationType(CommunicationType.INFORMAL)
                .withCampaignId(CAMPAIGN_ID_ANALOG_WORKFLOW)
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

        // Inizio controlli VAS non usato
        Assertions.assertFalse(
                TestUtils.checkIsPresentNationalRegistryValidationCall(iun, timelineService)
        );

        String expectedValidationCallTimelineId = TestUtils.buildTimelineEventIdNationalRegistryValidationCall(iun);

        Assertions.assertFalse(
                TestUtils.checkIsPresentNationalRegistryValidationResponse(expectedValidationCallTimelineId, iun, 0, timelineService)
        );
        // Fine controlli VAS non usato

        verifyNotificationRejection(
                iun,
                List.of(RefusalReason.builder()
                        .errorCode(PnDeliveryPushValidatorExceptionCodes.NotificationRefusedErrorCodeInt.NOT_VALID_ADDRESS.name())
                        .build()),
                null
        );
        ConsoleAppenderCustom.checkLogs();
    }

    @Test
    void notificationValidationComplete_digitalWorkflow() {
        UUID messageId = UUID.randomUUID();
        NotificationRecipientInt recipient = NotificationRecipientTestBuilder.builder()
                .withTaxId("TAXID01")
                .withMessageId(messageId.toString())
                .withDigitalDomicile(LegalDigitalAddressInt.builder()
                        .address("test@mail.it")
                        .type(LegalDigitalAddressInt.LEGAL_DIGITAL_ADDRESS_TYPE.PEC)
                        .build())
                .withRecipientType(RecipientTypeInt.PG)
                .build();

        pnDataVaultClientReactiveMock.insertMessage(MessageResponseDto.builder()
                .messageId(messageId)
                .senderId(DEFAULT_CAMPAIGN_SENDER_ID)
                .build());

        NotificationInt notification = NotificationTestBuilder.builder()
                .withPaId(DEFAULT_CAMPAIGN_SENDER_ID)
                .withNotificationRecipient(recipient)
                .withCommunicationType(CommunicationType.INFORMAL)
                .withCampaignId(CAMPAIGN_ID_DIGITAL_WORKFLOW)
                .build();

        pnDeliveryClientMock.addNotification(notification);
        String iun = notification.getIun();

        //WHEN the workflow start
        startWorkflowHandler.startWorkflow(iun, notification.getCommunicationType());

        //THEN
        await().untilAsserted(() ->
                Assertions.assertTrue(
                        TestUtils.checkIsPresentRequestAccepted(iun, timelineService)
                )
        );

        ConsoleAppenderCustom.checkLogs();
    }

    @Test
    void notificationValidationComplete_analogWorkflow_withoutLookupAddress() {
        UUID messageId = UUID.randomUUID();
        NotificationRecipientInt recipient = NotificationRecipientTestBuilder.builder()
                .withTaxId("TAXID01")
                .withPhysicalAddress(PhysicalAddressBuilder.builder()
                        .withAddress("Via Nuova_Ok")
                        .build())
                .withMessageId(messageId.toString())
                .build();

        pnDataVaultClientReactiveMock.insertMessage(MessageResponseDto.builder()
                .messageId(messageId)
                .senderId(DEFAULT_CAMPAIGN_SENDER_ID)
                .build());

        NotificationInt notification = NotificationTestBuilder.builder()
                .withPaId(DEFAULT_CAMPAIGN_SENDER_ID)
                .withNotificationRecipient(recipient)
                .withCommunicationType(CommunicationType.INFORMAL)
                .withCampaignId(CAMPAIGN_ID_ANALOG_WORKFLOW)
                .build();

        pnDeliveryClientMock.addNotification(notification);
        String iun = notification.getIun();

        //WHEN the workflow start
        startWorkflowHandler.startWorkflow(iun, notification.getCommunicationType());

        //THEN
        await().untilAsserted(() ->
                Assertions.assertTrue(
                        TestUtils.checkIsPresentRequestAccepted(iun, timelineService)
                )
        );

        //Verifichiamo che non sia stata richiesta la ricerca dell'indirizzo su NR (VAS)
        Assertions.assertFalse(TestUtils.checkIsPresentNationalRegistryValidationCall(iun, timelineService));

        // Verifichiamo sia stata richiesta la normalizzazione dell'indirizzo
        Assertions.assertTrue(checkIsPresentValidateNormalizeAddressRequest(iun, timelineService));

        ConsoleAppenderCustom.checkLogs();
    }

    @Test
    void notificationValidationComplete_analogWorkflow_withLookupAddress() {
        UUID messageId = UUID.randomUUID();
        NotificationRecipientInt recipient = NotificationRecipientTestBuilder.builder()
                .withTaxId("TAXID01")
                .withMessageId(messageId.toString())
                .build();
        recipient.setPhysicalAddress(null); // Per essere sicuri che non ci sia l'indirizzo nella notifica

        pnDataVaultClientReactiveMock.insertMessage(MessageResponseDto.builder()
                .messageId(messageId)
                .senderId(DEFAULT_CAMPAIGN_SENDER_ID)
                .build());

        UsedServicesInt usedServices = new UsedServicesInt().toBuilder()
                .physicalAddressLookUp(true)
                .build();

        NotificationInt notification = NotificationTestBuilder.builder()
                .withPaId(DEFAULT_CAMPAIGN_SENDER_ID)
                .withNotificationRecipient(recipient)
                .withCommunicationType(CommunicationType.INFORMAL)
                .withCampaignId(CAMPAIGN_ID_ANALOG_WORKFLOW)
                .withUsedServices(usedServices)
                .build();

        pnDeliveryClientMock.addNotification(notification);
        String iun = notification.getIun();

        //WHEN the workflow start
        startWorkflowHandler.startWorkflow(iun, notification.getCommunicationType());

        //THEN
        await().untilAsserted(() ->
                Assertions.assertTrue(
                        TestUtils.checkIsPresentRequestAccepted(iun, timelineService)
                )
        );

        //Verifichiamo che sia stata richiesta la ricerca dell'indirizzo su NR (VAS)
        Assertions.assertTrue(TestUtils.checkIsPresentNationalRegistryValidationCall(iun, timelineService));

        checkRecIndexInNationalRegistryValidationCall(iun, List.of(0));

        String expectedValidationCallTimelineId = TestUtils.buildTimelineEventIdNationalRegistryValidationCall(iun);


        await().untilAsserted(() ->
                Assertions.assertTrue(
                        TestUtils.checkIsPresentNationalRegistryValidationResponse(expectedValidationCallTimelineId, iun, 0, timelineService)
                )
        );

        // Verifichiamo sia stata richiesta la normalizzazione dell'indirizzo
        Assertions.assertTrue(checkIsPresentValidateNormalizeAddressRequest(iun, timelineService));

        ConsoleAppenderCustom.checkLogs();
    }

}