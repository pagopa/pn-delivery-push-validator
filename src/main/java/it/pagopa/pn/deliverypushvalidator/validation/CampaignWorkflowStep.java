package it.pagopa.pn.deliverypushvalidator.validation;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
@EqualsAndHashCode
@ToString
public class CampaignWorkflowStep {
    private String channel;       // IO, PEC, EMAIL, SMS, ANALOG
    private String desiredFeedback; // RECEIVED, READ, PAID, SKIP
    private Boolean includeAttachment;
}

