package it.pagopa.pn.deliverypushvalidator.action.utils;

import it.pagopa.pn.deliverypushvalidator.dto.address.PhysicalAddressInt;
import it.pagopa.pn.deliverypushvalidator.dto.ext.delivery.notification.NotificationInt;
import it.pagopa.pn.deliverypushvalidator.dto.ext.publicregistry.NationalRegistriesResponse;
import it.pagopa.pn.deliverypushvalidator.dto.legalfacts.LegalFactCategoryInt;
import it.pagopa.pn.deliverypushvalidator.dto.legalfacts.LegalFactsIdInt;
import it.pagopa.pn.deliverypushvalidator.dto.timeline.*;
import it.pagopa.pn.deliverypushvalidator.dto.timeline.details.*;
import it.pagopa.pn.deliverypushvalidator.service.TimelineService;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static it.pagopa.pn.deliverypushvalidator.dto.timeline.TimelineEventId.REQUEST_REFUSED;

@Component
@Slf4j
public class TimelineUtils {
    private final TimelineService timelineService;

    public TimelineUtils(TimelineService timelineService) {
        this.timelineService = timelineService;
    }

    public TimelineElementInternal buildTimeline(NotificationInt notification,
                                                 TimelineElementCategoryInt category,
                                                 String elementId,
                                                 @NotNull TimelineElementDetailsInt details) {

        TimelineElementInternal.TimelineElementInternalBuilder timelineBuilder = TimelineElementInternal.builder()
                .legalFactsIds(Collections.emptyList());

        return buildTimeline(notification, category, elementId, details, timelineBuilder);
    }

    public TimelineElementInternal buildTimeline(NotificationInt notification,
                                                 TimelineElementCategoryInt category,
                                                 String elementId,
                                                 TimelineElementDetailsInt details,
                                                 TimelineElementInternal.TimelineElementInternalBuilder timelineBuilder) {
        return timelineBuilder
                .iun(notification.getIun())
                .category(category)
                .timestamp(Instant.now())
                .elementId(elementId)
                .details(details)
                .paId(notification.getSender().getPaId())
                .communicationType(notification.getCommunicationType())
                .notificationSentAt(notification.getSentAt())
                .campaignId(notification.getCampaignId())
                .build();
    }

    public TimelineElementInternal buildValidateF24RequestTimelineElement(NotificationInt notification) {
        log.debug("buildValidateF24RequestTimelineElement - iun={}", notification.getIun());

        String correlationId = TimelineEventId.VALIDATE_F24_REQUEST.buildEventId(
                EventId.builder()
                        .iun(notification.getIun())
                        .build());

        ValidateF24Int details = ValidateF24Int.builder().build();
        return buildTimeline(notification, TimelineElementCategoryInt.VALIDATE_F24_REQUEST, correlationId, details);
    }

    public TimelineElementInternal buildValidatedF24TimelineElement(NotificationInt notification) {
        log.debug("buildValidatedF24TimelineElement - iun={}", notification.getIun());

        String correlationId = TimelineEventId.VALIDATED_F24.buildEventId(
                EventId.builder()
                        .iun(notification.getIun())
                        .build());

        ValidatedF24DetailInt detail = ValidatedF24DetailInt.builder().build();
        return buildTimeline(notification, TimelineElementCategoryInt.VALIDATED_F24, correlationId, detail);
    }

    public TimelineElementInternal buildValidateAndNormalizeAddressTimelineElement(NotificationInt notification, String elementId) {
        log.debug("buildValidateAddressTimelineElement - iun={}", notification.getIun());

        ValidateNormalizeAddressDetailsInt details = ValidateNormalizeAddressDetailsInt.builder().build();
        return buildTimeline(notification, TimelineElementCategoryInt.VALIDATE_NORMALIZE_ADDRESSES_REQUEST, elementId, details);
    }

    public TimelineElementInternal buildAcceptedRequestTimelineElement(NotificationInt notification, String legalFactId) {
        log.debug("buildAcceptedRequestTimelineElement - iun={}", notification.getIun());

        String elementId = TimelineEventId.REQUEST_ACCEPTED.buildEventId(
                EventId.builder()
                        .iun(notification.getIun())
                        .build());

        NotificationRequestAcceptedDetailsInt details = NotificationRequestAcceptedDetailsInt.builder()
                .paProtocolNumber(notification.getPaProtocolNumber())
                .idempotenceToken(notification.getIdempotenceToken())
                .notificationRequestId(Base64.getEncoder().encodeToString(notification.getIun().getBytes()))
                .build();

        TimelineElementInternal.TimelineElementInternalBuilder timelineBuilder = TimelineElementInternal.builder()
                .legalFactsIds(legalFactId != null
                        ? singleLegalFactId(legalFactId, LegalFactCategoryInt.SENDER_ACK)
                        : Collections.emptyList());

        return buildTimeline(notification, TimelineElementCategoryInt.REQUEST_ACCEPTED, elementId, details, timelineBuilder);
    }

    public TimelineElementInternal buildRefusedRequestTimelineElement(NotificationInt notification, List<NotificationRefusedErrorInt> errors, Integer notificationCost) {
        log.debug("buildRefusedRequestTimelineElement - iun={}", notification.getIun());

        String elementId = REQUEST_REFUSED.buildEventId(
                EventId.builder()
                        .iun(notification.getIun())
                        .build());

        int numberOfRecipients = notification.getRecipients().size();

        RequestRefusedDetailsInt details = RequestRefusedDetailsInt.builder()
                .refusalReasons(errors)
                .numberOfRecipients(numberOfRecipients)
                .notificationCost(notificationCost)
                .paProtocolNumber(notification.getPaProtocolNumber())
                .idempotenceToken(notification.getIdempotenceToken())
                .notificationRequestId(Base64.getEncoder().encodeToString(notification.getIun().getBytes()))
                .build();

        return buildTimeline(notification, TimelineElementCategoryInt.REQUEST_REFUSED, elementId, details);
    }

    public TimelineElementInternal buildSenderAckLegalFactCreationRequest(NotificationInt notification, String legalFactId) {
        log.debug("buildSenderAckLegalFactCreationRequest- iun={}", notification.getIun());

        String elementId = TimelineEventId.SENDERACK_CREATION_REQUEST.buildEventId(
                EventId.builder()
                        .iun(notification.getIun())
                        .build());

        SenderAckCreationRequestDetailsInt details = SenderAckCreationRequestDetailsInt.builder()
                .legalFactId(legalFactId)
                .build();

        TimelineElementInternal.TimelineElementInternalBuilder timelineBuilder = TimelineElementInternal.builder()
                .legalFactsIds(Collections.emptyList());

        return buildTimeline(
                notification,
                TimelineElementCategoryInt.SENDER_ACK_CREATION_REQUEST,
                elementId,
                details,
                timelineBuilder
        );
    }

    public TimelineElementInternal buildNormalizedAddressTimelineElement(NotificationInt notification,
                                                                         Integer recIndex,
                                                                         PhysicalAddressInt oldAddress,
                                                                         PhysicalAddressInt normalizedAddress) {
        log.debug("buildNormalizedAddressTimelineElement - IUN={} and id={}", notification.getIun(), recIndex);

        String elementId = TimelineEventId.NORMALIZED_ADDRESS.buildEventId(
                EventId.builder()
                        .iun(notification.getIun())
                        .recIndex(recIndex)
                        .build());

        NormalizedAddressDetailsInt details = NormalizedAddressDetailsInt.builder()
                .recIndex(recIndex)
                .oldAddress(oldAddress)
                .normalizedAddress(normalizedAddress)
                .build();

        return buildTimeline(notification, TimelineElementCategoryInt.NORMALIZED_ADDRESS, elementId, details);
    }

    public TimelineElementInternal buildGeneratedF24TimelineElement(NotificationInt notification, int recipientIndex, List<String> f24Attachments) {
        log.debug("buildGeneratedF24TimelineElement - IUN={} and id={}", notification.getIun(), recipientIndex);

        String elementId = TimelineEventId.GENERATED_F24.buildEventId(
                EventId.builder()
                        .iun(notification.getIun())
                        .recIndex(recipientIndex)
                        .build());
        GeneratedF24DetailsInt details = GeneratedF24DetailsInt.builder()
                .f24Attachments(f24Attachments)
                .recIndex(recipientIndex)
                .build();

        return buildTimeline(notification, TimelineElementCategoryInt.GENERATED_F24, elementId, details);
    }

    public TimelineElementInternal buildGenerateF24RequestTimelineElement(NotificationInt notification) {
        log.debug("buildGenerateF24RequestTimelineElement - IUN={}", notification.getIun());

        String elementId = TimelineEventId.GENERATE_F24_REQUEST.buildEventId(
                EventId.builder()
                        .iun(notification.getIun())
                        .build());

        GenerateF24Int details = GenerateF24Int.builder().build();
        return buildTimeline(notification, TimelineElementCategoryInt.GENERATE_F24_REQUEST, elementId, details);
    }



    public TimelineElementInternal buildNationalRegistryValidationCall(String eventId, NotificationInt notification, List<Integer> recIndexes, DeliveryModeInt deliveryMode) {

        log.debug("buildNationalRegistryValidationCall - iun={}", notification.getIun());

        PublicRegistryValidationCallDetailsInt details = PublicRegistryValidationCallDetailsInt.builder()
                .recIndexes(recIndexes)
                .deliveryMode(deliveryMode)
                .sendDate(Instant.now())
                .build();

        return buildTimeline(notification, TimelineElementCategoryInt.PUBLIC_REGISTRY_VALIDATION_CALL, eventId, details);
    }

    public TimelineElementInternal buildNationalRegistryValidationResponse(NotificationInt notification, NationalRegistriesResponse response) {
        String eventId = TimelineEventId.NATIONAL_REGISTRY_VALIDATION_RESPONSE.buildEventId(
                EventId.builder()
                        .relatedTimelineId(response.getCorrelationId())
                        .recIndex(response.getRecIndex())
                        .build());

        log.debug("buildNationalRegistryValidationResponse - iun={}", notification.getIun());

        PublicRegistryValidationResponseDetailsInt details = PublicRegistryValidationResponseDetailsInt.builder()
                .recIndex(response.getRecIndex())
                .registry(response.getRegistry())
                .physicalAddress(response.getPhysicalAddress())
                .requestTimelineId(response.getCorrelationId())
                .build();

        return buildTimeline(notification, TimelineElementCategoryInt.PUBLIC_REGISTRY_VALIDATION_RESPONSE, eventId, details);
    }

    public List<LegalFactsIdInt> singleLegalFactId(String legalFactKey, LegalFactCategoryInt type) {
        return Collections.singletonList(LegalFactsIdInt.builder()
                .key(legalFactKey)
                .category(type)
                .build());
    }

    public boolean checkIsNotificationCancellationRequested(String iun) {
        log.debug("checkIsNotificationCancellationRequested - iun={}", iun);

        boolean isNotificationCancelled = timelineService.isNotificationCancellationRequested(iun);
        log.debug("NotificationCancellationRequested value is={}", isNotificationCancelled);

        return isNotificationCancelled;
    }

    public TimelineElementInternal buildNotificationCostValidationRequest(NotificationInt notification) {
        log.debug("buildNotificationCostValidationRequest - IUN={}", notification.getIun());

        String elementId = TimelineEventId.NOTIFICATION_COST_VALIDATION_REQUEST.buildEventId(
                EventId.builder()
                        .iun(notification.getIun())
                        .build());

        NotificationCostValidationRequestDetailsInt details = NotificationCostValidationRequestDetailsInt.builder()
                .build();

        return buildTimeline(notification, TimelineElementCategoryInt.NOTIFICATION_COST_VALIDATION_REQUEST, elementId, details);
    }

    public TimelineElementInternal buildNotificationCostValidationResponse(NotificationInt notification) {
        log.debug("buildNotificationCostValidationResponse - IUN={}", notification.getIun());

        String elementId = TimelineEventId.NOTIFICATION_COST_VALIDATION_RESPONSE.buildEventId(
                EventId.builder()
                        .iun(notification.getIun())
                        .build());

        NotificationCostValidationResponseDetailsInt details = NotificationCostValidationResponseDetailsInt.builder()
                .build();

        return buildTimeline(notification, TimelineElementCategoryInt.NOTIFICATION_COST_VALIDATION_RESPONSE, elementId, details);
    }

    public Optional<TimelineElementInternal> getValidatedF24(String iun){
        String elementId = TimelineEventId.VALIDATED_F24.buildEventId(
                EventId.builder()
                        .iun(iun)
                        .build());
        return timelineService.getTimelineElement(iun, elementId);
    }

    public String getIunFromTimelineId(String timelineId) {
        //<timelineId = CATEGORY_VALUE>;IUN_<IUN_VALUE>;RECINDEX_<RECINDEX_VALUE>...
        return timelineId.split("\\" + TimelineEventIdBuilder.DELIMITER)[1].replace("IUN_", "");
    }
}
