package it.pagopa.pn.deliverypushvalidator.middleware.responsehandler;

import it.pagopa.pn.commons.exceptions.PnInternalException;
import it.pagopa.pn.deliverypushvalidator.action.startworkflow.notificationvalidation.NotificationValidationActionHandler;
import it.pagopa.pn.deliverypushvalidator.action.utils.TimelineUtils;
import it.pagopa.pn.deliverypushvalidator.dto.ext.delivery.notification.CommunicationType;
import it.pagopa.pn.deliverypushvalidator.dto.ext.notificationcost.NotificationCostValidationResponse;
import it.pagopa.pn.deliverypushvalidator.dto.timeline.TimelineElementInternal;
import it.pagopa.pn.deliverypushvalidator.middleware.queue.consumer.handler.utils.HandleEventUtils;
import it.pagopa.pn.deliverypushvalidator.service.TimelineService;
import lombok.AllArgsConstructor;
import lombok.CustomLog;
import org.springframework.stereotype.Component;

import java.util.Optional;

import static it.pagopa.pn.deliverypushvalidator.exception.PnDeliveryPushValidatorExceptionCodes.ERROR_CODE_TIMELINESERVICE_TIMELINE_ELEMENT_NOT_PRESENT;

@Component
@AllArgsConstructor
@CustomLog
public class NotificationCostResponseHandler {
    private final NotificationValidationActionHandler handler;
    private final TimelineService timelineService;

    public void handleResponseReceived(NotificationCostValidationResponse response) {
        String iun = response.getIun();
        log.info("Handle response received for iun {}", iun);
        String requestElementId = TimelineUtils.buildNotificationCostValidationRequestElementId(iun);
        Optional<TimelineElementInternal> optRequestElement = timelineService.getTimelineElement(iun, requestElementId);
        if(optRequestElement.isEmpty()) {
            log.warn("Timeline element not found for iun {} and requestElementId {}", iun, requestElementId);
            throw new PnInternalException("Timeline element not found for iun " + iun + " and requestElementId " + requestElementId, ERROR_CODE_TIMELINESERVICE_TIMELINE_ELEMENT_NOT_PRESENT);
        }

        TimelineElementInternal requestElement = optRequestElement.get();
        CommunicationType communicationType = requestElement.getCommunicationType();
        HandleEventUtils.addCommunicationTypeToMdc(communicationType);
        handler.handleValidateNotificationCost(iun, response, communicationType);
    }
}
