package it.pagopa.pn.deliverypushvalidator.dto.timeline.details;

import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.Instant;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@EqualsAndHashCode(callSuper = true)
@ToString
public class PublicRegistryValidationCallDetailsInt extends CategoryTypeTimelineElementDetailsInt implements TimelineElementDetailsInt{

    private List<Integer> recIndexes;
    private DeliveryModeInt deliveryMode;
    private Instant sendDate;

    @Override
    public String toLog() {
        return String.format(
                "recIndexes=%s deliveryMode=%s sendDate=%s",
                recIndexes.toString(),
                deliveryMode,
                sendDate
        );
    }
}
