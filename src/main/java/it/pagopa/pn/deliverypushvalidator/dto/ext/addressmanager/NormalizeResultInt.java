package it.pagopa.pn.deliverypushvalidator.dto.ext.addressmanager;


import it.pagopa.pn.deliverypushvalidator.dto.address.PhysicalAddressInt;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@Builder
@EqualsAndHashCode
@ToString
public class NormalizeResultInt {
    private String id;
    private PhysicalAddressInt normalizedAddress;
    private String error;

}
