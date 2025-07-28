package it.pagopa.pn.deliverypushvalidator.dto.mandate;


import it.pagopa.pn.deliverypushvalidator.dto.ext.notificationpaid.NotificationPaidInt;
import lombok.*;

@Getter
@Setter
@Builder(toBuilder = true)
@ToString
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public class DelegateInfoInt {
    private String internalId;
    private String taxId;
    private String operatorUuid;
    private String mandateId;
    private String denomination;
    private NotificationPaidInt.RecipientTypeInt delegateType;
}
