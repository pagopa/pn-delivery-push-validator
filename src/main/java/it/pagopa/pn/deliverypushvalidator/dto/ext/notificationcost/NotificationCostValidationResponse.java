package it.pagopa.pn.deliverypushvalidator.dto.ext.notificationcost;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder(toBuilder = true)
@EqualsAndHashCode
@ToString
public class NotificationCostValidationResponse {
    private String iun;
    private ValidationStatus status;
}
