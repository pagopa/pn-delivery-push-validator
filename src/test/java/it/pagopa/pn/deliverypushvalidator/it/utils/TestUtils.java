package it.pagopa.pn.deliverypushvalidator.it.utils;

import it.pagopa.pn.commons.utils.DateFormatUtils;
import it.pagopa.pn.deliverypushvalidator.action.utils.EndWorkflowStatus;
import it.pagopa.pn.deliverypushvalidator.config.PnDeliveryPushValidatorConfigs;
import it.pagopa.pn.deliverypushvalidator.dto.address.CourtesyDigitalAddressInt;
import it.pagopa.pn.deliverypushvalidator.dto.address.DigitalAddressSourceInt;
import it.pagopa.pn.deliverypushvalidator.dto.address.LegalDigitalAddressInt;
import it.pagopa.pn.deliverypushvalidator.dto.address.PhysicalAddressInt;
import it.pagopa.pn.deliverypushvalidator.dto.cost.PaymentsInfoForRecipientInt;
import it.pagopa.pn.deliverypushvalidator.dto.delivery.notification.*;
import it.pagopa.pn.deliverypushvalidator.dto.delivery.notification.status.NotificationStatusHistoryElementInt;
import it.pagopa.pn.deliverypushvalidator.dto.delivery.notification.status.NotificationStatusInt;
import it.pagopa.pn.deliverypushvalidator.dto.ext.safestorage.FileCreationWithContentRequest;
import it.pagopa.pn.deliverypushvalidator.dto.externalchannel.ResponseStatusInt;
import it.pagopa.pn.deliverypushvalidator.dto.legalfacts.LegalFactCategoryInt;
import it.pagopa.pn.deliverypushvalidator.dto.legalfacts.LegalFactsIdInt;
import it.pagopa.pn.deliverypushvalidator.dto.mandate.DelegateInfoInt;
import it.pagopa.pn.deliverypushvalidator.dto.timeline.EventId;
import it.pagopa.pn.deliverypushvalidator.dto.timeline.TimelineElementInternal;
import it.pagopa.pn.deliverypushvalidator.dto.timeline.TimelineEventId;
import it.pagopa.pn.deliverypushvalidator.dto.timeline.details.*;
import it.pagopa.pn.deliverypushvalidator.generated.openapi.msclient.pnsafestorage.model.FileCreationResponse;
import it.pagopa.pn.deliverypushvalidator.it.mockbean.*;
import it.pagopa.pn.deliverypushvalidator.legalfact.LegalFactGenerator;
import it.pagopa.pn.deliverypushvalidator.logtest.ConsoleAppenderCustom;
import it.pagopa.pn.deliverypushvalidator.middleware.queue.producer.abstractions.actionspool.ActionType;
import it.pagopa.pn.deliverypushvalidator.service.SchedulerService;
import it.pagopa.pn.deliverypushvalidator.service.TimelineService;
import it.pagopa.pn.deliverypushvalidator.utils.StatusUtils;
import it.pagopa.pn.deliverypushvalidator.utils.ThreadPool;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.io.IOException;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static org.mockito.ArgumentMatchers.eq;

@Slf4j
public class TestUtils {
    public static final String PN_NOTIFICATION_ATTACHMENT = "PN_NOTIFICATION_ATTACHMENT";
    public static final String TOO_BIG = "TOO_BIG";
    public static final String NOT_A_PDF = "NOT_A_PDF";

//todo: va tolto?

//    public static void checkSendCourtesyAddresses(String iun, Integer recIndex, List<CourtesyDigitalAddressInt> courtesyAddresses, TimelineService timelineService, ExternalChannelMock externalChannelMock) {
//
//        checkSendCourtesyAddressFromTimeline(iun, recIndex, courtesyAddresses, timelineService);
//        //Viene verificato l'effettivo invio del messaggio di cortesia verso external channel
//        Mockito.verify(externalChannelMock, Mockito.times(courtesyAddresses.size())).sendCourtesyNotification(
//                Mockito.any(NotificationInt.class),
//                Mockito.any(NotificationRecipientInt.class),
//                Mockito.any(CourtesyDigitalAddressInt.class),
//                Mockito.any(String.class),
//                Mockito.anyString(),
//                Mockito.anyString()
//        );
//    }

    public static void checkSendCourtesyAddressFromTimeline(String iun, Integer recIndex, List<CourtesyDigitalAddressInt> courtesyAddresses, TimelineService timelineService) {
        for (CourtesyDigitalAddressInt digitalAddress : courtesyAddresses) {
            String eventId = TimelineEventId.SEND_COURTESY_MESSAGE.buildEventId(
                    EventId.builder()
                            .iun(iun)
                            .recIndex(recIndex)
                            .courtesyAddressType(digitalAddress.getType())
                            .build());
            Optional<SendCourtesyMessageDetailsInt> sendCourtesyMessageDetailsOpt = timelineService.getTimelineElementDetails(iun, eventId, SendCourtesyMessageDetailsInt.class);

            Assertions.assertTrue(sendCourtesyMessageDetailsOpt.isPresent());
            SendCourtesyMessageDetailsInt sendCourtesyMessageDetails = sendCourtesyMessageDetailsOpt.get();
            Assertions.assertEquals(digitalAddress.getAddress(), sendCourtesyMessageDetails.getDigitalAddress().getAddress());
            Assertions.assertEquals(digitalAddress.getType(), sendCourtesyMessageDetails.getDigitalAddress().getType());
        }
    }

    public static void checkGetAddress(String iun, Integer recIndex, Boolean isAvailable, DigitalAddressSourceInt source, int sentAttempt, TimelineService timelineService) {
        String correlationId = TimelineEventId.GET_ADDRESS.buildEventId(
                EventId.builder()
                        .iun(iun)
                        .recIndex(recIndex)
                        .source(source)
                        .sentAttemptMade(sentAttempt)
                        .build());

        Optional<GetAddressInfoDetailsInt> getAddressInfoOpt = timelineService.getTimelineElementDetails(iun, correlationId, GetAddressInfoDetailsInt.class);
        if(isAvailable){
            Assertions.assertTrue(getAddressInfoOpt.isPresent());
            Assertions.assertEquals(true, getAddressInfoOpt.get().getIsAvailable());
        } else {
            Assertions.assertTrue(getAddressInfoOpt.isEmpty() || !getAddressInfoOpt.get().getIsAvailable());
        }
    }

    public static void checkRefinement(String iun, Integer recIndex, TimelineService timelineService) {
        Assertions.assertTrue(timelineService.getTimelineElement(
                iun,
                TimelineEventId.REFINEMENT.buildEventId(
                        EventId.builder()
                                .iun(iun)
                                .recIndex(recIndex)
                                .build())).isPresent());
    }

    public static void checkExternalChannelPecSend(String iunExpected, String addressExpected, String iunValue, String addressValue) {
        Assertions.assertEquals(iunExpected, iunValue);
        //Assertions.assertEquals(taxIdExpected, taxIdValue);
        Assertions.assertEquals(addressExpected, addressValue);
    }

    public static void checkExternalChannelPecSendFromTimeline(String iun, int recIndex, int sendAttemptMade, LegalDigitalAddressInt digitalAddress,
                                                               DigitalAddressSourceInt addressSource, TimelineService timelineService) {

        Boolean isFirstRetry = isPossibileCaseToRepeat(addressSource, sendAttemptMade);

        String timelineEventId = TimelineEventId.SEND_DIGITAL_DOMICILE.buildEventId(
                EventId.builder()
                        .iun(iun)
                        .recIndex(recIndex)
                        .sentAttemptMade(sendAttemptMade)
                        .source(addressSource)
                        .isFirstSendRetry(isFirstRetry)
                        .build()
        );

        Optional<TimelineElementInternal> timelineElementInternal = timelineService.getTimelineElement(iun, timelineEventId);

        Assertions.assertTrue(timelineElementInternal.isPresent());
        TimelineElementInternal timelineElement = timelineElementInternal.get();
        Assertions.assertEquals(digitalAddress.getAddress(), ((SendDigitalDetailsInt) timelineElement.getDetails()).getDigitalAddress().getAddress());
    }

    private static boolean isPossibileCaseToRepeat(DigitalAddressSourceInt digitalAddressSource, int sentAttemptMade) {
        return (DigitalAddressSourceInt.PLATFORM.equals(digitalAddressSource) ||
                DigitalAddressSourceInt.GENERAL.equals(digitalAddressSource))
                &&
                sentAttemptMade == 1;
    }

    public static void checkIsPresentAcceptanceInTimeline(String iun, int recIndex, int sendAttemptMade, LegalDigitalAddressInt digitalAddress,
                                                          DigitalAddressSourceInt addressSource, TimelineService timelineService) {

        Boolean isFirstRetry = isPossibileCaseToRepeat(addressSource, sendAttemptMade);
        checkAcceptance(iun, recIndex, sendAttemptMade, digitalAddress, addressSource, timelineService, isFirstRetry);
    }

    public static void checkAcceptance(String iun, int recIndex, int sendAttemptMade, LegalDigitalAddressInt digitalAddress, DigitalAddressSourceInt addressSource, TimelineService timelineService, Boolean isFirstRetry) {
        String timelineEventId = TimelineEventId.SEND_DIGITAL_PROGRESS.buildEventId(
                EventId.builder()
                        .iun(iun)
                        .recIndex(recIndex)
                        .isFirstSendRetry(isFirstRetry)
                        .sentAttemptMade(sendAttemptMade)
                        .source(addressSource)
                        .progressIndex(1)
                        .build()
        );

        Optional<TimelineElementInternal> timelineElementInternal = timelineService.getTimelineElement(iun, timelineEventId);

        Assertions.assertTrue(timelineElementInternal.isPresent());
        TimelineElementInternal timelineElement = timelineElementInternal.get();
        Assertions.assertNotNull(timelineElement.getLegalFactsIds().get(0));
        Assertions.assertNotNull(timelineElement.getTimestamp());

        SendDigitalProgressDetailsInt details = (SendDigitalProgressDetailsInt) timelineElement.getDetails();
        Assertions.assertEquals(digitalAddress.getAddress(), details.getDigitalAddress().getAddress());
    }

    public static void checkIsPresentDigitalFeedbackInTimeline(String iun, int recIndex, int sendAttemptMade, LegalDigitalAddressInt digitalAddress,
                                                               DigitalAddressSourceInt addressSource, TimelineService timelineService, ResponseStatusInt status) {

        Boolean isFirstRetry = isPossibileCaseToRepeat(addressSource, sendAttemptMade);

        checkDigitalFeedback(iun, recIndex, sendAttemptMade, digitalAddress, addressSource, timelineService, status, isFirstRetry);
    }

    public static void checkDigitalFeedback(String iun, int recIndex, int sendAttemptMade, LegalDigitalAddressInt digitalAddress, DigitalAddressSourceInt addressSource, TimelineService timelineService, ResponseStatusInt status, Boolean isFirstRetry) {
        String timelineEventId = TimelineEventId.SEND_DIGITAL_FEEDBACK.buildEventId(
                EventId.builder()
                        .iun(iun)
                        .recIndex(recIndex)
                        .sentAttemptMade(sendAttemptMade)
                        .source(addressSource)
                        .isFirstSendRetry(isFirstRetry)
                        .build()
        );

        Optional<TimelineElementInternal> timelineElementInternal = timelineService.getTimelineElement(iun, timelineEventId);

        Assertions.assertTrue(timelineElementInternal.isPresent());
        TimelineElementInternal timelineElement = timelineElementInternal.get();
        Assertions.assertNotNull(timelineElement.getLegalFactsIds().get(0));
        Assertions.assertNotNull(timelineElement.getTimestamp());

        SendDigitalFeedbackDetailsInt details = (SendDigitalFeedbackDetailsInt) timelineElement.getDetails();
        Assertions.assertEquals(digitalAddress.getAddress(), details.getDigitalAddress().getAddress());
        Assertions.assertEquals(status, details.getResponseStatus());
    }

    public synchronized static NotificationStatusInt getNotificationStatus(NotificationInt notification, TimelineService timelineService, StatusUtils statusUtils) {
        int numberOfRecipient = notification.getRecipients().size();
        Instant notificationCreatedAt = notification.getSentAt();

        Set<TimelineElementInternal> timelineElements = timelineService.getTimeline(notification.getIun(), true);

        List<NotificationStatusHistoryElementInt> statusHistoryElements = statusUtils.getStatusHistory(timelineElements, numberOfRecipient, notificationCreatedAt);
        
        log.info("[TEST] timelineElements {}", timelineElements.stream().map(t -> t.getElementId()).toList());
        NotificationStatusInt notificationStatusInt =  statusUtils.getCurrentStatus(statusHistoryElements);
        log.info("[TEST] notificationStatus {} - iun={}", notificationStatusInt, notification.getIun());
        return notificationStatusInt;
    }

    public synchronized static boolean checkNotificationStatusHistoryContainsDesiredStatus(NotificationInt notification, TimelineService timelineService, StatusUtils statusUtils, NotificationStatusInt desiredStatus) {
        int numberOfRecipient = notification.getRecipients().size();
        Instant notificationCreatedAt = notification.getSentAt();

        Set<TimelineElementInternal> timelineElements = timelineService.getTimeline(notification.getIun(), true);

        List<NotificationStatusHistoryElementInt> statusHistoryElements = statusUtils.getStatusHistory(timelineElements, numberOfRecipient, notificationCreatedAt);
        
        log.info("[TEST] Searching status {} in status history is {} ",desiredStatus, statusHistoryElements);
        return statusHistoryElements.stream().anyMatch(history -> history.getStatus().equals(desiredStatus));
    }

    public static void checkIsNotPresentRefinement(String iun, Integer recIndex, TimelineService timelineService) {
        Assertions.assertFalse(timelineService.getTimelineElement(
                iun,
                TimelineEventId.REFINEMENT.buildEventId(
                        EventId.builder()
                                .iun(iun)
                                .recIndex(recIndex)
                                .build())
        ).isPresent());
    }

    public static boolean checkIsPresentViewed(String iun, Integer recIndex, TimelineService timelineService) {
        Optional<TimelineElementInternal> timelineElementOpt = timelineService.getTimelineElement(
                iun,
                TimelineEventId.NOTIFICATION_VIEWED.buildEventId(
                        EventId.builder()
                                .iun(iun)
                                .recIndex(recIndex)
                                .build()));

        Assertions.assertTrue(timelineElementOpt.isPresent());

        return true;
    }

    public static boolean checkIsPresentRefinement(String iun, Integer recIndex, TimelineService timelineService) {
        Optional<TimelineElementInternal> timelineElementOpt = getRefinement(iun, recIndex, timelineService);

        Assertions.assertTrue(timelineElementOpt.isPresent());
        TimelineElementInternal timelineElement = timelineElementOpt.get();
        RefinementDetailsInt detailsInt = (RefinementDetailsInt) timelineElement.getDetails();
        Assertions.assertNotNull(detailsInt.getNotificationCost());

        return true;
    }

    public static boolean checkIsPresentDigitalSuccessWorkflowAndRefinement(String iun, Integer recIndex, TimelineService timelineService) {
        Optional<TimelineElementInternal> digitalSuccessOpt = timelineService.getTimelineElement(
                iun,
                TimelineEventId.DIGITAL_SUCCESS_WORKFLOW.buildEventId(
                        EventId.builder()
                                .iun(iun)
                                .recIndex(recIndex)
                                .build()
                )
        );
        
        if(digitalSuccessOpt.isPresent()){
            return timelineService.getTimelineElement(
                    iun,
                    TimelineEventId.REFINEMENT.buildEventId(
                            EventId.builder()
                                    .iun(iun)
                                    .recIndex(recIndex)
                                    .build()
                    )
            ).isPresent();
        }
        
        return false;
    }

    public static boolean checkIsPresentDigitalFailureWorkflowAndRefinement(String iun, Integer recIndex, TimelineService timelineService) {
        Optional<TimelineElementInternal> digitalFailureOpt = timelineService.getTimelineElement(
                iun,
                TimelineEventId.DIGITAL_FAILURE_WORKFLOW.buildEventId(
                        EventId.builder()
                                .iun(iun)
                                .recIndex(recIndex)
                                .build()
                )
        );

        if(digitalFailureOpt.isPresent()){
            return timelineService.getTimelineElement(
                    iun,
                    TimelineEventId.REFINEMENT.buildEventId(
                            EventId.builder()
                                    .iun(iun)
                                    .recIndex(recIndex)
                                    .build()
                    )
            ).isPresent();
        }

        return false;
    }

    public static boolean checkIsPresentAnalogSuccessWorkflowAndRefinement(String iun, Integer recIndex, TimelineService timelineService) {
        Optional<TimelineElementInternal> analogSuccessOpt = timelineService.getTimelineElement(
                iun,
                TimelineEventId.ANALOG_SUCCESS_WORKFLOW.buildEventId(
                        EventId.builder()
                                .iun(iun)
                                .recIndex(recIndex)
                                .build()
                )
        );

        if(analogSuccessOpt.isPresent()){
            return timelineService.getTimelineElement(
                    iun,
                    TimelineEventId.REFINEMENT.buildEventId(
                            EventId.builder()
                                    .iun(iun)
                                    .recIndex(recIndex)
                                    .build()
                    )
            ).isPresent();
        }

        return false;
    }

    public static boolean checkIsPresentAnalogFailureWorkflowAndRefinement(String iun, Integer recIndex, TimelineService timelineService) {
        Optional<TimelineElementInternal> analogFailure = timelineService.getTimelineElement(
                iun,
                TimelineEventId.ANALOG_FAILURE_WORKFLOW.buildEventId(
                        EventId.builder()
                                .iun(iun)
                                .recIndex(recIndex)
                                .build()
                )
        );

        if(analogFailure.isPresent()){
            return timelineService.getTimelineElement(
                    iun,
                    TimelineEventId.REFINEMENT.buildEventId(
                            EventId.builder()
                                    .iun(iun)
                                    .recIndex(recIndex)
                                    .build()
                    )
            ).isPresent();
        }

        return false;
    }

    public static boolean checkIsPresentAnalogWorkflowRecipientDeceased(String iun, Integer recIndex, TimelineService timelineService) {
        Optional<TimelineElementInternal> analogRecipientDeceased = timelineService.getTimelineElement(
                iun,
                TimelineEventId.ANALOG_WORKFLOW_RECIPIENT_DECEASED.buildEventId(
                        EventId.builder()
                                .iun(iun)
                                .recIndex(recIndex)
                                .build()
                )
        );

        return analogRecipientDeceased.isPresent();
    }
    
    public static boolean checkIsPresentDigitalFailure(String iun, Integer recIndex, TimelineService timelineService) {
        Optional<TimelineElementInternal> timelineElementOpt = timelineService.getTimelineElement(
                iun,
                TimelineEventId.DIGITAL_FAILURE_WORKFLOW.buildEventId(
                        EventId.builder()
                                .iun(iun)
                                .recIndex(recIndex)
                                .build()
                )
        );

        return timelineElementOpt.isPresent();
    }

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

    public static Optional<TimelineElementInternal> getRefinement(String iun, Integer recIndex, TimelineService timelineService) {
        return timelineService.getTimelineElement(
                iun,
                TimelineEventId.REFINEMENT.buildEventId(
                        EventId.builder()
                                .iun(iun)
                                .recIndex(recIndex)
                                .build()));
    }

    public static void checkFailureRefinement(String iun,
                                              Integer recIndex,
                                              int refinementNumberOfInvocation,
                                              TimelineService timelineService,
                                              SchedulerService scheduler,
                                              PnDeliveryPushValidatorConfigs pnDeliveryPushConfigs) {
        ArgumentCaptor<Instant> instantArgumentCaptor = ArgumentCaptor.forClass(Instant.class);

        Mockito.verify(scheduler, Mockito.times(refinementNumberOfInvocation)).scheduleEvent(eq(iun), eq(recIndex), instantArgumentCaptor.capture(), Mockito.any(ActionType.class));
        List<Instant> instantArgumentCaptorList = instantArgumentCaptor.getAllValues();
        //Viene ottenuta la data di perfezionamento (Valutare se inserire la data di scheduling come campo del timeline element details)
        Instant refinementDate = instantArgumentCaptorList.get(instantArgumentCaptorList.size() - 1);

        List<TimelineElementInternal> lastSendDigitalElementList = timelineService.getTimeline(iun, false).stream()
                .filter(element -> TimelineElementCategoryInt.SEND_DIGITAL_DOMICILE.equals(element.getCategory()))
                .sorted(Comparator.comparing(TimelineElementInternal::getTimestamp)).collect(Collectors.toList());

        Instant lastSendDigitalDate = lastSendDigitalElementList.get(lastSendDigitalElementList.size() - 1).getTimestamp();
        //Viene ottenuta la data dell'ultimo invio verso externalChannel
        ZonedDateTime notificationDateTime = DateFormatUtils.parseInstantToZonedDateTime(lastSendDigitalDate);

        ZonedDateTime schedulingDate = notificationDateTime.plus(pnDeliveryPushConfigs.getTimeParams().getSchedulingDaysFailureDigitalRefinement());

        //Viene verificato che la data di perfezionamento sia uguale alla data dell'ultimo invio + giorni previsti dal perfezionamento
        Assertions.assertEquals(schedulingDate.toInstant(), refinementDate);
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
                                                NotificationRecipientInt recipient,
                                                Integer recIndex,
                                                int sentPecAttemptNumber,
                                                GeneratedLegalFactsInfo generatedLegalFactsInfo,
                                                EndWorkflowStatus endWorkflowStatus,
                                                LegalFactGenerator legalFactGenerator,
                                                TimelineService timelineService,
                                                DelegateInfoInt delegateInfo
    ) {
        TestUtils.checkNotificationReceivedLegalFactGeneration(
                notification,
                legalFactGenerator,
                generatedLegalFactsInfo.isNotificationReceivedLegalFactGenerated()
        );

        TestUtils.generateNotificationAAR(
                notification,
                recipient,
                legalFactGenerator,
                generatedLegalFactsInfo.isNotificationAARGenerated()
        );

        TestUtils.checkNotificationViewedLegalFact(
                notification.getIun(),
                recipient,
                legalFactGenerator,
                delegateInfo,
                generatedLegalFactsInfo.isNotificationViewedLegalFactGenerated(),
                notification
        );

        TestUtils.checkPecDeliveryWorkflowLegalFactsGeneration(
                notification,
                recipient,
                sentPecAttemptNumber,
                endWorkflowStatus,
                legalFactGenerator,
                generatedLegalFactsInfo.isPecDeliveryWorkflowLegalFactsGenerated()
        );

        TestUtils.checkCompletelyUnreachableLegalFactsGeneration(notification,
                recipient,
                endWorkflowStatus,
                legalFactGenerator,
                generatedLegalFactsInfo.isNotificationCompletelyUnreachableLegalFactGenerated());

        TestUtils.checkGenerateNotificationCancelledLegalFact(notification,
                legalFactGenerator,
                generatedLegalFactsInfo.notificationCancelled);
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

    private static void checkNotificationViewedLegalFact(String iun,
                                                         NotificationRecipientInt recipient,
                                                         LegalFactGenerator legalFactGenerator,
                                                         DelegateInfoInt delegateInfo,
                                                         boolean itWasGenerated,
                                                         NotificationInt notification) {
        int times = getTimes(itWasGenerated);

        try {
            Mockito.verify(legalFactGenerator, Mockito.times(times)).generateNotificationViewedLegalFact(eq(iun), eq(recipient), eq(delegateInfo), Mockito.any(Instant.class), eq(notification));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void generateNotificationAAR(NotificationInt notification,
                                                NotificationRecipientInt recipient,
                                                LegalFactGenerator legalFactGenerator,
                                                boolean itWasGenerated) {
        int times = getTimes(itWasGenerated);
        String quickAccessToken = "test";

        try {
            Mockito.verify(legalFactGenerator, Mockito.times(times)).generateNotificationAAR(notification, recipient, quickAccessToken);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void checkPecDeliveryWorkflowLegalFactsGeneration(NotificationInt notification,
                                                                     NotificationRecipientInt recipient,
                                                                     int sentPecAttemptNumber,
                                                                     EndWorkflowStatus endWorkflowStatus,
                                                                     LegalFactGenerator legalFactGenerator,
                                                                     boolean itWasGenerated
    ) {
        int times = getTimes(itWasGenerated);

        ArgumentCaptor<List<SendDigitalFeedbackDetailsInt>> sendDigitalFeedbackCaptor = ArgumentCaptor.forClass(List.class);

        try {
            Mockito.verify(legalFactGenerator, Mockito.times(times)).generatePecDeliveryWorkflowLegalFact(sendDigitalFeedbackCaptor.capture(), eq(notification),
                    eq(recipient), eq(endWorkflowStatus), Mockito.any(Instant.class));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        if (itWasGenerated) {
            List<SendDigitalFeedbackDetailsInt> listSendDigitalFeedbackDetail = sendDigitalFeedbackCaptor.getValue();

            Assertions.assertEquals(sentPecAttemptNumber, listSendDigitalFeedbackDetail.size());
        }
    }


    private static void checkCompletelyUnreachableLegalFactsGeneration(NotificationInt notification,
                                                                       NotificationRecipientInt recipient,
                                                                       EndWorkflowStatus endWorkflowStatus,
                                                                       LegalFactGenerator legalFactGenerator,
                                                                       boolean itWasGenerated
    ) {
        int times = getTimes(itWasGenerated);


        try {
            Mockito.verify(legalFactGenerator, Mockito.times(times)).generateAnalogDeliveryFailureWorkflowLegalFact(eq(notification),
                    eq(recipient), eq(endWorkflowStatus), Mockito.any(Instant.class));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }


    private static void checkGenerateNotificationCancelledLegalFact(NotificationInt notification,
                                                                       LegalFactGenerator legalFactGenerator,
                                                                       boolean itWasGenerated
    ) {
        int times = getTimes(itWasGenerated);
        try {
            Mockito.verify(legalFactGenerator, Mockito.times(times)).generateNotificationCancelledLegalFact(eq(notification), Mockito.any(Instant.class));
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
        int int_random = rand.nextInt(upperbound);
        return "iun-" + callerMethod + "_" + int_random;
    }


    public static void initializeAllMockClient(SafeStorageClientMock safeStorageClientMock,
                                               PnDeliveryClientMock pnDeliveryClientMock,
//                                               NationalRegistriesClientMock nationalRegistriesClientMock,//ToDo: questo va riportato?
                                               PnDataVaultClientReactiveMock pnDataVaultClientReactiveMock,
                                               DocumentCreationRequestDaoMock documentCreationRequestDaoMock,
                                               AddressManagerClientMock addressManagerClientMock,
                                               ActionPoolMock actionPoolMock
    ) {

        log.info("CLEARING MOCKS");

        ThreadPool.killThreads();

        safeStorageClientMock.clear();
        pnDeliveryClientMock.clear();
//        nationalRegistriesClientMock.clear(); //ToDo: questo va riportato?
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
