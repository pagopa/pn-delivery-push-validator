package it.pagopa.pn.deliverypushvalidator.service.impl;

import it.pagopa.pn.commons.exceptions.PnInternalException;
import it.pagopa.pn.deliverypushvalidator.action.utils.TimelineUtils;
import it.pagopa.pn.deliverypushvalidator.dto.ext.delivery.notification.CommunicationType;
import it.pagopa.pn.deliverypushvalidator.dto.ext.delivery.notification.NotificationInt;
import it.pagopa.pn.deliverypushvalidator.dto.ext.delivery.notification.NotificationRecipientInt;
import it.pagopa.pn.deliverypushvalidator.dto.timeline.TimelineElementInternal;
import it.pagopa.pn.deliverypushvalidator.middleware.externalclient.pnclient.notificationcostservice.NotificationCostServiceClient;
import it.pagopa.pn.deliverypushvalidator.service.NotificationCostService;
import it.pagopa.pn.deliverypushvalidator.service.TimelineService;
import it.pagopa.pn.deliverypushvalidator.service.mapper.NotificationCostServiceMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Objects;

import static it.pagopa.pn.deliverypushvalidator.exception.PnDeliveryPushValidatorExceptionCodes.ERROR_CODE_DELIVERYPUSH_INCOHERENT_COMMUNICATION_TYPE;

@Slf4j
@AllArgsConstructor
@Service
public class NotificationCostServiceImpl implements NotificationCostService {

    private final NotificationCostServiceClient notificationCostServiceClient;
    private final TimelineUtils timelineUtils;
    private final NotificationCostServiceMapper notificationCostServiceMapper;
    private final TimelineService timelineService;

    @Override
    public void initializeAndValidateNotificationCost(NotificationInt notificationInt) {

        TimelineElementInternal buildNotificationCostValidationRequest = timelineUtils.buildNotificationCostValidationRequest(notificationInt);

        log.debug("Invoke initializeNotificationCost elementId: {}", buildNotificationCostValidationRequest.getElementId());

        try {
            switch(notificationInt.getCommunicationType()) {
                case LEGAL -> notificationCostServiceClient.initializeNotificationCost(notificationInt.getIun(),
                                notificationCostServiceMapper.mapNotificationToNewNotificationCostRequest(notificationInt))
                        .block();
                case INFORMAL -> notificationCostServiceClient.initializeInformalNotificationCost(notificationInt.getIun(),
                                notificationCostServiceMapper.mapNotificationToNewInformalNotificationCostRequest(notificationInt))
                        .block();
                default -> {
                    String errorMessage = String.format("Incoherent communicationType %s for notification %s", notificationInt.getCommunicationType(), notificationInt.getIun());
                    throw new PnInternalException(errorMessage, ERROR_CODE_DELIVERYPUSH_INCOHERENT_COMMUNICATION_TYPE);
                }
            }


            timelineService.addTimelineElement(buildNotificationCostValidationRequest, notificationInt);

        } catch (Exception e) {
            log.error("Failed to initialize notification cost for iun: {}", notificationInt.getIun(), e);
            throw e;
        }
    }

    @Override
    public boolean shouldValidateInformalNotificationCost(NotificationInt notificationInt) {
        if (notificationInt.getCommunicationType() != CommunicationType.INFORMAL) {
            throw new PnInternalException(
                    "CommunicationType is not INFORMAL for iun: " + notificationInt.getIun(),
                    ERROR_CODE_DELIVERYPUSH_INCOHERENT_COMMUNICATION_TYPE
            );
        }

        // Exists at least one recipient with a payment method of type pagoPA
        return notificationInt.getRecipients().stream()
                .map(NotificationRecipientInt::getPayments)
                .filter(Objects::nonNull)
                .flatMap(Collection::stream)
                .anyMatch(payment -> payment.getPagoPA() != null);
    }

}
