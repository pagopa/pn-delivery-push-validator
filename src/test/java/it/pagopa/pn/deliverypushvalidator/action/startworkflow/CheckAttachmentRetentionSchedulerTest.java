package it.pagopa.pn.deliverypushvalidator.action.startworkflow;

import it.pagopa.pn.deliverypushvalidator.config.PnDeliveryPushValidatorConfigs;
import it.pagopa.pn.deliverypushvalidator.dto.timeline.CommunicationType;
import it.pagopa.pn.deliverypushvalidator.middleware.queue.producer.abstractions.actionspool.ActionType;
import it.pagopa.pn.deliverypushvalidator.middleware.queue.producer.abstractions.actionspool.impl.TimeParams;
import it.pagopa.pn.deliverypushvalidator.service.SchedulerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;

import java.time.Duration;
import java.time.Instant;
import java.util.EnumMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class CheckAttachmentRetentionSchedulerTest {

    @Mock
    private SchedulerService schedulerService;

    @Mock
    private PnDeliveryPushValidatorConfigs configs;

    private CheckAttachmentRetentionScheduler checkAttachmentRetentionScheduler;

    @BeforeEach
    void setup() {
        checkAttachmentRetentionScheduler = new CheckAttachmentRetentionScheduler(schedulerService, configs);
    }

    @Test
    void scheduleCheckAttachmentRetentionBeforeExpirationUsesCommunicationTypeConfig() {
        TimeParams legalParams = new TimeParams();
        legalParams.setAttachmentRetentionTimeAfterValidation(Duration.ofDays(120));
        legalParams.setCheckAttachmentTimeBeforeExpiration(Duration.ofDays(10));

        TimeParams informalParams = new TimeParams();
        informalParams.setAttachmentRetentionTimeAfterValidation(Duration.ofDays(30));
        informalParams.setCheckAttachmentTimeBeforeExpiration(Duration.ofDays(5));

        Map<CommunicationType, TimeParams> map = new EnumMap<>(CommunicationType.class);
        map.put(CommunicationType.LEGAL, legalParams);
        map.put(CommunicationType.INFORMAL, informalParams);
        Mockito.when(configs.getTimeParamsMap()).thenReturn(map);

        checkAttachmentRetentionScheduler.scheduleCheckAttachmentRetentionBeforeExpiration("IUN_123", CommunicationType.INFORMAL);

        ArgumentCaptor<Instant> dateCaptor = ArgumentCaptor.forClass(Instant.class);
        Mockito.verify(schedulerService).scheduleEvent(
                Mockito.eq("IUN_123"),
                dateCaptor.capture(),
                Mockito.eq(ActionType.CHECK_ATTACHMENT_RETENTION),
                Mockito.isNull(),
                Mockito.eq(CommunicationType.INFORMAL)
        );

        Instant scheduledDate = dateCaptor.getValue();
        Instant expectedMin = Instant.now().plus(Duration.ofDays(25)).minusSeconds(5);
        Instant expectedMax = Instant.now().plus(Duration.ofDays(25)).plusSeconds(5);
        assertTrue(!scheduledDate.isBefore(expectedMin) && !scheduledDate.isAfter(expectedMax));
    }
}


