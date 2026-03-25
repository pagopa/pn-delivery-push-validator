package it.pagopa.pn.deliverypushvalidator.dto.timeline.details;

import lombok.*;
import lombok.experimental.SuperBuilder;

@NoArgsConstructor
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@EqualsAndHashCode(callSuper = true)
@ToString
public class NotificationCostValidationResponseDetailsInt extends CategoryTypeTimelineElementDetailsInt implements TimelineElementDetailsInt {
    public String toLog() {
        return String.format("categoryType=%s", getCategoryType());
    }
}
