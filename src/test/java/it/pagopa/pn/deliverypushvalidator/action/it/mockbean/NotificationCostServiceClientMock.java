package it.pagopa.pn.deliverypushvalidator.action.it.mockbean;

import it.pagopa.pn.api.dto.events.notificationcost.utils.ValidationStatus;
import it.pagopa.pn.api.dto.events.notificationcost.validation.PnNotificationCostValidationEventPayload;
import it.pagopa.pn.deliverypushvalidator.action.it.utils.MethodExecutor;
import it.pagopa.pn.deliverypushvalidator.action.startworkflow.notificationvalidation.NotificationValidationActionHandler;
import it.pagopa.pn.deliverypushvalidator.action.utils.TimelineUtils;
import it.pagopa.pn.deliverypushvalidator.dto.timeline.TimelineEventId;
import it.pagopa.pn.deliverypushvalidator.generated.openapi.msclient.notificationcostservice.model.NewNotificationCostRequest;
import it.pagopa.pn.deliverypushvalidator.middleware.externalclient.pnclient.notificationcostservice.NotificationCostServiceClient;
import it.pagopa.pn.deliverypushvalidator.service.TimelineService;
import it.pagopa.pn.deliverypushvalidator.utils.ThreadPool;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import reactor.core.publisher.Mono;

@Setter
@Slf4j
public class NotificationCostServiceClientMock implements NotificationCostServiceClient {

    private NotificationValidationActionHandler notificationValidationActionHandler;
    private TimelineService timelineService;
    private TimelineUtils timelineUtils;

    public NotificationCostServiceClientMock(@Lazy NotificationValidationActionHandler notificationValidationActionHandler,
                                             @Lazy TimelineService timelineService,
                                             @Lazy TimelineUtils timelineUtils) {
        this.notificationValidationActionHandler = notificationValidationActionHandler;
        this.timelineService = timelineService;
        this.timelineUtils = timelineUtils;
    }

    public void clear() {
        // pulizia se necessario
    }

    @Override
    public Mono<String> initializeNotificationCost(String iun, NewNotificationCostRequest newNotificationCostRequest) {

        ThreadPool.start(new Thread(() -> {
            MethodExecutor.waitForExecution(
                    () -> timelineService.getTimelineElement(iun, String.valueOf(TimelineEventId.NOTIFICATION_COST_VALIDATION_REQUEST))
            );

            log.info("[TEST] Start handle notification cost validation for iun={}", iun);

            PnNotificationCostValidationEventPayload eventPayload = createEventPayload(iun);

            notificationValidationActionHandler.handleValidateNotificationCost(iun, eventPayload);

            log.info("[TEST] END handle notification cost validation for iun={}", iun);
        }));

        return Mono.just(iun);
    }

    private PnNotificationCostValidationEventPayload createEventPayload(String iun) {
        // L'evento deve avere:
        // - iun: l'identificativo della notifica
        // - status: OK o KO

        return PnNotificationCostValidationEventPayload.builder()
                .iun(iun)
                .status(ValidationStatus.OK)
                .build();
    }

}
