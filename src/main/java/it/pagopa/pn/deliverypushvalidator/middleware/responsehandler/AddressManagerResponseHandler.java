package it.pagopa.pn.deliverypushvalidator.middleware.responsehandler;

import it.pagopa.pn.deliverypushvalidator.action.startworkflow.notificationvalidation.NotificationValidationActionHandler;
import it.pagopa.pn.deliverypushvalidator.action.utils.TimelineUtils;
import it.pagopa.pn.deliverypushvalidator.dto.ext.addressmanager.NormalizeItemsResultInt;
import it.pagopa.pn.deliverypushvalidator.dto.timeline.CommunicationType;
import it.pagopa.pn.deliverypushvalidator.dto.timeline.TimelineElementInternal;
import it.pagopa.pn.deliverypushvalidator.generated.openapi.msclient.addressmanager.model.NormalizeItemsResult;
import it.pagopa.pn.deliverypushvalidator.middleware.externalclient.pnclient.addressmanager.AddressManagerClient;
import it.pagopa.pn.deliverypushvalidator.middleware.queue.consumer.handler.utils.HandleEventUtils;
import it.pagopa.pn.deliverypushvalidator.service.TimelineService;
import it.pagopa.pn.deliverypushvalidator.service.mapper.AddressManagerMapper;
import lombok.AllArgsConstructor;
import lombok.CustomLog;
import org.springframework.stereotype.Component;

@Component
@CustomLog
@AllArgsConstructor
public class AddressManagerResponseHandler {

    private NotificationValidationActionHandler notificationValidationActionHandler;
    private TimelineUtils timelineUtils;
    private TimelineService timelineService;
    
    public void handleResponseReceived( NormalizeItemsResult response ) {
        String iun = timelineUtils.getIunFromTimelineId(response.getCorrelationId());
        addMdcFilter(iun, response.getCorrelationId());

        log.info("Async response received from service {} for {} with correlationId={}",
                AddressManagerClient.CLIENT_NAME, AddressManagerClient.NORMALIZE_ADDRESS_PROCESS_NAME, response.getCorrelationId());
        final String processName = AddressManagerClient.NORMALIZE_ADDRESS_PROCESS_NAME + " response handler";

        if (timelineUtils.checkIsNotificationCancellationRequested(iun)){
            log.warn("Process {} blocked: cancellation requested for iun {}", processName, iun);
            return;
        }

        try {
            log.logStartingProcess(processName);

            TimelineElementInternal timelineElement = timelineService.getTimelineElement(iun, response.getCorrelationId())
                    .orElseThrow(() -> new IllegalStateException("Timeline element not found for iun " + iun + " and correlationId " + response.getCorrelationId()));
            CommunicationType communicationType = timelineElement.getCommunicationType();

            NormalizeItemsResultInt normalizeItemsResult = AddressManagerMapper.externalToInternal(response);
            notificationValidationActionHandler.handleValidateAndNormalizeAddressResponse(iun, normalizeItemsResult, communicationType);
            log.logEndingProcess(processName);
        } catch (Exception ex){
            log.logEndingProcess(processName, false, ex.getMessage(),ex);
            throw ex;
        }
        
    }

    private static void addMdcFilter(String iun, String correlationId) {
        HandleEventUtils.addIunToMdc(iun);
        HandleEventUtils.addCorrelationIdToMdc(correlationId);
    }
}
