package it.pagopa.pn.deliverypushvalidator.action.startworkflow.notificationvalidation;

import it.pagopa.pn.api.dto.events.PnF24MetadataValidationEndEventPayload;
import it.pagopa.pn.commons.exceptions.PnValidationException;
import it.pagopa.pn.commons.log.PnAuditLogEvent;
import it.pagopa.pn.commons.log.PnAuditLogEventType;
import it.pagopa.pn.commons.utils.MDCUtils;
import it.pagopa.pn.deliverypushvalidator.action.details.NotificationValidationActionDetails;
import it.pagopa.pn.deliverypushvalidator.action.startworkflow.LookupAddressHandler;
import it.pagopa.pn.deliverypushvalidator.action.startworkflow.NormalizeAddressHandler;
import it.pagopa.pn.deliverypushvalidator.action.utils.TimelineUtils;
import it.pagopa.pn.deliverypushvalidator.config.PnDeliveryPushValidatorConfigs;
import it.pagopa.pn.deliverypushvalidator.dto.ext.addressmanager.NormalizeItemsResultInt;
import it.pagopa.pn.deliverypushvalidator.dto.ext.delivery.notification.NotificationInt;
import it.pagopa.pn.deliverypushvalidator.dto.timeline.NotificationRefusedErrorInt;
import it.pagopa.pn.deliverypushvalidator.exception.PnLookupAddressValidationFailedException;
import it.pagopa.pn.deliverypushvalidator.exception.PnValidationFileNotFoundException;
import it.pagopa.pn.deliverypushvalidator.middleware.queue.producer.abstractions.actionspool.ActionType;
import it.pagopa.pn.deliverypushvalidator.service.*;
import it.pagopa.pn.deliverypushvalidator.validation.CampaignData;
import it.pagopa.pn.deliverypushvalidator.validation.CampaignValidator;
import it.pagopa.pn.deliverypushvalidator.validation.MessageValidator;
import lombok.AllArgsConstructor;
import lombok.CustomLog;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Validation strategy for INFORMAL (bonarie) notifications.
 * <p>
 * Key differences from LEGAL:
 * <ul>
 *   <li>Validates campaign (ParameterStore) and message (Dynamo mock) instead of payments/F24/CF/20grams.</li>
 *   <li>No AAR (attestazione opponibile) generation — skips SCHEDULE_RECEIVED_LEGALFACT_GENERATION.</li>
 *   <li>Lookup and normalization are non-blocking — errors do not cause notification refusal.</li>
 *   <li>Attachment validation is reused as-is (same SHA/PDF/size checks).</li>
 * </ul>
 */
@Component
@AllArgsConstructor
@CustomLog
public class InformalNotificationValidationStrategy implements NotificationValidationStrategy {

    private static final int FIRST_VALIDATION_STEP = 1;
    private static final int SECOND_VALIDATION_STEP = 2;
    private static final int THIRD_VALIDATION_STEP = 3;
    private static final int FOURTH_VALIDATION_STEP = 4;
    private static final String NOTIFICATION_IS_NOT_VALID_MSG = "Notification is not valid - iun={} ex={}";

    private final AttachmentUtils attachmentUtils;
    private final CampaignValidator campaignValidator;
    private final MessageValidator messageValidator;
    private final TimelineUtils timelineUtils;
    private final NotificationService notificationService;
    private final NotificationValidationScheduler notificationValidationScheduler;
    private final AddressValidator addressValidator;
    private final AuditLogService auditLogService;
    private final NormalizeAddressHandler normalizeAddressHandler;
    private final SchedulerService schedulerService;
    private final PnDeliveryPushValidatorConfigs cfg;
    private final LookupAddressHandler lookupAddressHandler;
    private final NotificationRefusedSchedulerHelper refusedSchedulerHelper;

    @Override
    public void validate(NotificationInt notification, NotificationValidationActionDetails details) {
        log.debug("Start INFORMAL validateNotification - iun={}", notification.getIun());

        PnAuditLogEvent logEvent = generateAuditLog(notification, FIRST_VALIDATION_STEP);

        try {
            // Step 1: Validate attachments (same checks as LEGAL: SHA-256, PDF, size)
            attachmentUtils.validateAttachment(notification);

            // Step 2: Validate campaign
            CampaignData campaign = campaignValidator.validateAndGetCampaign(notification);
            generateAuditLog(notification, SECOND_VALIDATION_STEP).generateSuccess().log();

            // Step 3: Validate message (resolved from notification or campaign)
            messageValidator.validateMessage(notification, campaign);

            // Step 4: Lookup + Normalize (non-blocking for INFORMAL)
            verifyLookUpAddressAndNormalizeAddress(notification);

            logEvent.generateSuccess().log();

        } catch (PnValidationFileNotFoundException ex) {
            if (cfg.isSafeStorageFileNotFoundRetry()) {
                logEvent.generateWarning("Validation need to be rescheduled - iun={} ex=", notification.getIun(), ex).log();
                log.info("Notification validation need to be rescheduled  - iun={}", notification.getIun());
                notificationValidationScheduler.scheduleNotificationValidation(notification, details.getRetryAttempt(), ex, details.getStartWorkflowTime());
            } else {
                handleValidationError(notification, ex);
            }
        } catch (PnValidationException ex) {
            logEvent.generateWarning(NOTIFICATION_IS_NOT_VALID_MSG, notification.getIun(), ex).log();
            handleValidationError(notification, ex);
        } catch (RuntimeException ex) {
            logEvent.generateWarning("Validation need to be rescheduled - iun={} ex=", notification.getIun(), ex).log();
            log.warn(String.format("RuntimeException in INFORMAL validateNotification - iun=%s", notification.getIun()), ex);
            notificationValidationScheduler.scheduleNotificationValidation(notification, details.getRetryAttempt(), ex, details.getStartWorkflowTime());
        }
    }

    @Override
    public void handleValidateF24Response(PnF24MetadataValidationEndEventPayload payload) {
        // F24 validation is not applicable for INFORMAL notifications
        log.warn("handleValidateF24Response invoked for INFORMAL notification setId={} — ignoring", payload.getSetId());
    }

    @Override
    public void handleValidateAndNormalizeAddressResponse(String iun, NormalizeItemsResultInt normalizeItemsResult) {
        NotificationInt notification = notificationService.getNotificationByIun(iun);
        PnAuditLogEvent logEvent = generateAuditLog(notification, FOURTH_VALIDATION_STEP);

        // Non-blocking: errors do NOT cause refusal
        addressValidator.handleAddressValidation(iun, normalizeItemsResult, false);
        normalizeAddressHandler.handleNormalizedAddressResponse(notification, normalizeItemsResult, false);

        log.debug("INFORMAL notification validated successfully (non-blocking address) - iun={}", iun);

        // Schedule post-informal-validation (no AAR generation)
        Instant schedulingDate = Instant.now();
        log.debug("Scheduling POST_INFORMAL_VALIDATION_COMPLETED, schedulingDate={} - iun={}", schedulingDate, iun);
        schedulerService.scheduleEvent(iun, schedulingDate, ActionType.POST_INFORMAL_VALIDATION_COMPLETED);

        logEvent.generateSuccess().log();
    }

    // ---- private helpers ----

    private void verifyLookUpAddressAndNormalizeAddress(NotificationInt notificationInt) {
        log.debug("Start verifyLookUpAddressAndNormalizeAddress (INFORMAL) - iun={}", notificationInt.getIun());

        if (timelineUtils.checkIsNotificationCancellationRequested(notificationInt.getIun())) {
            log.warn("Process blocked: cancellation requested for iun {}", notificationInt.getIun());
            return;
        }

        if (notificationInt.getUsedServices() != null && notificationInt.getUsedServices().getPhysicalAddressLookUp()) {
            generateAuditLog(notificationInt, THIRD_VALIDATION_STEP).generateSuccess().log();
            try {
                // Non-blocking lookup: errors logged but not thrown
                lookupAddressHandler.performValidation(notificationInt, false);
                NotificationInt refreshedNotification = notificationService.getNotificationByIun(notificationInt.getIun());
                MDCUtils.addMDCToContextAndExecute(
                        addressValidator.requestValidateAndNormalizeAddresses(refreshedNotification)
                ).block();
            } catch (PnLookupAddressValidationFailedException ex) {
                // Non-blocking: log and continue, do NOT refuse
                log.warn("Lookup address validation failed (non-blocking INFORMAL) - iun={}", notificationInt.getIun(), ex);
                // Still proceed with normalization of whatever addresses are available
                MDCUtils.addMDCToContextAndExecute(
                        addressValidator.requestValidateAndNormalizeAddresses(notificationInt)
                ).block();
            }
        } else {
            generateSkipAuditLog(notificationInt, THIRD_VALIDATION_STEP, " Lookup address validation will be skipped (INFORMAL)").generateSuccess().log();
            MDCUtils.addMDCToContextAndExecute(
                    addressValidator.requestValidateAndNormalizeAddresses(notificationInt)
            ).block();
        }
    }

    @NotNull
    private PnAuditLogEvent generateAuditLog(NotificationInt notification, int validationStep) {
        String message = "INFORMAL notification validation step {} of 4.";
        return auditLogService.buildAuditLogEvent(
                notification.getIun(), PnAuditLogEventType.AUD_NT_VALID, message, validationStep);
    }

    @NotNull
    private PnAuditLogEvent generateSkipAuditLog(NotificationInt notification, int validationStep, String detail) {
        String message = "INFORMAL notification validation step {} of 4." + detail;
        return auditLogService.buildAuditLogEvent(
                notification.getIun(), PnAuditLogEventType.AUD_NT_VALID, message, validationStep);
    }

    private void handleValidationError(NotificationInt notification, PnValidationException ex) {
        List<NotificationRefusedErrorInt> errors = new ArrayList<>();
        if (Objects.nonNull(ex.getProblem())) {
            ex.getProblem().getErrors().forEach(elem ->
                    errors.add(NotificationRefusedErrorInt.builder()
                            .errorCode(elem.getCode())
                            .detail(elem.getDetail())
                            .build()));
        }
        log.info("INFORMAL notification refused, errors {} - iun {}", errors, notification.getIun());
        refusedSchedulerHelper.scheduleNotificationRefused(notification.getIun(), errors);
    }
}

