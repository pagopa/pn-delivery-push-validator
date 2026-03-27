package it.pagopa.pn.deliverypushvalidator.service.impl;

import it.pagopa.pn.deliverypushvalidator.action.utils.TimelineUtils;
import it.pagopa.pn.deliverypushvalidator.dto.ext.delivery.notification.NotificationInt;
import it.pagopa.pn.deliverypushvalidator.dto.timeline.TimelineElementInternal;
import it.pagopa.pn.deliverypushvalidator.middleware.externalclient.pnclient.notificationcostservice.NotificationCostServiceClient;
import it.pagopa.pn.deliverypushvalidator.service.NotificationCostService;
import it.pagopa.pn.deliverypushvalidator.service.TimelineService;
import it.pagopa.pn.deliverypushvalidator.service.mapper.NotificationCostServiceMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@AllArgsConstructor
@Service
public class NotificationCostServiceImpl implements NotificationCostService {

    private final NotificationCostServiceClient notificationCostServiceClient;
    private final TimelineUtils timelineUtils;
    private final NotificationCostServiceMapper notificationCostServiceMapper;
    private final TimelineService timelineService;

    @Override
    public void initializeNotificationCost(NotificationInt notificationInt) {

        TimelineElementInternal buildNotificationCostValidationRequest = timelineUtils.buildNotificationCostValidationRequest(notificationInt);

        log.debug("Invoke initializeNotificationCost elementId: {}", buildNotificationCostValidationRequest.getElementId());

        try {
            notificationCostServiceClient.initializeNotificationCost(notificationInt.getIun(),
                            notificationCostServiceMapper.mapNotificationToRequest(notificationInt))
                    .block();

            timelineService.addTimelineElement(buildNotificationCostValidationRequest, notificationInt);

        } catch (Exception e) {
            log.error("Failed to initialize notification cost for iun: {}", notificationInt.getIun(), e);
            throw e;
        }
    }

}
