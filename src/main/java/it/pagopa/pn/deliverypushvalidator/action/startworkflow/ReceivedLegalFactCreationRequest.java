package it.pagopa.pn.deliverypushvalidator.action.startworkflow;

import it.pagopa.pn.deliverypushvalidator.action.startworkflow.notificationvalidation.AttachmentUtils;
import it.pagopa.pn.deliverypushvalidator.action.utils.TimelineUtils;
import it.pagopa.pn.deliverypushvalidator.dto.ext.delivery.notification.NotificationInt;
import it.pagopa.pn.deliverypushvalidator.dto.documentcreation.DocumentCreationTypeInt;
import it.pagopa.pn.deliverypushvalidator.dto.ext.delivery.notification.CommunicationType;
import it.pagopa.pn.deliverypushvalidator.dto.timeline.TimelineElementInternal;
import it.pagopa.pn.deliverypushvalidator.service.*;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
@Slf4j
public class ReceivedLegalFactCreationRequest {
    private final SaveLegalFactsService saveLegalFactsService;
    private final DocumentCreationRequestService documentCreationRequestService;
    private final TimelineService timelineService;
    private final TimelineUtils timelineUtils;
    private final AttachmentUtils attachmentUtils;
    private final NotificationService notificationService;
    private final CheckAttachmentRetentionScheduler checkAttachmentRetentionScheduler;
    
    
    public void saveNotificationReceivedLegalFacts(String iun) {
        NotificationInt notification = notificationService.getNotificationByIun(iun);

        // cambio lo stato degli attachment in ATTACHED e schedulo la verifica retention degli attachment prima che la stessa scada
        checkAttachmentRetentionScheduler.scheduleCheckAttachmentRetentionBeforeExpiration(iun, CommunicationType.LEGAL);
        attachmentUtils.changeAttachmentsStatusToAttached(notification);

        // Invio richiesta di creazione di atto opponibile a terzi di avvenuta ricezione da parte di PN a SafeStorage
        String legalFactId = saveLegalFactsService.sendCreationRequestForNotificationReceivedLegalFact(notification);

        TimelineElementInternal timelineElementInternal = timelineUtils.buildSenderAckLegalFactCreationRequest(notification, legalFactId);
        addTimelineElement( timelineElementInternal , notification);
        
        //Vengono inserite le informazioni della richiesta di creazione del legalFacts a safeStorage
        documentCreationRequestService.addDocumentCreationRequest(legalFactId, notification.getIun(), DocumentCreationTypeInt.SENDER_ACK, timelineElementInternal.getElementId());
    }

    private void addTimelineElement(TimelineElementInternal element, NotificationInt notification) {
        timelineService.addTimelineElement(element, notification);
    }
}
