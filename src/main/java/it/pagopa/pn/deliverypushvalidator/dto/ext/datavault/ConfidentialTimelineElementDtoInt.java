package it.pagopa.pn.deliverypushvalidator.dto.ext.datavault;


import it.pagopa.pn.deliverypushvalidator.dto.address.PhysicalAddressInt;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder(toBuilder = true)
@EqualsAndHashCode
@ToString
public class ConfidentialTimelineElementDtoInt {
    private String timelineElementId;
    private String taxId;
    private String denomination;
    private String digitalAddress;
    private PhysicalAddressInt physicalAddress;
    private PhysicalAddressInt newPhysicalAddress;
}
