package it.pagopa.pn.deliverypushvalidator.action.startworkflow.notificationvalidation;

import it.pagopa.pn.api.dto.events.PnF24MetadataValidationEndEventPayload;
import it.pagopa.pn.api.dto.events.notificationcost.validation.PnNotificationCostValidationEventPayload;
import it.pagopa.pn.commons.exceptions.PnInternalException;
import it.pagopa.pn.commons.log.PnAuditLogEvent;
import it.pagopa.pn.commons.log.PnAuditLogEventType;
import it.pagopa.pn.deliverypushvalidator.action.details.NotificationValidationActionDetails;
import it.pagopa.pn.deliverypushvalidator.action.it.utils.TestUtils;
import it.pagopa.pn.deliverypushvalidator.action.startworkflow.LookupAddressHandler;
import it.pagopa.pn.deliverypushvalidator.action.startworkflow.NormalizeAddressHandler;
import it.pagopa.pn.deliverypushvalidator.config.PnDeliveryPushValidatorConfigs;
import it.pagopa.pn.deliverypushvalidator.dto.campaign.Campaign;
import it.pagopa.pn.deliverypushvalidator.dto.campaign.Channel;
import it.pagopa.pn.deliverypushvalidator.dto.campaign.WorkflowEntity;
import it.pagopa.pn.deliverypushvalidator.dto.ext.addressmanager.NormalizeItemsResultInt;
import it.pagopa.pn.deliverypushvalidator.dto.ext.addressmanager.NormalizeResultInt;
import it.pagopa.pn.deliverypushvalidator.dto.ext.delivery.notification.NotificationInt;
import it.pagopa.pn.deliverypushvalidator.dto.ext.delivery.notification.UsedServicesInt;
import it.pagopa.pn.deliverypushvalidator.dto.timeline.CommunicationType;
import it.pagopa.pn.deliverypushvalidator.exception.PnLookupAddressValidationFailedException;
import it.pagopa.pn.deliverypushvalidator.exception.PnValidationFileNotFoundException;
import it.pagopa.pn.deliverypushvalidator.exception.PnValidationNotMatchingShaException;
import it.pagopa.pn.deliverypushvalidator.exception.PnValidationNotValidAddressException;
import it.pagopa.pn.deliverypushvalidator.middleware.queue.producer.abstractions.actionspool.ActionType;
import it.pagopa.pn.deliverypushvalidator.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
class InformalNotificationValidationStrategyTest {

    @Mock
    private NotificationValidationScheduler notificationValidationScheduler;
    @Mock
    private SchedulerService schedulerService;
    @Mock
    private NotificationService notificationService;
    @Mock
    private AddressValidator addressValidator;
    @Mock
    private NormalizeAddressHandler normalizeAddressHandler;
    @Mock
    private AuditLogService auditLogService;
    @Mock
    private PnDeliveryPushValidatorConfigs cfg;
    @Mock
    private AttachmentUtils attachmentUtils;
    @Mock
    private CampaignValidator campaignValidator;
    @Mock
    private MessageValidator messageValidator;
    @Mock
    private LookupAddressHandler lookupAddressHandler;
    @Mock
    private DigitalAddressValidator digitalAddressValidator;

    private InformalNotificationValidationStrategy handler;
    private static final String IUN = "TEST-IUN-001";

    private Campaign campaignWithAnalog() {
        WorkflowEntity analogWorkflow = WorkflowEntity.builder()
                .channel(Channel.ANALOG)
                .build();
        return Campaign.builder()
                .workflow(List.of(analogWorkflow))
                .build();
    }

    private Campaign campaignWithoutAnalog() {
        WorkflowEntity digitalWorkflow = WorkflowEntity.builder()
                .channel(Channel.EMAIL)
                .build();
        return Campaign.builder()
                .workflow(List.of(digitalWorkflow))
                .build();
    }

    private NotificationValidationActionDetails defaultDetails() {
        return NotificationValidationActionDetails.builder()
                .retryAttempt(1)
                .startWorkflowTime(Instant.now())
                .build();
    }

    private PnAuditLogEvent mockAuditLogEvent(NotificationInt notification) {
        PnAuditLogEvent auditLogEvent = Mockito.mock(PnAuditLogEvent.class);
        Mockito.when(auditLogService.buildAuditLogEvent(
                        Mockito.eq(notification.getIun()),
                        Mockito.eq(PnAuditLogEventType.AUD_COM_VALID),
                        Mockito.anyString(),
                        any()))
                .thenReturn(auditLogEvent);
        Mockito.when(auditLogEvent.generateSuccess()).thenReturn(auditLogEvent);
        // Varargs overload (String, Object[]) — must return non-null or .log() throws NPE
        Mockito.when(auditLogEvent.generateWarning(Mockito.anyString(), any(Object[].class))).thenReturn(auditLogEvent);
        // Two-arg overload kept for completeness
        Mockito.when(auditLogEvent.generateWarning(Mockito.anyString(), any(), any())).thenReturn(auditLogEvent);
        return auditLogEvent;
    }

    @BeforeEach
    void setup() {
        handler = new InformalNotificationValidationStrategy(
                notificationValidationScheduler,
                schedulerService,
                notificationService,
                addressValidator,
                normalizeAddressHandler,
                auditLogService,
                cfg,
                attachmentUtils,
                campaignValidator,
                messageValidator,
                lookupAddressHandler,
                digitalAddressValidator
        );
    }

    @Test
    void getNotification_returnsInformalNotification() {
        // GIVEN
        String iun = "testIun";
        NotificationInt expected = TestUtils.getNotification();
        Mockito.when(notificationService.getInformalNotificationByIun(iun)).thenReturn(expected);

        // WHEN
        NotificationInt result = handler.getNotification(iun);

        // THEN
        assertEquals(expected, result);
        Mockito.verify(notificationService).getInformalNotificationByIun(iun);
    }

    /**
     * Campaign has NO analog channel → lookup and normalize steps are skipped,
     * end-validation action is scheduled immediately.
     */
    @Test
    void validate_campaignWithoutAnalog_skipsLookupAndNormalize() {
        // GIVEN
        NotificationInt notification = TestUtils.getNotification();
        Mockito.when(notificationService.getInformalNotificationByIun(Mockito.anyString()))
                .thenReturn(notification);

        PnAuditLogEvent auditLogEvent = mockAuditLogEvent(notification);
        Mockito.when(campaignValidator.validateAndGetCampaign(notification)).thenReturn(campaignWithoutAnalog());
        Mockito.when(messageValidator.validate(notification)).thenReturn(Mono.empty());

        // WHEN
        handler.validate(IUN, defaultDetails());

        // THEN
        Mockito.verify(attachmentUtils).validateAttachment(notification);
        Mockito.verify(campaignValidator).validateAndGetCampaign(notification);
        Mockito.verify(messageValidator).validate(notification);
        // lookup and normalize must NOT be invoked
        Mockito.verify(lookupAddressHandler, never()).performValidation(any());
        Mockito.verify(addressValidator, never()).requestValidateAndNormalizeAddresses(any());
        // end-validation must be scheduled
        Mockito.verify(schedulerService).scheduleEvent(
                Mockito.eq(notification.getIun()),
                Mockito.any(Instant.class),
                Mockito.eq(ActionType.POST_VALIDATION_COMPLETED),
                Mockito.eq(it.pagopa.pn.deliverypushvalidator.dto.timeline.CommunicationType.INFORMAL)
        );
        Mockito.verify(auditLogEvent, times(3)).generateSuccess(); // step-1 + 2x generateSkipAuditLog (step-2 and step-3)
    }

    /**
     * Campaign has analog channel, physicalAddressLookUp is FALSE →
     * lookup is skipped, normalize proceeds directly.
     */
    @Test
    void validate_campaignWithAnalog_lookupSkipped_whenPhysicalAddressLookUpFalse() {
        // GIVEN
        UsedServicesInt usedServices = UsedServicesInt.builder()
                .physicalAddressLookUp(false)
                .build();
        NotificationInt notification = TestUtils.getNotificationV2(usedServices);
        Mockito.when(notificationService.getInformalNotificationByIun(Mockito.anyString()))
                .thenReturn(notification);

        PnAuditLogEvent auditLogEvent = mockAuditLogEvent(notification);
        Mockito.when(campaignValidator.validateAndGetCampaign(notification)).thenReturn(campaignWithAnalog());
        Mockito.when(messageValidator.validate(notification)).thenReturn(Mono.empty());
        Mockito.when(addressValidator.requestValidateAndNormalizeAddresses(notification))
                .thenReturn(Mono.empty());

        // WHEN
        handler.validate(IUN, defaultDetails());

        // THEN
        Mockito.verify(lookupAddressHandler, never()).performValidation(any());
        Mockito.verify(addressValidator).requestValidateAndNormalizeAddresses(notification);
        Mockito.verify(auditLogEvent, times(2)).generateSuccess(); // step-1 + generateSkipAuditLog for lookup skip (step-2)
    }

    /**
     * Campaign has analog channel, physicalAddressLookUp is TRUE →
     * lookup is performed, refreshed notification is used for normalize.
     */
    @Test
    void validate_campaignWithAnalog_lookupPerformed_whenPhysicalAddressLookUpTrue() {
        // GIVEN
        UsedServicesInt usedServices = UsedServicesInt.builder()
                .physicalAddressLookUp(true)
                .build();
        NotificationInt notification = TestUtils.getNotificationV2(usedServices);
        NotificationInt refreshedNotification = TestUtils.getNotificationV2(usedServices);

        Mockito.when(notificationService.getInformalNotificationByIun(Mockito.anyString()))
                .thenReturn(notification)
                .thenReturn(refreshedNotification);
        PnAuditLogEvent auditLogEvent = mockAuditLogEvent(notification);
        Mockito.when(campaignValidator.validateAndGetCampaign(notification)).thenReturn(campaignWithAnalog());
        Mockito.when(messageValidator.validate(notification)).thenReturn(Mono.empty());
        Mockito.when(addressValidator.requestValidateAndNormalizeAddresses(refreshedNotification))
                .thenReturn(Mono.empty());

        // WHEN
        handler.validate(IUN, defaultDetails());

        // THEN
        Mockito.verify(lookupAddressHandler).performValidation(notification);
        Mockito.verify(addressValidator).requestValidateAndNormalizeAddresses(refreshedNotification);
        Mockito.verify(auditLogEvent, times(2)).generateSuccess();
    }

    /**
     * Campaign has analog channel, usedServices is null →
     * lookup is skipped, normalize proceeds directly.
     */
    @Test
    void validate_campaignWithAnalog_lookupSkipped_whenUsedServicesNull() {
        // GIVEN
        NotificationInt notification = TestUtils.getNotification(); // usedServices = null
        Mockito.when(notificationService.getInformalNotificationByIun(Mockito.anyString()))
                .thenReturn(notification);

        PnAuditLogEvent auditLogEvent = mockAuditLogEvent(notification);
        Mockito.when(campaignValidator.validateAndGetCampaign(notification)).thenReturn(campaignWithAnalog());
        Mockito.when(messageValidator.validate(notification)).thenReturn(Mono.empty());
        Mockito.when(addressValidator.requestValidateAndNormalizeAddresses(notification))
                .thenReturn(Mono.empty());

        // WHEN
        handler.validate(IUN, defaultDetails());

        // THEN
        Mockito.verify(lookupAddressHandler, never()).performValidation(any());
        Mockito.verify(addressValidator).requestValidateAndNormalizeAddresses(notification);
    }

    @Test
    void validate_PnValidationFileNotFoundException_safeStorageRetryEnabled_schedulesRetry() {
        // GIVEN
        NotificationInt notification = TestUtils.getNotification();
        Mockito.when(notificationService.getInformalNotificationByIun(Mockito.anyString()))
                .thenReturn(notification);
        Mockito.when(cfg.isSafeStorageFileNotFoundRetry()).thenReturn(true);

        PnValidationFileNotFoundException ex = new PnValidationFileNotFoundException("detail", new RuntimeException());
        doThrow(ex).when(attachmentUtils).validateAttachment(notification);

        PnAuditLogEvent auditLogEvent = mockAuditLogEvent(notification);
        NotificationValidationActionDetails details = defaultDetails();

        // WHEN
        handler.validate(IUN, details);

        // THEN
        Mockito.verify(notificationValidationScheduler)
                .scheduleNotificationValidation(notification, details.getRetryAttempt(), ex, details.getStartWorkflowTime());
        Mockito.verify(auditLogEvent).generateWarning(any(), any(String.class), any());
        Mockito.verify(addressValidator, never()).requestValidateAndNormalizeAddresses(any());
    }

    @Test
    void validate_PnValidationFileNotFoundException_safeStorageRetryDisabled_doesNotScheduleRetry() {
        // GIVEN
        NotificationInt notification = TestUtils.getNotification();
        Mockito.when(notificationService.getInformalNotificationByIun(Mockito.anyString()))
                .thenReturn(notification);
        Mockito.when(cfg.isSafeStorageFileNotFoundRetry()).thenReturn(false);

        PnValidationFileNotFoundException ex = new PnValidationFileNotFoundException("detail", new RuntimeException());
        doThrow(ex).when(attachmentUtils).validateAttachment(notification);

        mockAuditLogEvent(notification);
        NotificationValidationActionDetails details = defaultDetails();

        // WHEN
        handler.validate(IUN, details);

        // THEN – when retry is disabled the notification is refused (not rescheduled)
        Mockito.verify(notificationValidationScheduler, never())
                .scheduleNotificationValidation(any(), anyInt(), any(), any());
        Mockito.verify(addressValidator, never()).requestValidateAndNormalizeAddresses(any());
    }

    @Test
    void validate_PnValidationException_SHA_mismatch_refusesNotification() {
        // GIVEN
        NotificationInt notification = TestUtils.getNotification();
        Mockito.when(notificationService.getInformalNotificationByIun(Mockito.anyString()))
                .thenReturn(notification);

        doThrow(new PnValidationNotMatchingShaException("sha mismatch"))
                .when(attachmentUtils).validateAttachment(notification);

        PnAuditLogEvent auditLogEvent = mockAuditLogEvent(notification);

        // WHEN
        handler.validate(IUN, defaultDetails());

        // THEN
        Mockito.verify(addressValidator, never()).requestValidateAndNormalizeAddresses(any());
        Mockito.verify(auditLogEvent).generateWarning(any(), any(String.class), any());
        Mockito.verify(notificationValidationScheduler, never())
                .scheduleNotificationValidation(any(), anyInt(), any(), any());
    }

    @Test
    void validate_RuntimeException_schedulesRetry() {
        // GIVEN
        NotificationInt notification = TestUtils.getNotification();
        Mockito.when(notificationService.getInformalNotificationByIun(Mockito.anyString()))
                .thenReturn(notification);

        RuntimeException ex = new RuntimeException("generic error");
        doThrow(ex).when(attachmentUtils).validateAttachment(notification);

        PnAuditLogEvent auditLogEvent = mockAuditLogEvent(notification);
        NotificationValidationActionDetails details = defaultDetails();

        // WHEN
        handler.validate(IUN, details);

        // THEN
        Mockito.verify(notificationValidationScheduler)
                .scheduleNotificationValidation(notification, details.getRetryAttempt(), ex, details.getStartWorkflowTime());
        Mockito.verify(auditLogEvent).generateWarning(any(), any(String.class), any());
        Mockito.verify(addressValidator, never()).requestValidateAndNormalizeAddresses(any());
    }

    /**
     * Lookup validation fails → error is handled (refused), end-validation is NOT scheduled.
     */
    @Test
    void validate_PnLookupAddressValidationFailedException_handlesLookupError() {
        // GIVEN
        UsedServicesInt usedServices = UsedServicesInt.builder()
                .physicalAddressLookUp(true)
                .build();
        NotificationInt notification = TestUtils.getNotificationV2(usedServices);
        Mockito.when(notificationService.getInformalNotificationByIun(Mockito.anyString()))
                .thenReturn(notification);

        PnAuditLogEvent auditLogEvent = mockAuditLogEvent(notification);
        Mockito.when(campaignValidator.validateAndGetCampaign(notification)).thenReturn(campaignWithAnalog());
        Mockito.when(messageValidator.validate(notification)).thenReturn(Mono.empty());

        PnLookupAddressValidationFailedException ex =
                new PnLookupAddressValidationFailedException(Collections.emptyList());
        doThrow(ex).when(lookupAddressHandler).performValidation(notification);

        // WHEN
        handler.validate(IUN, defaultDetails());

        // THEN
        Mockito.verify(addressValidator, never()).requestValidateAndNormalizeAddresses(any());
        Mockito.verify(schedulerService, never()).scheduleEvent(
                any(), any(), eq(ActionType.POST_VALIDATION_COMPLETED), eq(CommunicationType.INFORMAL));
        Mockito.verify(notificationValidationScheduler, never())
                .scheduleNotificationValidation(any(), anyInt(), any(), any());
    }

    @Test
    void handleValidateAndNormalizeAddressResponse_OK_schedulesEndValidation() {
        // GIVEN
        String iun = "testIun";
        NotificationInt notification = TestUtils.getNotification();
        Mockito.when(notificationService.getInformalNotificationByIun(iun)).thenReturn(notification);

        PnAuditLogEvent auditLogEvent = mockAuditLogEvent(notification);

        NormalizeItemsResultInt normalizeItemsResult = NormalizeItemsResultInt.builder()
                .correlationId("corrId")
                .resultItems(List.of(NormalizeResultInt.builder().id("0").build()))
                .build();

        Mockito.doNothing().when(addressValidator).handleAddressValidation(iun, normalizeItemsResult);
        Mockito.doNothing().when(normalizeAddressHandler).handleNormalizedAddressResponse(notification, normalizeItemsResult);

        // WHEN
        assertDoesNotThrow(() -> handler.handleValidateAndNormalizeAddressResponse(iun, normalizeItemsResult));

        // THEN
        Mockito.verify(addressValidator).handleAddressValidation(iun, normalizeItemsResult);
        Mockito.verify(normalizeAddressHandler).handleNormalizedAddressResponse(notification, normalizeItemsResult);
        Mockito.verify(schedulerService).scheduleEvent(
                Mockito.eq(iun),
                Mockito.any(Instant.class),
                Mockito.eq(ActionType.POST_VALIDATION_COMPLETED),
                Mockito.eq(it.pagopa.pn.deliverypushvalidator.dto.timeline.CommunicationType.INFORMAL)
        );
        Mockito.verify(auditLogEvent).generateSuccess();
    }

    @Test
    void handleValidateAndNormalizeAddressResponse_InvalidAddress_refusesNotification() {
        // GIVEN
        String iun = "testIun";
        NotificationInt notification = TestUtils.getNotification();
        Mockito.when(notificationService.getInformalNotificationByIun(iun)).thenReturn(notification);

        PnAuditLogEvent auditLogEvent = mockAuditLogEvent(notification);

        NormalizeItemsResultInt normalizeItemsResult = NormalizeItemsResultInt.builder()
                .correlationId("corrId")
                .resultItems(Collections.emptyList())
                .build();

        PnValidationNotValidAddressException ex = new PnValidationNotValidAddressException("invalid address");
        doThrow(ex).when(addressValidator).handleAddressValidation(iun, normalizeItemsResult);

        // WHEN
        assertDoesNotThrow(() -> handler.handleValidateAndNormalizeAddressResponse(iun, normalizeItemsResult));

        // THEN
        Mockito.verify(normalizeAddressHandler, never()).handleNormalizedAddressResponse(any(), any());
        Mockito.verify(schedulerService, never()).scheduleEvent(
                any(), any(), eq(ActionType.POST_VALIDATION_COMPLETED), eq(CommunicationType.INFORMAL));
        Mockito.verify(auditLogEvent).generateWarning(any(), any(String.class), any());
    }

    @Test
    void handleValidateF24Response_throwsPnInternalException() {
        // GIVEN
        PnF24MetadataValidationEndEventPayload payload = PnF24MetadataValidationEndEventPayload.builder()
                .setId("someIun")
                .status("ok")
                .errors(Collections.emptyList())
                .build();

        // WHEN / THEN
        assertThrows(PnInternalException.class, () -> handler.handleValidateF24Response(payload));
    }

    @Test
    void handleValidateNotificationCost_throwsPnInternalException() {
        // GIVEN
        String iun = "testIun";
        PnNotificationCostValidationEventPayload event = PnNotificationCostValidationEventPayload.builder()
                .iun(iun)
                .build();

        // WHEN / THEN
        assertThrows(PnInternalException.class, () -> handler.handleValidateNotificationCost(iun, event));
    }

    @Test
    void scheduleEndValidationAction_schedulesPostValidationCompleted_withInformalType() {
        // GIVEN
        String iun = "testIun";

        // WHEN
        handler.scheduleEndValidationAction(iun);

        // THEN
        Mockito.verify(schedulerService).scheduleEvent(
                Mockito.eq(iun),
                Mockito.any(Instant.class),
                Mockito.eq(ActionType.POST_VALIDATION_COMPLETED),
                Mockito.eq(it.pagopa.pn.deliverypushvalidator.dto.timeline.CommunicationType.INFORMAL)
        );
    }
}