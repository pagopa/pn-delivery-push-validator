package it.pagopa.pn.deliverypushvalidator.dto.timeline.details;


import it.pagopa.pn.deliverypushvalidator.dto.address.LegalDigitalAddressInt;
import it.pagopa.pn.deliverypushvalidator.dto.address.PhysicalAddressInt;
import it.pagopa.pn.deliverypushvalidator.utils.AuditLogUtils;
import lombok.*;
import lombok.experimental.SuperBuilder;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@EqualsAndHashCode(callSuper = true)
@ToString
public class PublicRegistryResponseDetailsInt extends CategoryTypeTimelineElementDetailsInt implements RecipientRelatedTimelineElementDetails, DigitalAddressRelatedTimelineElement {
    protected int recIndex;
    protected LegalDigitalAddressInt digitalAddress;
    protected PhysicalAddressInt physicalAddress;
    protected String requestTimelineId;

    public String toLog() {
        return String.format(
                "recIndex=%d digitalAddress=%s physicalAddress=%s requestTimelineId=%s",
                recIndex,
                AuditLogUtils.SENSITIVE,
                AuditLogUtils.SENSITIVE,
                requestTimelineId
        );
    }
}
