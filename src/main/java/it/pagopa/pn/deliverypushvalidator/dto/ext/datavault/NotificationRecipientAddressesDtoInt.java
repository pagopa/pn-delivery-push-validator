package it.pagopa.pn.deliverypushvalidator.dto.ext.datavault;


import it.pagopa.pn.deliverypushvalidator.dto.address.DigitalAddressInt;
import it.pagopa.pn.deliverypushvalidator.dto.address.PhysicalAddressInt;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
@EqualsAndHashCode
@ToString
public class NotificationRecipientAddressesDtoInt {
    private String denomination;
    private DigitalAddressInt digitalAddress;
    private PhysicalAddressInt physicalAddress;
    private Integer recIndex;
    private Boolean addressNormalized;
}
