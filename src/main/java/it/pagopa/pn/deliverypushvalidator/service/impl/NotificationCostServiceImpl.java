package it.pagopa.pn.deliverypushvalidator.service.impl;

import it.pagopa.pn.deliverypushvalidator.action.utils.TimelineUtils;
import it.pagopa.pn.deliverypushvalidator.dto.ext.delivery.notification.NotificationInt;
import it.pagopa.pn.deliverypushvalidator.dto.timeline.TimelineElementInternal;
import it.pagopa.pn.deliverypushvalidator.middleware.externalclient.pnclient.notificationcostservice.NotificationCostServiceClient;
import it.pagopa.pn.deliverypushvalidator.service.NotificationCostService;
import it.pagopa.pn.deliverypushvalidator.service.TimelineService;
import it.pagopa.pn.deliverypushvalidator.service.mapper.NotificationCostServiceMapper;
import it.pagopa.pn.deliverypushvalidator.utils.NotificationCostServiceFeatureFlagUtils;
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
    private final NotificationCostServiceFeatureFlagUtils notificationCostServiceFeatureFlagUtils;

    @Override
    public void initializeAndValidateNotificationCost(NotificationInt notificationInt) {

        TimelineElementInternal buildNotificationCostValidationRequest = timelineUtils.buildNotificationCostValidationRequest(notificationInt);

        log.debug("Invoke initializeNotificationCost elementId: {}", buildNotificationCostValidationRequest.getElementId());

        try {
            if(notificationCostServiceFeatureFlagUtils.checkNotificationCostServiceStartDate(notificationInt)){
                notificationCostServiceClient.initializeNotificationCost(notificationInt.getIun(),
                                notificationCostServiceMapper.mapNotificationToRequest(notificationInt))
                        .block();

                timelineService.addTimelineElement(buildNotificationCostValidationRequest, notificationInt);
            } else {
                log.info("NotificationCostService is not enabled for iun: {}. Skipping initialization.", notificationInt.getIun());
            }

        } catch (Exception e) {
            log.error("Failed to initialize notification cost for iun: {}", notificationInt.getIun(), e);
            throw e;
        }
    }

}
