package it.pagopa.pn.deliverypushvalidator.action.details;


import it.pagopa.pn.deliverypushvalidator.middleware.queue.producer.abstractions.actionspool.ActionDetails;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DocumentCreationResponseActionDetails implements ActionDetails {
    private String key;
    private String documentCreationType;
    private String timelineId;
}
