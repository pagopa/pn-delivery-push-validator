package it.pagopa.pn.deliverypushvalidator.service;

import it.pagopa.pn.deliverypushvalidator.action.utils.EndWorkflowStatus;
import it.pagopa.pn.deliverypushvalidator.dto.delivery.notification.NotificationInt;
import it.pagopa.pn.deliverypushvalidator.dto.delivery.notification.NotificationRecipientInt;
import it.pagopa.pn.deliverypushvalidator.dto.legalfacts.PdfInfo;
import it.pagopa.pn.deliverypushvalidator.dto.mandate.DelegateInfoInt;
import it.pagopa.pn.deliverypushvalidator.dto.timeline.details.SendDigitalFeedbackDetailsInt;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.List;

public interface SaveLegalFactsService {
    PdfInfo sendCreationRequestForAAR(NotificationInt notification, NotificationRecipientInt recipient, String quickAccessToken);

    String sendCreationRequestForNotificationReceivedLegalFact(NotificationInt notification);

    String sendCreationRequestForPecDeliveryWorkflowLegalFact(
            List<SendDigitalFeedbackDetailsInt> listFeedbackFromExtChannel,
            NotificationInt notification,
            NotificationRecipientInt recipient,
            EndWorkflowStatus status,
            Instant completionWorkflowDate
    );

    String sendCreationRequestForAnalogDeliveryFailureWorkflowLegalFact(
            NotificationInt notification,
            NotificationRecipientInt recipient,
            EndWorkflowStatus status,
            Instant failureWorkflowDate
    );

    Mono<String> sendCreationRequestForNotificationViewedLegalFact(
            NotificationInt notification,
            NotificationRecipientInt recipient,
            DelegateInfoInt delegateInfo,
            Instant timeStamp
    );

    String sendCreationRequestForNotificationCancelledLegalFact(NotificationInt notification, Instant notificationCancellationRequestDate);
}
