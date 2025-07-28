package it.pagopa.pn.deliverypushvalidator.dto.timeline.details;

import it.pagopa.pn.deliverypushvalidator.dto.address.DigitalAddressSourceInt;
import it.pagopa.pn.deliverypushvalidator.dto.address.LegalDigitalAddressInt;
import lombok.*;
import java.time.Instant;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder(toBuilder = true)
@EqualsAndHashCode
@ToString
public class ScheduleDigitalWorkflowDetailsInt implements RecipientRelatedTimelineElementDetails, DigitalAddressRelatedTimelineElement {
    private int recIndex;
    private LegalDigitalAddressInt digitalAddress;
    private DigitalAddressSourceInt digitalAddressSource;
    private Integer sentAttemptMade;
    private Instant lastAttemptDate;
    private Instant schedulingDate;


    public String toLog() {
        return String.format(
                "recIndex=%d sentAttemptMade=%d",
                recIndex,
                sentAttemptMade
        );
    }
}
