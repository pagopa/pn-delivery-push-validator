package it.pagopa.pn.deliverypushvalidator.middleware.responsehandler;

import it.pagopa.pn.api.dto.events.*;
import it.pagopa.pn.commons.exceptions.PnInternalException;
import it.pagopa.pn.deliverypushvalidator.action.startworkflow.notificationvalidation.NotificationValidationActionHandler;
import it.pagopa.pn.deliverypushvalidator.action.utils.TimelineUtils;
import it.pagopa.pn.deliverypushvalidator.dto.ext.delivery.notification.CommunicationType;
import it.pagopa.pn.deliverypushvalidator.exception.PnDeliveryPushValidatorExceptionCodes;
import it.pagopa.pn.deliverypushvalidator.exception.PnValidationNotValidF24Exception;
import it.pagopa.pn.deliverypushvalidator.middleware.externalclient.pnclient.f24.PnF24Client;
import it.pagopa.pn.deliverypushvalidator.middleware.queue.consumer.handler.utils.HandleEventUtils;
import it.pagopa.pn.deliverypushvalidator.middleware.queue.producer.abstractions.actionspool.ActionType;
import it.pagopa.pn.deliverypushvalidator.service.F24Service;
import it.pagopa.pn.deliverypushvalidator.service.SchedulerService;
import lombok.AllArgsConstructor;
import lombok.CustomLog;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@CustomLog
@AllArgsConstructor
public class F24ResponseHandler {
    private TimelineUtils timelineUtils;
    private NotificationValidationActionHandler validationActionHandler;
    private final F24Service f24Service;
    private final SchedulerService schedulerService;
    private static final String PATH_TOKEN_SEPARATOR = "_";


    public void handleEventF24(DetailedTypePayload event) {
        switch (event) {
            case PnF24MetadataValidationEndEvent.Detail metadataValidationEndEvent -> {
                log.info("Handle event PnF24MetadataValidationEndEvent to handleValidationResponseReceived");
                handleValidationResponseReceived(metadataValidationEndEvent);
            }
            case PnF24PdfSetReadyEvent.Detail pdfSetReadyEvent -> {
                log.info("Handle event PnF24PdfSetReadyEvent to handlePrepareResponseReceived");
                handlePrepareResponseReceived(pdfSetReadyEvent);
            }
            default -> throw new PnInternalException("Invalid type for handleMessageF24", PnDeliveryPushValidatorExceptionCodes.ERROR_CODE_DELIVERYPUSH_INVALIDEVENTCODE);
        }
    }

    private void handleValidationResponseReceived(PnF24MetadataValidationEndEvent.Detail event) {
        if (event.getMetadataValidationEnd() != null) {
            PnF24MetadataValidationEndEventPayload metadataValidationEndEvent = event.getMetadataValidationEnd();
            String iun = metadataValidationEndEvent.getSetId();
            CommunicationType communicationType = CommunicationType.LEGAL;
            HandleEventUtils.addIunAndCommunicationTypeToMdc(iun, communicationType);
            log.info("Async response received from service {} for {} with iun={}",
                    PnF24Client.CLIENT_NAME, PnF24Client.VALIDATE_F24_PROCESS_NAME, event.getMetadataValidationEnd().getSetId());

            final String processName = PnF24Client.VALIDATE_F24_PROCESS_NAME + " response handler";

            if (timelineUtils.checkIsNotificationCancellationRequested(iun)) {
                log.warn("Process {} blocked: cancellation requested for iun {}", processName, iun);
                return;
            }

            try {
                log.logStartingProcess(processName);
                validationActionHandler.handleValidateF24Response(metadataValidationEndEvent, communicationType);
                log.logEndingProcess(processName);
            } catch (Exception ex){
                log.logEndingProcess(processName, false, ex.getMessage(),ex);
                throw ex;
            }
        }else{
            throw new PnValidationNotValidF24Exception("invalid event payload");
        }
    }

    private void handlePrepareResponseReceived(PnF24PdfSetReadyEvent.Detail event){
        PnF24PdfSetReadyEventPayload pdfSetReady = event.getPdfSetReady();

        List<PnF24PdfSetReadyEventItem> generatedPdfsUrls = pdfSetReady.getGeneratedPdfsUrls();
        String timelineId = pdfSetReady.getRequestId();
        String iunFromTimelineId = timelineUtils.getIunFromTimelineId(timelineId);

        log.debug("Start mapping PnF24PdfSetReadyEvent.Detail iun {}",iunFromTimelineId);
        Map<Integer, List<String>> result = generatedPdfsUrls.stream()
                .collect(Collectors.groupingBy(
                        item -> Integer.parseInt(item.getPathTokens().split(PATH_TOKEN_SEPARATOR)[0]),// Estrae recIndex
                        LinkedHashMap::new,
                        Collectors.mapping(PnF24PdfSetReadyEventItem::getUri, Collectors.toList())
                ));


        log.debug("Invoke f24Service.handleF24PrepareResponse for iun {}", iunFromTimelineId);
        f24Service.handleF24PrepareResponse(iunFromTimelineId,result);

        log.debug("scheduleEvent POST_ACCEPTED_PROCESSING_COMPLETED for iun {}", iunFromTimelineId);
        schedulerService.scheduleEvent(iunFromTimelineId, Instant.now(), ActionType.POST_ACCEPTED_PROCESSING_COMPLETED);

    }
}
