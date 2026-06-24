package it.pagopa.pn.deliverypushvalidator.dto.campaign;

import it.pagopa.pn.commons.utils.qr.models.RecipientTypeInt;
import lombok.*;

import java.time.Duration;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class WorkflowEntity {
    private Channel channel;
    private RecipientTypeInt recipientType;
    private Duration timeout;
    private DesiredFeedback desiredFeedback;
    private Boolean includeAttachment;
}
