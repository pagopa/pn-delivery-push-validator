package it.pagopa.pn.deliverypushvalidator.action.startworkflow.notificationvalidation;

import it.pagopa.pn.commons.log.PnAuditLogEvent;
import it.pagopa.pn.commons.log.PnAuditLogEventType;
import it.pagopa.pn.deliverypushvalidator.action.details.NotificationValidationActionDetails;
import it.pagopa.pn.deliverypushvalidator.action.startworkflow.LookupAddressHandler;
import it.pagopa.pn.deliverypushvalidator.action.startworkflow.NormalizeAddressHandler;
import it.pagopa.pn.deliverypushvalidator.action.utils.TimelineUtils;
import it.pagopa.pn.deliverypushvalidator.config.PnDeliveryPushValidatorConfigs;
import it.pagopa.pn.deliverypushvalidator.dto.ext.delivery.notification.*;
import it.pagopa.pn.deliverypushvalidator.exception.PnValidationCampaignException;
import it.pagopa.pn.deliverypushvalidator.exception.PnValidationMessageException;
import it.pagopa.pn.deliverypushvalidator.service.*;
import it.pagopa.pn.deliverypushvalidator.validation.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class InformalNotificationValidationStrategyTest {

    @Mock private AttachmentUtils attachmentUtils;
    @Mock private CampaignValidator campaignValidator;
    @Mock private MessageValidator messageValidator;
    @Mock private TimelineUtils timelineUtils;
    @Mock private NotificationService notificationService;
    @Mock private NotificationValidationScheduler notificationValidationScheduler;
    @Mock private AddressValidator addressValidator;
    @Mock private AuditLogService auditLogService;
    @Mock private NormalizeAddressHandler normalizeAddressHandler;
    @Mock private SchedulerService schedulerService;
    @Mock private PnDeliveryPushValidatorConfigs cfg;
    @Mock private LookupAddressHandler lookupAddressHandler;
    @Mock private NotificationRefusedSchedulerHelper refusedSchedulerHelper;

    private InformalNotificationValidationStrategy strategy;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        strategy = new InformalNotificationValidationStrategy(
                attachmentUtils, campaignValidator, messageValidator,
                timelineUtils, notificationService, notificationValidationScheduler,
                addressValidator, auditLogService, normalizeAddressHandler,
                schedulerService, cfg, lookupAddressHandler, refusedSchedulerHelper);
    }

    private NotificationInt buildInformalNotification() {
        return NotificationInt.builder()
                .iun("IUN-INF-001")
                .type(NotificationType.INFORMAL)
                .campaignId("CAMP-001")
                .messageId("MSG-001")
                .sender(NotificationSenderInt.builder().paId("PA-001").paTaxId("TAX-001").build())
                .recipients(List.of(NotificationRecipientInt.builder().taxId("CF1").build()))
                .documents(List.of(NotificationDocumentInt.builder()
                        .ref(NotificationDocumentInt.Ref.builder().key("key1").build()).build()))
                .build();
    }

    private void mockAuditLog() {
        PnAuditLogEvent auditLogEvent = Mockito.mock(PnAuditLogEvent.class);
        Mockito.when(auditLogService.buildAuditLogEvent(any(), any(PnAuditLogEventType.class), any(), any()))
                .thenReturn(auditLogEvent);
        Mockito.when(auditLogEvent.generateSuccess()).thenReturn(auditLogEvent);
        Mockito.when(auditLogEvent.generateWarning(any(), any(String.class), any())).thenReturn(auditLogEvent);
    }

    @Test
    void validate_success() {
        mockAuditLog();
        NotificationInt notification = buildInformalNotification();

        CampaignData campaign = CampaignData.builder().campaignId("CAMP-001").closed(false)
                .startDate(Instant.now().minus(1, ChronoUnit.DAYS))
                .endDate(Instant.now().plus(30, ChronoUnit.DAYS)).build();
        Mockito.when(campaignValidator.validateAndGetCampaign(notification)).thenReturn(campaign);

        MessageData message = MessageData.builder().messageId("MSG-001").language("IT").build();
        Mockito.when(messageValidator.validateMessage(notification, campaign)).thenReturn(message);

        Mockito.when(addressValidator.requestValidateAndNormalizeAddresses(notification)).thenReturn(Mono.empty());

        NotificationValidationActionDetails details = NotificationValidationActionDetails.builder()
                .retryAttempt(0).startWorkflowTime(Instant.now()).build();

        strategy.validate(notification, details);

        verify(attachmentUtils).validateAttachment(notification);
        verify(campaignValidator).validateAndGetCampaign(notification);
        verify(messageValidator).validateMessage(notification, campaign);
        verify(notificationValidationScheduler, never()).scheduleNotificationValidation(any(), anyInt(), any(), any());
    }

    @Test
    void validate_campaignClosed_refused() {
        mockAuditLog();
        NotificationInt notification = buildInformalNotification();

        Mockito.doThrow(new PnValidationCampaignException("Campaign is closed"))
                .when(campaignValidator).validateAndGetCampaign(notification);

        NotificationValidationActionDetails details = NotificationValidationActionDetails.builder()
                .retryAttempt(0).startWorkflowTime(Instant.now()).build();

        strategy.validate(notification, details);

        verify(refusedSchedulerHelper).scheduleNotificationRefused(eq("IUN-INF-001"), any());
        verify(messageValidator, never()).validateMessage(any(), any());
    }

    @Test
    void validate_messageNotFound_refused() {
        mockAuditLog();
        NotificationInt notification = buildInformalNotification();

        CampaignData campaign = CampaignData.builder().campaignId("CAMP-001").closed(false).build();
        Mockito.when(campaignValidator.validateAndGetCampaign(notification)).thenReturn(campaign);

        Mockito.doThrow(new PnValidationMessageException("Message not found"))
                .when(messageValidator).validateMessage(notification, campaign);

        NotificationValidationActionDetails details = NotificationValidationActionDetails.builder()
                .retryAttempt(0).startWorkflowTime(Instant.now()).build();

        strategy.validate(notification, details);

        verify(refusedSchedulerHelper).scheduleNotificationRefused(eq("IUN-INF-001"), any());
    }

    @Test
    void validate_runtimeException_rescheduled() {
        mockAuditLog();
        NotificationInt notification = buildInformalNotification();

        RuntimeException ex = new RuntimeException("service unavailable");
        Mockito.doThrow(ex).when(attachmentUtils).validateAttachment(notification);

        NotificationValidationActionDetails details = NotificationValidationActionDetails.builder()
                .retryAttempt(1).startWorkflowTime(Instant.now()).build();

        strategy.validate(notification, details);

        verify(notificationValidationScheduler).scheduleNotificationValidation(
                eq(notification), eq(1), eq(ex), any(Instant.class));
    }
}

