package it.pagopa.pn.deliverypushvalidator.dto.cost;

import lombok.*;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder(toBuilder = true)
@EqualsAndHashCode
@ToString
public class UpdateNotificationCostResponseInt {
    private List<UpdateNotificationCostResultInt> updateResults;
}
