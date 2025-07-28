package it.pagopa.pn.deliverypushvalidator.dto.timeline.details;

import lombok.*;

import java.time.Instant;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder( toBuilder = true )
@EqualsAndHashCode
@ToString
public class NotificationRADDRetrievedDetailsInt implements RecipientRelatedTimelineElementDetails, ElementTimestampTimelineElementDetails{
    private int recIndex;
    private String raddType;
    private String raddTransactionId;
    private Instant eventTimestamp;
    
    public String toLog() {
        return String.format(
                "recIndex=%d eventTimestamp=%s",
                recIndex,
                eventTimestamp
        );
    }

    @Override
    public Instant getElementTimestamp() {
        return eventTimestamp;
    }
}
