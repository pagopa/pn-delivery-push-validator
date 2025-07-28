package it.pagopa.pn.deliverypushvalidator.action.details;


import it.pagopa.pn.deliverypushvalidator.dto.address.DigitalAddressInfoSentAttempt;
import it.pagopa.pn.deliverypushvalidator.middleware.queue.producer.abstractions.actionspool.ActionDetails;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class SendDigitalFinalStatusResponseDetails implements ActionDetails {
  private DigitalAddressInfoSentAttempt lastAttemptAddressInfo;
  private Boolean isFirstSendRetry;
  private String alreadyPresentRelatedFeedbackTimelineId;
}
