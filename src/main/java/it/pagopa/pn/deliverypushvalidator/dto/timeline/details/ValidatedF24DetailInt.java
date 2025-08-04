package it.pagopa.pn.deliverypushvalidator.dto.timeline.details;

import lombok.*;
import lombok.experimental.SuperBuilder;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@EqualsAndHashCode(callSuper = true)
@ToString
public class ValidatedF24DetailInt extends CategoryTypeTimelineElementDetailsInt implements TimelineElementDetailsInt{
    private String status;

    public String toLog() {
        return String.format(
                "status=%s ",
                status
        );
    }
}