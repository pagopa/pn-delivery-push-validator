package it.pagopa.pn.deliverypushvalidator.service.mapper;

import it.pagopa.pn.commons.exceptions.PnInternalException;
import it.pagopa.pn.deliverypushvalidator.dto.ext.delivery.notification.NotificationInt;
import it.pagopa.pn.deliverypushvalidator.dto.legalfacts.LegalFactCategoryInt;
import it.pagopa.pn.deliverypushvalidator.dto.legalfacts.LegalFactsIdInt;
import it.pagopa.pn.deliverypushvalidator.dto.timeline.StatusInfoInternal;
import it.pagopa.pn.deliverypushvalidator.dto.timeline.TimelineElementInternal;
import it.pagopa.pn.deliverypushvalidator.dto.timeline.details.TimelineElementCategoryInt;
import it.pagopa.pn.deliverypushvalidator.dto.timeline.details.TimelineElementDetailsInt;
import it.pagopa.pn.deliverypushvalidator.generated.openapi.msclient.timelineservice.model.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

import static it.pagopa.pn.deliverypushvalidator.exception.PnDeliveryPushValidatorExceptionCodes.ERROR_CODE_TIMELINESERVICE_COMMUNICATION_TYPE_NOT_PRESENT;

@Slf4j
@Component
@RequiredArgsConstructor
public class TimelineServiceMapper {
    private final SmartMapper smartMapper;

    public NewTimelineElement getNewTimelineElement(TimelineElementInternal timelineElementInternal,
                                                    NotificationInt notificationInt) {
        return new NewTimelineElement()
                .timelineElement(toTimelineElement(timelineElementInternal))
                .notificationInfo(toNotificationInfo(notificationInt));
    }

    public TimelineElementInternal toTimelineElementInternal(TimelineElement timelineElement) {
        if (timelineElement == null) {
            return null;
        }
        TimelineElementCategoryInt category = TimelineElementCategoryInt.valueOf(timelineElement.getCategory().getValue());

        return TimelineElementInternal.builder()
                .iun(timelineElement.getIun())
                .elementId(timelineElement.getElementId())
                .timestamp(timelineElement.getTimestamp())
                .paId(timelineElement.getPaId())
                .legalFactsIds(timelineElement.getLegalFactsIds() != null ? toLegalFactsIdIntList(timelineElement.getLegalFactsIds()) : null)
                .category(category)
                .details(toTimelineElementDetailsInt(timelineElement.getDetails(), category))
                .statusInfo(toStatusInfoInternal(timelineElement.getStatusInfo()))
                .notificationSentAt(timelineElement.getNotificationSentAt())
                .ingestionTimestamp(timelineElement.getIngestionTimestamp())
                .eventTimestamp(timelineElement.getEventTimestamp())
                .communicationType(toInternalCommunicationType(timelineElement.getCommunicationType()))
                .build();
    }

    private NotificationInfo toNotificationInfo(NotificationInt notificationInt) {
        return new NotificationInfo()
                .iun(notificationInt.getIun())
                .paProtocolNumber(notificationInt.getPaProtocolNumber())
                .sentAt(notificationInt.getSentAt())
                .numberOfRecipients(notificationInt.getRecipients() != null ? notificationInt.getRecipients().size() : null);
    }

    private TimelineElement toTimelineElement(TimelineElementInternal timelineElementInternal) {
        return new TimelineElement()
                .iun(timelineElementInternal.getIun())
                .elementId(timelineElementInternal.getElementId())
                .timestamp(timelineElementInternal.getTimestamp())
                .paId(timelineElementInternal.getPaId())
                .legalFactsIds(timelineElementInternal.getLegalFactsIds() != null ? toLegalFactsIdList(timelineElementInternal.getLegalFactsIds()) : null)
                .category(TimelineCategory.valueOf(timelineElementInternal.getCategory().name()))
                .details(toTimelineElementDetails(timelineElementInternal.getDetails(), timelineElementInternal.getCategory().name()))
                .notificationSentAt(timelineElementInternal.getNotificationSentAt())
                .communicationType(toExternalCommunicationType(timelineElementInternal.getCommunicationType()));
    }

    private List<LegalFactsId> toLegalFactsIdList(List<LegalFactsIdInt> legalFactsIdIntList) {
        if (legalFactsIdIntList.isEmpty()) {
            return Collections.emptyList();
        }

        return legalFactsIdIntList.stream()
                .map(legalFactsIdInt -> new LegalFactsId()
                        .key(legalFactsIdInt.getKey())
                        .category(LegalFactsId.CategoryEnum.valueOf(legalFactsIdInt.getCategory().getValue())))
                .toList();
    }

    private List<LegalFactsIdInt> toLegalFactsIdIntList(List<LegalFactsId> legalFactsIdList) {
        if (legalFactsIdList.isEmpty()) {
            return Collections.emptyList();
        }
        return legalFactsIdList.stream()
                .map(legalFactsId -> {
                    assert legalFactsId.getCategory() != null;
                    return LegalFactsIdInt.builder()
                            .key(legalFactsId.getKey())
                            .category(LegalFactCategoryInt.valueOf(legalFactsId.getCategory().getValue()))
                            .build();
                })
                .toList();
    }

    private TimelineElementDetails toTimelineElementDetails(TimelineElementDetailsInt detailsInt, String category) {
        if (detailsInt == null) {
            return null;
        }

        detailsInt.setCategoryType(category);
        return smartMapper.mapToClassWithObjectMapper(detailsInt, TimelineElementDetails.class);
    }

    public TimelineElementDetailsInt toTimelineElementDetailsInt(TimelineElementDetails details, TimelineElementCategoryInt category) {        return SmartMapper.mapToClass(details, category.getDetailsJavaClass());
    }

    private it.pagopa.pn.deliverypushvalidator.dto.timeline.CommunicationType toInternalCommunicationType(CommunicationType communicationType) {
        if (communicationType == null) {
            throw new PnInternalException("communicationType is null in timeline element", ERROR_CODE_TIMELINESERVICE_COMMUNICATION_TYPE_NOT_PRESENT);
        }

        return it.pagopa.pn.deliverypushvalidator.dto.timeline.CommunicationType.valueOf(communicationType.name());
    }

    private CommunicationType toExternalCommunicationType(it.pagopa.pn.deliverypushvalidator.dto.timeline.CommunicationType communicationType) {
        if (communicationType == null) {
            throw new PnInternalException("communicationType is null in timeline element internal", ERROR_CODE_TIMELINESERVICE_COMMUNICATION_TYPE_NOT_PRESENT);
        }

        return CommunicationType.valueOf(communicationType.name());
    }

    private StatusInfoInternal toStatusInfoInternal(StatusInfo statusInfo) {
        if (statusInfo == null) return null;

        return StatusInfoInternal.builder()
                .actual(statusInfo.getActual())
                .statusChangeTimestamp(statusInfo.getStatusChangeTimestamp())
                .statusChanged(statusInfo.getStatusChanged() != null ? statusInfo.getStatusChanged() : Boolean.FALSE)
                .build();
    }
}

