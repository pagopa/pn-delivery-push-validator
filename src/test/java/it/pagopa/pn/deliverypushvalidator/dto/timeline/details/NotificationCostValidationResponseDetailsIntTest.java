package it.pagopa.pn.deliverypushvalidator.dto.timeline.details;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class NotificationCostValidationResponseDetailsIntTest {

    @Test
    void testNoArgsConstructorAndSetters() {
        NotificationCostValidationResponseDetailsInt details = new NotificationCostValidationResponseDetailsInt();
        details.setCategoryType(TimelineElementCategoryInt.NOTIFICATION_COST_VALIDATION_RESPONSE.name());

        assertEquals(TimelineElementCategoryInt.NOTIFICATION_COST_VALIDATION_RESPONSE.name(), details.getCategoryType());
    }

    @Test
    void testBuilderAndToBuilder() {
        NotificationCostValidationResponseDetailsInt details = NotificationCostValidationResponseDetailsInt.builder()
                .categoryType(TimelineElementCategoryInt.NOTIFICATION_COST_VALIDATION_RESPONSE.name())
                .build();

        assertEquals(TimelineElementCategoryInt.NOTIFICATION_COST_VALIDATION_RESPONSE.name(), details.getCategoryType());

        NotificationCostValidationResponseDetailsInt clonedDetails = details.toBuilder()
                .categoryType(TimelineElementCategoryInt.NOTIFICATION_COST_VALIDATION_RESPONSE.name())
                .build();

        assertEquals(details.getCategoryType(), clonedDetails.getCategoryType());
    }

    @Test
    void testToLog() {
        NotificationCostValidationResponseDetailsInt details = NotificationCostValidationResponseDetailsInt.builder()
                .categoryType(TimelineElementCategoryInt.NOTIFICATION_COST_VALIDATION_RESPONSE.name())
                .build();

        assertEquals(
                "categoryType=NOTIFICATION_COST_VALIDATION_RESPONSE",
                details.toLog()
        );
    }
}
