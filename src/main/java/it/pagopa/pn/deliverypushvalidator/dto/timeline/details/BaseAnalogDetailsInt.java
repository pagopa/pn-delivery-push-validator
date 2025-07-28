package it.pagopa.pn.deliverypushvalidator.dto.timeline.details;

import it.pagopa.pn.deliverypushvalidator.dto.address.PhysicalAddressInt;
import it.pagopa.pn.deliverypushvalidator.utils.AuditLogUtils;
import lombok.*;
import lombok.experimental.SuperBuilder;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@SuperBuilder( toBuilder = true )
@EqualsAndHashCode
@ToString
public class BaseAnalogDetailsInt implements RecipientRelatedTimelineElementDetails, PhysicalAddressRelatedTimelineElement {

    protected int recIndex;
    protected PhysicalAddressInt physicalAddress;
    protected ServiceLevelInt serviceLevel;
    protected Integer sentAttemptMade;
    protected String relatedRequestId;

    public String toLog() {
        return String.format(
                "recIndex=%d sentAttemptMade=%d relatedRequestId=%s physicalAddress=%s",
                recIndex,
                sentAttemptMade,
                relatedRequestId,
                AuditLogUtils.SENSITIVE
        );
    }

}
