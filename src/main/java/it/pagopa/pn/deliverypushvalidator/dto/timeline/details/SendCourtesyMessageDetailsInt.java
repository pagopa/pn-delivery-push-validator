package it.pagopa.pn.deliverypushvalidator.dto.timeline.details;

import it.pagopa.pn.deliverypushvalidator.dto.address.CourtesyDigitalAddressInt;
import it.pagopa.pn.deliverypushvalidator.dto.io.IoSendMessageResultInt;
import it.pagopa.pn.deliverypushvalidator.utils.AuditLogUtils;
import lombok.*;

import java.time.Instant;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder(toBuilder = true)
@EqualsAndHashCode
@ToString
public class SendCourtesyMessageDetailsInt implements RecipientRelatedTimelineElementDetails, CourtesyAddressRelatedTimelineElement {
    private int recIndex;
    private CourtesyDigitalAddressInt digitalAddress;
    private Instant sendDate;
    private IoSendMessageResultInt ioSendMessageResult;
    
    public String toLog() {
        return String.format(
                "recIndex=%d addressType=%s digitalAddress=%s",
                recIndex,
                digitalAddress.getType(),
                AuditLogUtils.SENSITIVE
        );
    }
}
