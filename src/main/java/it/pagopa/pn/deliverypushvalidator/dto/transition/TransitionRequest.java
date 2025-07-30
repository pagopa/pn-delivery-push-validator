package it.pagopa.pn.deliverypushvalidator.dto.transition;

import it.pagopa.pn.deliverypushvalidator.dto.ext.delivery.notification.status.NotificationStatusInt;
import it.pagopa.pn.deliverypushvalidator.dto.timeline.details.TimelineElementCategoryInt;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TransitionRequest {

    private NotificationStatusInt fromStatus;
    private TimelineElementCategoryInt timelineRowType;
    private boolean multiRecipient;
}
