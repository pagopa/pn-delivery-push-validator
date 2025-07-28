package it.pagopa.pn.deliverypushvalidator.dto.timeline.details;

import it.pagopa.pn.deliverypushvalidator.utils.AuditLogUtils;
import lombok.*;

@NoArgsConstructor
@Getter
@Setter
@Builder(toBuilder = true)
@EqualsAndHashCode
@ToString
public class GenerateF24Int implements TimelineElementDetailsInt{

    public String toLog() {
        return AuditLogUtils.EMPTY;
    }
}
