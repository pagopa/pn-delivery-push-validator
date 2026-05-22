package it.pagopa.pn.deliverypushvalidator.action.startworkflow;

import it.pagopa.pn.deliverypushvalidator.config.PnDeliveryPushValidatorConfigs;
import it.pagopa.pn.deliverypushvalidator.dto.ext.delivery.notification.CommunicationType;
import it.pagopa.pn.deliverypushvalidator.middleware.queue.producer.abstractions.actionspool.ActionType;
import it.pagopa.pn.deliverypushvalidator.middleware.queue.producer.abstractions.actionspool.impl.TimeParams;
import it.pagopa.pn.deliverypushvalidator.service.SchedulerService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;

@Component
@AllArgsConstructor
@Slf4j
public class CheckAttachmentRetentionScheduler {

    private final SchedulerService schedulerService;
    private final PnDeliveryPushValidatorConfigs configs;

    public void scheduleCheckAttachmentRetentionBeforeExpiration(String iun, CommunicationType communicationType) {
        Map<CommunicationType, TimeParams> timeParamsMap = configs.getTimeParamsMap();
        TimeParams timeParams = timeParamsMap != null ? timeParamsMap.get(communicationType) : null;
        if (timeParams == null) {
            throw new IllegalStateException("Missing time params configuration for communicationType=" + communicationType);
        }

        Duration attachmentRetentionTimeAfterValidation = timeParams.getAttachmentRetentionTimeAfterValidation();
        Duration checkAttachmentTimeBeforeExpiration = timeParams.getCheckAttachmentTimeBeforeExpiration();

        log.info("Start scheduleCheckAttachmentRetentionBeforeExpiration - attachmentRetentionDaysAfterValidation={} checkAttachmentDaysBeforeExpiration={} iun={} communicationType={}",
                attachmentRetentionTimeAfterValidation, checkAttachmentTimeBeforeExpiration, iun, communicationType);

        Duration checkAttachmentTimeToWait = attachmentRetentionTimeAfterValidation.minus(checkAttachmentTimeBeforeExpiration);
        Instant checkAttachmentDate = Instant.now().plus(checkAttachmentTimeToWait);

        log.info("Scheduling checkAttachmentRetention schedulingDate={} - iun={} communicationType={}", checkAttachmentDate, iun, communicationType);
        schedulerService.scheduleEvent(iun, checkAttachmentDate, ActionType.CHECK_ATTACHMENT_RETENTION, null, communicationType);
    }
}

