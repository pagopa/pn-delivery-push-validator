package it.pagopa.pn.deliverypushvalidator.dto.timeline.details;

import it.pagopa.pn.deliverypushvalidator.dto.timeline.NotificationRefusedErrorInt;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@EqualsAndHashCode(callSuper = true)
@ToString
public class RequestRefusedDetailsInt extends CategoryTypeTimelineElementDetailsInt implements TimelineElementDetailsInt {
    private List<NotificationRefusedErrorInt> refusalReasons;
    private Integer numberOfRecipients;
    private Integer notificationCost;
    private String notificationRequestId;
    private String paProtocolNumber;
    private String idempotenceToken;

    public String toLog() {
        return String.format(
                "errors=%s, notificationRequestId=%s, paProtocolNumber=%s, idempotenceToken=%s",
                refusalReasons,
                notificationRequestId,
                paProtocolNumber,
                idempotenceToken
        );
    }
}
