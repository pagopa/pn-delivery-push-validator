package it.pagopa.pn.deliverypushvalidator.dto.timeline.details;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class NotificationCostValidationRequestDetailsIntTest {

    @Test
    void testNoArgsConstructorAndSetters() {
        NotificationCostValidationRequestDetailsInt details = new NotificationCostValidationRequestDetailsInt();
        details.setIun("testIun");
        details.setCategoryType(TimelineElementCategoryInt.NOTIFICATION_COST_VALIDATION_REQUEST.name());

        assertEquals(TimelineElementCategoryInt.NOTIFICATION_COST_VALIDATION_REQUEST.name(), details.getCategoryType());
        assertEquals("testIun", details.getIun());
    }

    @Test
    void testBuilderAndToBuilder() {
        NotificationCostValidationRequestDetailsInt details = NotificationCostValidationRequestDetailsInt.builder()
                .iun("testIun")
                .categoryType(TimelineElementCategoryInt.NOTIFICATION_COST_VALIDATION_REQUEST.name())
                .build();

        assertEquals(TimelineElementCategoryInt.NOTIFICATION_COST_VALIDATION_REQUEST.name(), details.getCategoryType());

        NotificationCostValidationRequestDetailsInt clonedDetails = details.toBuilder()
                .categoryType(TimelineElementCategoryInt.NOTIFICATION_COST_VALIDATION_REQUEST.name())
                .build();

        assertEquals(details.getCategoryType(), clonedDetails.getCategoryType());
    }

    @Test
    void testToLog() {
        NotificationCostValidationRequestDetailsInt details = NotificationCostValidationRequestDetailsInt.builder()
                .categoryType(TimelineElementCategoryInt.NOTIFICATION_COST_VALIDATION_REQUEST.name())
                .iun("testIun")
                .build();

        assertEquals(
                "iun=testIun, categoryType=NOTIFICATION_COST_VALIDATION_REQUEST",
                details.toLog()
        );
    }

    @Test
    void testGetElementTimestamp() {
        NotificationCostValidationRequestDetailsInt details = NotificationCostValidationRequestDetailsInt.builder()
                .categoryType(TimelineElementCategoryInt.NOTIFICATION_COST_VALIDATION_REQUEST.name())
                .iun("testIun")
                .build();

        assertNull(details.getElementTimestamp());
    }
}
