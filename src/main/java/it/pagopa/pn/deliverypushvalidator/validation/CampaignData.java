package it.pagopa.pn.deliverypushvalidator.validation;

import lombok.*;

import java.time.Instant;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder(toBuilder = true)
@EqualsAndHashCode
@ToString
public class CampaignData {
    private String campaignId;
    private String senderId;
    private String title;
    private String description;
    private Instant startDate;
    private Instant endDate;
    private Boolean closed;
    private Boolean sensitiveContent;
    private Boolean stopOnViewed;
    private List<CampaignMessageRef> messages;
    private List<CampaignWorkflowStep> workflow;
}

