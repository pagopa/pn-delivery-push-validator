package it.pagopa.pn.deliverypushvalidator.dto.delivery.notification;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder(toBuilder = true)
@EqualsAndHashCode
@ToString
public class UsedServicesInt {
    Boolean physicalAddressLookUp;
}
