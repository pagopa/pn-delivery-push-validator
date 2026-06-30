package it.pagopa.pn.deliverypushvalidator.dto.campaign;

import lombok.*;

@EqualsAndHashCode
@ToString
@Getter
@Setter
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class WorkflowEntity {
    private Channel channel;
    private DesiredFeedback desiredFeedback;
    private boolean includeAttachment;
}
