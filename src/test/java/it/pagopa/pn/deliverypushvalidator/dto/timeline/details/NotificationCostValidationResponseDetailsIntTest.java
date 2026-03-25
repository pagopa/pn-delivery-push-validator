package it.pagopa.pn.deliverypushvalidator.dto.timeline.details;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class NotificationCostValidationResponseDetailsIntTest {

    @Test
    void testNoArgsConstructorAndSetters() {
        NotificationCostValidationResponseDetailsInt details = new NotificationCostValidationResponseDetailsInt();
        details.setIun("testIun");
        details.setCategoryType(TimelineElementCategoryInt.NOTIFICATION_COST_VALIDATION_RESPONSE.name());

        assertEquals(TimelineElementCategoryInt.NOTIFICATION_COST_VALIDATION_RESPONSE.name(), details.getCategoryType());
        assertEquals("testIun", details.getIun());
    }

    @Test
    void testBuilderAndToBuilder() {
        NotificationCostValidationResponseDetailsInt details = NotificationCostValidationResponseDetailsInt.builder()
                .iun("testIun")
                .categoryType(TimelineElementCategoryInt.NOTIFICATION_COST_VALIDATION_RESPONSE.name())
                .build();

        assertEquals(TimelineElementCategoryInt.NOTIFICATION_COST_VALIDATION_RESPONSE.name(), details.getCategoryType());

        NotificationCostValidationResponseDetailsInt clonedDetails = details.toBuilder()
                .iun("testIun")
                .categoryType(TimelineElementCategoryInt.NOTIFICATION_COST_VALIDATION_RESPONSE.name())
                .build();

        assertEquals(details.getCategoryType(), clonedDetails.getCategoryType());
    }

    @Test
    void testToLog() {
        NotificationCostValidationResponseDetailsInt details = NotificationCostValidationResponseDetailsInt.builder()
                .iun("testIun")
                .categoryType(TimelineElementCategoryInt.NOTIFICATION_COST_VALIDATION_RESPONSE.name())
                .build();

        assertEquals(
                "iun=testIun, categoryType=NOTIFICATION_COST_VALIDATION_RESPONSE",
                details.toLog()
        );
    }

    @Test
    void testGetElementTimestamp() {
        NotificationCostValidationResponseDetailsInt details = NotificationCostValidationResponseDetailsInt.builder()
                .iun("testIun")
                .categoryType(TimelineElementCategoryInt.NOTIFICATION_COST_VALIDATION_RESPONSE.name())
                .build();

        assertNull(details.getElementTimestamp());
    }
}
