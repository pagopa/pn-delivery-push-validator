package it.pagopa.pn.deliverypushvalidator.action.it.mockbean;

import it.pagopa.pn.api.dto.events.notificationcost.utils.ValidationStatus;
import it.pagopa.pn.api.dto.events.notificationcost.validation.PnNotificationCostValidationEventPayload;
import it.pagopa.pn.deliverypushvalidator.action.it.utils.MethodExecutor;
import it.pagopa.pn.deliverypushvalidator.action.startworkflow.notificationvalidation.NotificationValidationActionHandler;
import it.pagopa.pn.deliverypushvalidator.dto.ext.delivery.notification.CommunicationType;
import it.pagopa.pn.deliverypushvalidator.dto.timeline.EventId;
import it.pagopa.pn.deliverypushvalidator.dto.timeline.TimelineEventId;
import it.pagopa.pn.deliverypushvalidator.generated.openapi.msclient.notificationcostservice.model.NewInformalNotificationCostRequest;
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

    public NotificationCostServiceClientMock(@Lazy NotificationValidationActionHandler notificationValidationActionHandler,
                                             @Lazy TimelineService timelineService) {
        this.notificationValidationActionHandler = notificationValidationActionHandler;
        this.timelineService = timelineService;
    }

    public void clear() {
        // pulizia se necessario
    }

    @Override
    public Mono<String> initializeNotificationCost(String iun, NewNotificationCostRequest newNotificationCostRequest) {
        handleAsyncInvocationOfTargetConsumer(iun, CommunicationType.LEGAL);
        return Mono.just(iun);
    }

    @Override
    public Mono<String> initializeInformalNotificationCost(String iun, NewInformalNotificationCostRequest newInformalNotificationCostRequest) {
        handleAsyncInvocationOfTargetConsumer(iun, CommunicationType.INFORMAL);
        return Mono.just(iun);
    }

    private void handleAsyncInvocationOfTargetConsumer(String iun, CommunicationType communicationType) {
        ThreadPool.start(new Thread(() -> {
            MethodExecutor.waitForExecution(
                    () -> timelineService.getTimelineElement(
                            iun,
                            TimelineEventId.NOTIFICATION_COST_VALIDATION_REQUEST.buildEventId(
                                    EventId.builder()
                                            .iun(iun)
                                            .build()
                            )
                    )
            );

            log.info("[TEST] Start handle notification cost validation for iun={} with communicationType={}", iun, communicationType);

            PnNotificationCostValidationEventPayload eventPayload = createEventPayload(iun);

            notificationValidationActionHandler.handleValidateNotificationCost(iun, eventPayload, communicationType);

            log.info("[TEST] END handle informal notification cost validation for iun={} with communicationType={}", iun, communicationType);
        }));
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
