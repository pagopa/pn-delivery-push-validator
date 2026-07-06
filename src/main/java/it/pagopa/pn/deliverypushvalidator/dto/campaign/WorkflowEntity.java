package it.pagopa.pn.deliverypushvalidator.dto.campaign;

import it.pagopa.pn.deliverypushvalidator.dto.ext.datavault.RecipientTypeInt;
import lombok.*;

import java.time.Duration;
import java.util.Set;

@EqualsAndHashCode
@ToString
@Getter
@Setter
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class WorkflowEntity {
    private Channel channel;
    private Set<RecipientTypeInt> recipientType;
    private Duration timeout;
    private Set<DesiredFeedback> desiredFeedback;
    private Boolean includeAttachment;
}
