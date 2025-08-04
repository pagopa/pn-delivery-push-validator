package it.pagopa.pn.deliverypushvalidator.dto.timeline;

import it.pagopa.pn.deliverypushvalidator.dto.documentcreation.DocumentCreationTypeInt;
import it.pagopa.pn.deliverypushvalidator.dto.timeline.details.ContactPhaseInt;
import it.pagopa.pn.deliverypushvalidator.dto.timeline.details.DeliveryModeInt;
import lombok.*;


@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder(toBuilder = true)
@EqualsAndHashCode
@ToString
public class EventId {
    private String iun;
    private Integer recIndex;
    private ContactPhaseInt contactPhase;
    private Integer sentAttemptMade;
    private DeliveryModeInt deliveryMode;
    private Integer progressIndex;
    private DocumentCreationTypeInt documentCreationType;
    private String creditorTaxId;
    private String noticeCode;
    private Boolean isFirstSendRetry;
    private String relatedTimelineId;
    private Boolean optin;
}
