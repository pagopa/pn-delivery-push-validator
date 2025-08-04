package it.pagopa.pn.deliverypushvalidator.dto.timeline.details;


import it.pagopa.pn.deliverypushvalidator.utils.AuditLogUtils;
import lombok.*;
import lombok.experimental.SuperBuilder;


@NoArgsConstructor
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@EqualsAndHashCode(callSuper = true)
@ToString
public class ValidateNormalizeAddressDetailsInt extends CategoryTypeTimelineElementDetailsInt implements TimelineElementDetailsInt{
    
    public String toLog() {
        return AuditLogUtils.EMPTY;
    }
}

