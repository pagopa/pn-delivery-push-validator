package it.pagopa.pn.deliverypushvalidator.service.mapper;

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
                .iun("IUN123").elementId("EID456").category(TimelineElementCategoryInt.VALIDATED_F24).build();

        NotificationInt notificationInt = NotificationInt.builder()
                .iun("IUN123").paProtocolNumber("PROT789").sentAt(Instant.now())
                .recipients(List.of(recipient)).build();

        NewTimelineElement result = mapper.getNewTimelineElement(timelineElementInternal, notificationInt);

        assertThat(result.getTimelineElement()).isNotNull();
        assertThat(result.getNotificationInfo().getIun()).isEqualTo("IUN123");
        assertThat(result.getNotificationInfo().getNumberOfRecipients()).isEqualTo(1);
    }

    @Test
    void getNewTimelineElementHandlesNullRecipients() {
        TimelineElementInternal timelineElementInternal = TimelineElementInternal.builder()
                .iun("IUN123").elementId("EID456").category(TimelineElementCategoryInt.VALIDATED_F24).build();

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
                .notificationSentAt(Instant.now()).ingestionTimestamp(Instant.now()).eventTimestamp(Instant.now());

        TimelineElementInternal result = mapper.toTimelineElementInternal(timelineElement);

        assertThat(result.getIun()).isEqualTo("IUN123");
        assertThat(result.getElementId()).isEqualTo("EID456");
        assertThat(result.getCategory()).isEqualTo(TimelineElementCategoryInt.VALIDATE_NORMALIZE_ADDRESSES_REQUEST);
        assertThat(result.getLegalFactsIds()).isNotEmpty();
        assertThat(result.getLegalFactsIds().getFirst().getKey()).isEqualTo(legalFactsIdInt.getKey());
        assertThat(result.getLegalFactsIds().getFirst().getCategory()).isEqualTo(legalFactsIdInt.getCategory());
        assertThat(result.getStatusInfo().getActual()).isEqualTo("DELIVERED");
    }

    @Test
    void toTimelineElementInternalHandlesEmptyLegalFactsIds() {
        TimelineElement timelineElement = new TimelineElement()
                .iun("IUN123").elementId("EID456").timestamp(Instant.now()).paId("PAID")
                .legalFactsIds(List.of())
                .category(TimelineCategory.VALIDATE_NORMALIZE_ADDRESSES_REQUEST)
                .details(mock(TimelineElementDetails.class))
                .statusInfo(new StatusInfo().actual("DELIVERED").statusChanged(true).statusChangeTimestamp(Instant.now()))
                .notificationSentAt(Instant.now()).ingestionTimestamp(Instant.now()).eventTimestamp(Instant.now());

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
                .notificationSentAt(Instant.now()).ingestionTimestamp(Instant.now()).eventTimestamp(Instant.now());

        TimelineElementInternal result = mapper.toTimelineElementInternal(timelineElement);

        assertThat(result.getLegalFactsIds()).isNull();
    }
}
