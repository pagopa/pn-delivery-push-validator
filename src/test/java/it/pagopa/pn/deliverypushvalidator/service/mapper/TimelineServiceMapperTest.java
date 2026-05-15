package it.pagopa.pn.deliverypushvalidator.service.mapper;

import it.pagopa.pn.commons.exceptions.PnInternalException;
import it.pagopa.pn.deliverypushvalidator.dto.address.PhysicalAddressInt;
import it.pagopa.pn.deliverypushvalidator.dto.ext.delivery.notification.NotificationInt;
import it.pagopa.pn.deliverypushvalidator.dto.ext.delivery.notification.NotificationRecipientInt;
import it.pagopa.pn.deliverypushvalidator.dto.legalfacts.LegalFactCategoryInt;
import it.pagopa.pn.deliverypushvalidator.dto.legalfacts.LegalFactsIdInt;
import it.pagopa.pn.deliverypushvalidator.dto.timeline.TimelineElementInternal;
import it.pagopa.pn.deliverypushvalidator.dto.timeline.details.TimelineElementCategoryInt;
import it.pagopa.pn.deliverypushvalidator.generated.openapi.msclient.timelineservice.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static it.pagopa.pn.deliverypushvalidator.exception.PnDeliveryPushValidatorExceptionCodes.ERROR_CODE_TIMELINESERVICE_COMMUNICATION_TYPE_NOT_PRESENT;
import static org.mockito.Mockito.mock;

class TimelineServiceMapperTest {

    private TimelineServiceMapper mapper;

    @BeforeEach
    void setup() {
        SmartMapper smartMapper = mock(SmartMapper.class);
        mapper = new TimelineServiceMapper(smartMapper);
    }

    @Test
    void getNewTimelineElementMapsNotificationInfoAndTimelineElementCorrectly() {
        PhysicalAddressInt address = PhysicalAddressInt.builder()
                .zip("zip").at("at").address("address").municipality("mun")
                .province("province").fullname("fullname").build();

        NotificationRecipientInt recipient = NotificationRecipientInt.builder()
                .taxId("taxId").denomination("denomination").physicalAddress(address).build();

        TimelineElementInternal timelineElementInternal = TimelineElementInternal.builder()
                .iun("IUN123").elementId("EID456")
                .category(TimelineElementCategoryInt.VALIDATED_F24)
                .communicationType(it.pagopa.pn.deliverypushvalidator.dto.timeline.CommunicationType.INFORMAL)
                .build();

        NotificationInt notificationInt = NotificationInt.builder()
                .iun("IUN123").paProtocolNumber("PROT789").sentAt(Instant.now())
                .recipients(List.of(recipient)).build();

        NewTimelineElement result = mapper.getNewTimelineElement(timelineElementInternal, notificationInt);

        assertThat(result.getTimelineElement()).isNotNull();
        assertThat(result.getTimelineElement().getCommunicationType()).isEqualTo(CommunicationType.INFORMAL);
        assertThat(result.getNotificationInfo().getIun()).isEqualTo("IUN123");
        assertThat(result.getNotificationInfo().getNumberOfRecipients()).isEqualTo(1);
    }

    @Test
    void getNewTimelineElementHandlesNullRecipients() {
        TimelineElementInternal timelineElementInternal = TimelineElementInternal.builder()
                .iun("IUN123").elementId("EID456")
                .category(TimelineElementCategoryInt.VALIDATED_F24)
                .communicationType(it.pagopa.pn.deliverypushvalidator.dto.timeline.CommunicationType.INFORMAL)
                .build();

        NotificationInt notificationInt = NotificationInt.builder()
                .iun("IUN123").paProtocolNumber("PROT789").sentAt(Instant.now())
                .recipients(null).build();

        NewTimelineElement result = mapper.getNewTimelineElement(timelineElementInternal, notificationInt);

        assertThat(result.getNotificationInfo()).isNotNull();
    }

    @Test
    void toTimelineElementInternalReturnsNullIfInputIsNull() {
        assertThat(mapper.toTimelineElementInternal(null)).isNull();
    }

    @Test
    void toTimelineElementInternalMapsAllFieldsCorrectly() {
        LegalFactsId legalFactsId = new LegalFactsId();
        legalFactsId.setKey("chiaveTest");
        legalFactsId.setCategory(LegalFactsId.CategoryEnum.ANALOG_DELIVERY);

        LegalFactsIdInt legalFactsIdInt = LegalFactsIdInt.builder()
                .key("chiaveTest")
                .category(LegalFactCategoryInt.ANALOG_DELIVERY)
                .build();

        TimelineElementDetails details = mock(TimelineElementDetails.class);
        TimelineElement timelineElement = new TimelineElement()
                .iun("IUN123").elementId("EID456").timestamp(Instant.now()).paId("PAID")
                .legalFactsIds(List.of(legalFactsId))
                .category(TimelineCategory.VALIDATE_NORMALIZE_ADDRESSES_REQUEST)
                .details(details)
                .statusInfo(new StatusInfo().actual("DELIVERED").statusChanged(true).statusChangeTimestamp(Instant.now()))
                .notificationSentAt(Instant.now()).ingestionTimestamp(Instant.now()).eventTimestamp(Instant.now())
                .communicationType(CommunicationType.INFORMAL);

        TimelineElementInternal result = mapper.toTimelineElementInternal(timelineElement);

        assertThat(result.getIun()).isEqualTo("IUN123");
        assertThat(result.getElementId()).isEqualTo("EID456");
        assertThat(result.getCategory()).isEqualTo(TimelineElementCategoryInt.VALIDATE_NORMALIZE_ADDRESSES_REQUEST);
        assertThat(result.getLegalFactsIds()).isNotEmpty();
        assertThat(result.getLegalFactsIds().getFirst().getKey()).isEqualTo(legalFactsIdInt.getKey());
        assertThat(result.getLegalFactsIds().getFirst().getCategory()).isEqualTo(legalFactsIdInt.getCategory());
        assertThat(result.getStatusInfo().getActual()).isEqualTo("DELIVERED");
        assertThat(result.getCommunicationType()).isEqualTo(it.pagopa.pn.deliverypushvalidator.dto.timeline.CommunicationType.INFORMAL);
    }

    @Test
    void getNewTimelineElementMapsNullCommunicationType() {
        TimelineElementInternal timelineElementInternal = TimelineElementInternal.builder()
                .iun("IUN123").elementId("EID456")
                .category(TimelineElementCategoryInt.VALIDATED_F24)
                .communicationType(null)
                .build();

        NotificationInt notificationInt = NotificationInt.builder()
                .iun("IUN123").paProtocolNumber("PROT789").sentAt(Instant.now())
                .recipients(List.of()).build();

        assertThatThrownBy(() -> mapper.getNewTimelineElement(timelineElementInternal, notificationInt))
                .isInstanceOf(PnInternalException.class)
                .satisfies(throwable -> {
                    PnInternalException exception = (PnInternalException) throwable;
                    assertThat(exception.getProblem().getErrors()).isNotEmpty();
                    assertThat(exception.getProblem().getErrors().stream().findFirst().orElseThrow().getCode())
                            .isEqualTo(ERROR_CODE_TIMELINESERVICE_COMMUNICATION_TYPE_NOT_PRESENT);
                });
    }

    @Test
    void toTimelineElementInternalMapsNullCommunicationType() {
        TimelineElement timelineElement = new TimelineElement()
                .iun("IUN123").elementId("EID456").timestamp(Instant.now()).paId("PAID")
                .legalFactsIds(List.of())
                .category(TimelineCategory.VALIDATE_NORMALIZE_ADDRESSES_REQUEST)
                .details(mock(TimelineElementDetails.class))
                .statusInfo(new StatusInfo().actual("DELIVERED").statusChanged(true).statusChangeTimestamp(Instant.now()))
                .notificationSentAt(Instant.now()).ingestionTimestamp(Instant.now()).eventTimestamp(Instant.now())
                .communicationType(null);

        assertThatThrownBy(() -> mapper.toTimelineElementInternal(timelineElement))
                .isInstanceOf(PnInternalException.class)
                .satisfies(throwable -> {
                    PnInternalException exception = (PnInternalException) throwable;
                    assertThat(exception.getProblem().getErrors()).isNotEmpty();
                    assertThat(exception.getProblem().getErrors().stream().findFirst().orElseThrow().getCode())
                            .isEqualTo(ERROR_CODE_TIMELINESERVICE_COMMUNICATION_TYPE_NOT_PRESENT);
                });
    }

    @Test
    void getNewTimelineElementMapsLegalFactsIds() {
        LegalFactsIdInt legalFactsIdInt = LegalFactsIdInt.builder()
                .key("lf-001")
                .category(LegalFactCategoryInt.ANALOG_DELIVERY)
                .build();

        TimelineElementInternal timelineElementInternal = TimelineElementInternal.builder()
                .iun("IUN123")
                .elementId("EID456")
                .category(TimelineElementCategoryInt.VALIDATED_F24)
                .legalFactsIds(List.of(legalFactsIdInt))
                .communicationType(it.pagopa.pn.deliverypushvalidator.dto.timeline.CommunicationType.INFORMAL)
                .build();

        NotificationInt notificationInt = NotificationInt.builder()
                .iun("IUN123")
                .paProtocolNumber("PROT789")
                .sentAt(Instant.now())
                .recipients(List.of())
                .build();

        NewTimelineElement result = mapper.getNewTimelineElement(timelineElementInternal, notificationInt);

        assertThat(result.getTimelineElement()).isNotNull();
        assertThat(result.getTimelineElement().getLegalFactsIds()).hasSize(1);
        assertThat(result.getTimelineElement().getLegalFactsIds().getFirst().getKey()).isEqualTo("lf-001");
        assertThat(result.getTimelineElement().getLegalFactsIds().getFirst().getCategory())
                .isEqualTo(LegalFactsId.CategoryEnum.ANALOG_DELIVERY);
    }

    @Test
    void toTimelineElementInternalMapsNullStatusInfo() {
        TimelineElement timelineElement = new TimelineElement()
                .iun("IUN123").elementId("EID456").timestamp(Instant.now()).paId("PAID")
                .legalFactsIds(List.of())
                .category(TimelineCategory.VALIDATE_NORMALIZE_ADDRESSES_REQUEST)
                .details(mock(TimelineElementDetails.class))
                .statusInfo(null)
                .notificationSentAt(Instant.now()).ingestionTimestamp(Instant.now()).eventTimestamp(Instant.now())
                .communicationType(CommunicationType.INFORMAL);

        TimelineElementInternal result = mapper.toTimelineElementInternal(timelineElement);

        assertThat(result.getStatusInfo()).isNull();
    }

    @Test
    void toTimelineElementInternalMapsStatusChangedDefaultToFalseWhenNull() {
        TimelineElement timelineElement = new TimelineElement()
                .iun("IUN123").elementId("EID456").timestamp(Instant.now()).paId("PAID")
                .legalFactsIds(List.of())
                .category(TimelineCategory.VALIDATE_NORMALIZE_ADDRESSES_REQUEST)
                .details(mock(TimelineElementDetails.class))
                .statusInfo(new StatusInfo().actual("DELIVERED").statusChangeTimestamp(Instant.now()).statusChanged(null))
                .notificationSentAt(Instant.now()).ingestionTimestamp(Instant.now()).eventTimestamp(Instant.now())
                .communicationType(CommunicationType.INFORMAL);

        TimelineElementInternal result = mapper.toTimelineElementInternal(timelineElement);

        assertThat(result.getStatusInfo()).isNotNull();
        assertThat(result.getStatusInfo().isStatusChanged()).isFalse();
    }

    @Test
    void toTimelineElementInternalHandlesEmptyLegalFactsIds() {
        TimelineElement timelineElement = new TimelineElement()
                .iun("IUN123").elementId("EID456").timestamp(Instant.now()).paId("PAID")
                .legalFactsIds(List.of())
                .category(TimelineCategory.VALIDATE_NORMALIZE_ADDRESSES_REQUEST)
                .details(mock(TimelineElementDetails.class))
                .statusInfo(new StatusInfo().actual("DELIVERED").statusChanged(true).statusChangeTimestamp(Instant.now()))
                .notificationSentAt(Instant.now()).ingestionTimestamp(Instant.now()).eventTimestamp(Instant.now())
                .communicationType(CommunicationType.INFORMAL);

        TimelineElementInternal result = mapper.toTimelineElementInternal(timelineElement);

        assertThat(result.getLegalFactsIds()).isEmpty();
    }

    @Test
    void toTimelineElementInternalHandlesNullLegalFactsIds() {
        TimelineElement timelineElement = new TimelineElement()
                .iun("IUN123").elementId("EID456").timestamp(Instant.now()).paId("PAID")
                .legalFactsIds(null)
                .category(TimelineCategory.VALIDATE_NORMALIZE_ADDRESSES_REQUEST)
                .details(mock(TimelineElementDetails.class))
                .statusInfo(new StatusInfo().actual("DELIVERED").statusChanged(true).statusChangeTimestamp(Instant.now()))
                .notificationSentAt(Instant.now()).ingestionTimestamp(Instant.now()).eventTimestamp(Instant.now())
                .communicationType(CommunicationType.INFORMAL);

        TimelineElementInternal result = mapper.toTimelineElementInternal(timelineElement);

        assertThat(result.getLegalFactsIds()).isNull();
    }
}
