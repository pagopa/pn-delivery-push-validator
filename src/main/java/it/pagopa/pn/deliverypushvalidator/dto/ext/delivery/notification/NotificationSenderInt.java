package it.pagopa.pn.deliverypushvalidator.dto.ext.delivery.notification;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder(toBuilder = true)
@EqualsAndHashCode
@ToString
public class NotificationSenderInt {
    private String paId;
    private String paDenomination;
    private String paTaxId;
}
