package it.pagopa.pn.deliverypushvalidator.action.utils;

import it.pagopa.pn.deliverypushvalidator.dto.address.LegalDigitalAddressInt;
import it.pagopa.pn.deliverypushvalidator.dto.address.PhysicalAddressInt;
import it.pagopa.pn.deliverypushvalidator.dto.ext.delivery.notification.NotificationDocumentInt;
import it.pagopa.pn.deliverypushvalidator.dto.ext.delivery.notification.NotificationInt;
import it.pagopa.pn.deliverypushvalidator.dto.ext.delivery.notification.NotificationRecipientInt;
import it.pagopa.pn.deliverypushvalidator.dto.ext.delivery.notification.NotificationSenderInt;
import it.pagopa.pn.deliverypushvalidator.dto.ext.publicregistry.NationalRegistriesResponse;
import it.pagopa.pn.deliverypushvalidator.dto.timeline.*;
import it.pagopa.pn.deliverypushvalidator.dto.timeline.details.*;
import it.pagopa.pn.deliverypushvalidator.service.TimelineService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;

@ExtendWith(SpringExtension.class)
class TimelineUtilsTest {

    @Mock
    private TimelineService timelineService;

    private TimelineUtils timelineUtils;

    @BeforeEach
    void setUp() {
        timelineService = mock(TimelineService.class);
        timelineUtils = new TimelineUtils(timelineService);
    }

    @Test
    void buildTimeline() {

        NotificationRequestAcceptedDetailsInt detailsInt = new NotificationRequestAcceptedDetailsInt();
        TimelineElementInternal actual = timelineUtils.buildTimeline(buildNotificationInt(), TimelineElementCategoryInt.REQUEST_ACCEPTED, "001", detailsInt);
        Assertions.assertEquals("001", actual.getIun());
        Assertions.assertEquals("001", actual.getElementId());
        Assertions.assertEquals("pa_02", actual.getPaId());
        Assertions.assertEquals(TimelineElementCategoryInt.REQUEST_ACCEPTED, actual.getCategory());
        Assertions.assertEquals(detailsInt, actual.getDetails());
    }

    @Test
    void buildAcceptedRequestTimelineElement() {
        NotificationInt notification = buildNotification();
        TimelineElementInternal actual = timelineUtils.buildAcceptedRequestTimelineElement(notification, "001");
        String timelineEventIdExpected = "REQUEST_ACCEPTED#IUN_Example_IUN_1234_Test".replace("#", TimelineEventIdBuilder.DELIMITER);
        Assertions.assertAll(
                () -> Assertions.assertEquals("Example_IUN_1234_Test", actual.getIun()),
                () -> Assertions.assertEquals(timelineEventIdExpected, actual.getElementId()),
                () -> Assertions.assertEquals("TEST_PA_ID", actual.getPaId())
        );
    }

    private NotificationSenderInt createSender() {
        return NotificationSenderInt.builder()
                .paId("TEST_PA_ID")
                .paTaxId("TEST_TAX_ID")
                .paDenomination("TEST_PA_DENOMINATION")
                .build();
    }

    private NotificationInt buildNotification() {
        return NotificationInt.builder()
                .sender(createSender())
                .sentAt(Instant.now().minus(Duration.ofDays(1).minus(Duration.ofMinutes(10))))
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
                .recipients(buildRecipients())
                .build();
    }

    private List<NotificationRecipientInt> buildRecipients() {
        NotificationRecipientInt rec1 = NotificationRecipientInt.builder()
                .internalId("internalId")
                .taxId("CDCFSC11R99X001Z")
                .denomination("Galileo Bruno")
                .digitalDomicile(LegalDigitalAddressInt.builder()
                        .address("test@dominioPec.it")
                        .type(LegalDigitalAddressInt.LEGAL_DIGITAL_ADDRESS_TYPE.PEC)
                        .build())
                .physicalAddress(new PhysicalAddressInt(
                        "Galileo Bruno",
                        "Palazzo dell'Inquisizione",
                        "corso Italia 666",
                        "Piano Terra (piatta)",
                        "00100",
                        "Roma",
                        null,
                        "RM",
                        "IT"
                ))
                .build();

        return Collections.singletonList(rec1);
    }

    private PhysicalAddressInt buildPhysicalAddressInt() {
        return PhysicalAddressInt.builder()
                .addressDetails("001")
                .foreignState("002")
                .at("003")
                .province("004")
                .municipality("005")
                .zip("006")
                .municipalityDetails("007")
                .build();
    }

    private NotificationInt buildNotificationInt() {
        return NotificationInt.builder()
                .iun("001")
                .paProtocolNumber("protocol_01")
                .sender(NotificationSenderInt.builder()
                        .paId("pa_02")
                        .build()
                )
                .recipients(Collections.singletonList(
                        NotificationRecipientInt.builder()
                                .taxId("testIdRecipient")
                                .denomination("Nome Cognome/Ragione Sociale")
                                .build()
                ))
                .build();
    }

    @Test
    void checkIsNotificationCancellationNotRequested() {
        String iun = "IUN-checkIsNotificationCancellationNotRequested";

        Mockito.when(timelineService.getTimelineByIunTimelineId(Mockito.eq(iun), Mockito.anyString(), Mockito.eq(false))).thenReturn(new HashSet<>());

        boolean isNotificationCancellationRequested = timelineUtils.checkIsNotificationCancellationRequested(iun);
        Assertions.assertFalse(isNotificationCancellationRequested);
    }

    @Test
    void checkIsNotificationCancellationRequested() {
        String iun = "IUN-checkIsNotificationCancellationRequested";

        Mockito.when(timelineService.getNotificationCancellationRequested(anyString())).thenReturn(Optional.of(Instant.now()));

        boolean isNotificationCancellationRequested = timelineUtils.checkIsNotificationCancellationRequested(iun);
        Assertions.assertTrue(isNotificationCancellationRequested);
    }

    @Test
    void buildNationalRegistryValidationCall() {
        NotificationInt notification = buildNotification();
        String eventId = TimelineEventId.NATIONAL_REGISTRY_VALIDATION_CALL.buildEventId(
                EventId.builder()
                        .iun("Example_IUN_1234_Test")
                        .deliveryMode(DeliveryModeInt.ANALOG)
                        .build());
        List<Integer> recIndexes = new ArrayList<>();
        TimelineElementInternal actual = timelineUtils.buildNationalRegistryValidationCall(eventId, notification, recIndexes, DeliveryModeInt.ANALOG);
        String timelineEventIdExpected = "NATIONAL_REGISTRY_VALIDATION_CALL.IUN_Example_IUN_1234_Test.DELIVERYMODE_ANALOG";

        Assertions.assertAll(
                () -> Assertions.assertEquals("Example_IUN_1234_Test", actual.getIun()),
                () -> Assertions.assertEquals(timelineEventIdExpected, actual.getElementId()),
                () -> Assertions.assertEquals("TEST_PA_ID", actual.getPaId())
        );
    }

    @Test
    void buildNationalRegistryValidationResponse() {
        NotificationInt notification = buildNotification();
        NationalRegistriesResponse response = NationalRegistriesResponse.builder()
                .correlationId("CorrelationId")
                .physicalAddress(buildPhysicalAddressInt())
                .registry("ANPR")
                .recIndex(1)
                .build();
        TimelineElementInternal actual = timelineUtils.buildNationalRegistryValidationResponse(notification, response);
        String timelineEventIdExpected = "NATIONAL_REGISTRY_VALIDATION_RESPONSE.RECINDEX_1.CORRELATIONID_CorrelationId";

        Assertions.assertAll(
                () -> Assertions.assertEquals("Example_IUN_1234_Test", actual.getIun()),
                () -> Assertions.assertEquals(timelineEventIdExpected, actual.getElementId()),
                () -> Assertions.assertEquals("TEST_PA_ID", actual.getPaId())
        );
    }

    @Test
    void buildRefusedRequestTimelineElement_ShouldBuildCorrectTimelineElement() {
        NotificationInt notification = NotificationInt.builder()
                .iun("Test_IUN_123")
                .sender(NotificationSenderInt.builder().paId("TEST_PA_ID").build())
                .recipients(buildRecipients())
                .build();

        List<NotificationRefusedErrorInt> errors = List.of(
                NotificationRefusedErrorInt.builder().errorCode("ADDRESS_SEARCH_FAILED").detail("Address search for recipient index: 0, encountered an error").recIndex(0).build(),
                NotificationRefusedErrorInt.builder().errorCode("ADDRESS_SEARCH_FAILED").detail("Address search for recipient index: 1, encountered an error").recIndex(1).build()
        );

        TimelineElementInternal result = timelineUtils.buildRefusedRequestTimelineElement(notification, errors, 100);

        Assertions.assertAll(
                () -> Assertions.assertEquals("Test_IUN_123", result.getIun()),
                () -> Assertions.assertTrue(result.getElementId().contains("REQUEST_REFUSED")),
                () -> Assertions.assertEquals(TimelineElementCategoryInt.REQUEST_REFUSED, result.getCategory()),
                () -> Assertions.assertNotNull(result.getDetails()),
                () -> Assertions.assertEquals(errors, ((RequestRefusedDetailsInt) result.getDetails()).getRefusalReasons())
        );
    }
}