package it.pagopa.pn.deliverypushvalidator.dto.timeline.details;


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
public class NormalizedAddressDetailsInt extends CategoryTypeTimelineElementDetailsInt implements RecipientRelatedTimelineElementDetails, NewAddressRelatedTimelineElement, PhysicalAddressRelatedTimelineElement{
    private int recIndex;
    private PhysicalAddressInt oldAddress;
    private PhysicalAddressInt normalizedAddress;

    public String toLog() {
        return String.format(
                "recIndex=%d oldPhysicalAddress%s normalizedAddress=%s",
                recIndex,
                AuditLogUtils.SENSITIVE,
                AuditLogUtils.SENSITIVE
        );
    }

    @Override
    public PhysicalAddressInt getNewAddress() {
        return normalizedAddress;
    }

    @Override
    public void setNewAddress(PhysicalAddressInt physicalAddress) {
        this.normalizedAddress = physicalAddress;
    }

    @Override
    public PhysicalAddressInt getPhysicalAddress() {
        return oldAddress;
    }

    @Override
    public void setPhysicalAddress(PhysicalAddressInt physicalAddressInt) {
        this.oldAddress = physicalAddressInt;
    }
}