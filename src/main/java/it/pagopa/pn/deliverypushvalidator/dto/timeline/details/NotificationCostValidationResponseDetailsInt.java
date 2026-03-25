package it.pagopa.pn.deliverypushvalidator.dto.timeline.details;

import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.Instant;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@EqualsAndHashCode(callSuper = true)
@ToString
public class NotificationCostValidationResponseDetailsInt extends CategoryTypeTimelineElementDetailsInt implements ElementTimestampTimelineElementDetails {
    private String categoryType;
    private String iun;

    public String toLog() {
        return String.format("iun=%s, categoryType=%s", iun,categoryType);
    }

    @Override
    public Instant getElementTimestamp() {
        return null;
    }
}
