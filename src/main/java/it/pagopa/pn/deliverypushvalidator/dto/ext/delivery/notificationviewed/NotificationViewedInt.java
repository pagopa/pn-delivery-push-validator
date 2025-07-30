package it.pagopa.pn.deliverypushvalidator.dto.ext.delivery.notificationviewed;

import it.pagopa.pn.deliverypushvalidator.dto.mandate.DelegateInfoInt;
import it.pagopa.pn.deliverypushvalidator.dto.radd.RaddInfo;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Builder
@Getter
public class NotificationViewedInt {
    String iun;
    Integer recipientIndex;
    DelegateInfoInt delegateInfo;
    Instant viewedDate;
    RaddInfo raddInfo;
    String sourceChannel;
    String sourceChannelDetails;
}
