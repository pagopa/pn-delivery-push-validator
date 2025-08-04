package it.pagopa.pn.deliverypushvalidator.dto.cost;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder(toBuilder = true)
@EqualsAndHashCode
@ToString
public class PaymentsInfoForRecipientInt {
    private Integer recIndex;
    private String creditorTaxId;
    private String noticeCode;
}
