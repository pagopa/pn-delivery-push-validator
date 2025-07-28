package it.pagopa.pn.deliverypushvalidator.dto.timeline.details;


import it.pagopa.pn.deliverypushvalidator.dto.address.DigitalAddressSourceInt;
import it.pagopa.pn.deliverypushvalidator.dto.address.LegalDigitalAddressInt;
import it.pagopa.pn.deliverypushvalidator.utils.AuditLogUtils;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder(toBuilder = true)
@EqualsAndHashCode
@ToString
public class SendDigitalDetailsInt implements DigitalSendTimelineElementDetails {
    private int recIndex;
    private LegalDigitalAddressInt digitalAddress;
    private DigitalAddressSourceInt digitalAddressSource;
    private Integer retryNumber;
    private DownstreamIdInt downstreamId;
    private Boolean isFirstSendRetry;
    private String relatedFeedbackTimelineId;
    
    public String toLog() {
        return String.format(
                "recIndex=%d source=%s retryNumber=%s digitalAddress=%s isFirstSendRetry=%s",
                recIndex,
                digitalAddressSource,
                retryNumber,
                AuditLogUtils.SENSITIVE,
                isFirstSendRetry
        );
    }
}
