package it.pagopa.pn.deliverypushvalidator.dto.ext.addressmanager;

import lombok.*;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@Builder
@EqualsAndHashCode
@ToString
public class NormalizeItemsResultInt {
    private String correlationId;
    private List<NormalizeResultInt> resultItems;

}
