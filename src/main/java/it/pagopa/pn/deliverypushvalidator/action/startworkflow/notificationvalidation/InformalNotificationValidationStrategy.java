package it.pagopa.pn.deliverypushvalidator.action.startworkflow.notificationvalidation;

import it.pagopa.pn.api.dto.events.PnF24MetadataValidationEndEventPayload;
import it.pagopa.pn.api.dto.events.notificationcost.validation.PnNotificationCostValidationEventPayload;
import it.pagopa.pn.commons.exceptions.PnInternalException;
import it.pagopa.pn.commons.exceptions.PnValidationException;
import it.pagopa.pn.commons.log.PnAuditLogEvent;
import it.pagopa.pn.commons.log.PnAuditLogEventType;
import it.pagopa.pn.commons.utils.MDCUtils;
import it.pagopa.pn.deliverypushvalidator.action.details.NotificationValidationActionDetails;
import it.pagopa.pn.deliverypushvalidator.action.startworkflow.LookupAddressHandler;
import it.pagopa.pn.deliverypushvalidator.action.startworkflow.NormalizeAddressHandler;
import it.pagopa.pn.deliverypushvalidator.config.PnDeliveryPushValidatorConfigs;
import it.pagopa.pn.deliverypushvalidator.dto.campaign.Campaign;
import it.pagopa.pn.deliverypushvalidator.dto.campaign.Channel;
import it.pagopa.pn.deliverypushvalidator.dto.ext.addressmanager.NormalizeItemsResultInt;
import it.pagopa.pn.deliverypushvalidator.dto.ext.delivery.notification.NotificationInt;
import it.pagopa.pn.deliverypushvalidator.dto.timeline.CommunicationType;
import it.pagopa.pn.deliverypushvalidator.exception.PnLookupAddressValidationFailedException;
import it.pagopa.pn.deliverypushvalidator.exception.PnValidationFileNotFoundException;
import it.pagopa.pn.deliverypushvalidator.exception.PnValidationNotValidAddressException;
import it.pagopa.pn.deliverypushvalidator.middleware.queue.producer.abstractions.actionspool.ActionType;
import it.pagopa.pn.deliverypushvalidator.service.AuditLogService;
import it.pagopa.pn.deliverypushvalidator.service.CampaignValidator;
import it.pagopa.pn.deliverypushvalidator.service.NotificationService;
import it.pagopa.pn.deliverypushvalidator.service.SchedulerService;
import lombok.CustomLog;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;

import static it.pagopa.pn.deliverypushvalidator.exception.PnDeliveryPushValidatorExceptionCodes.ERROR_CODE_DELIVERYPUSH_VALIDATION_STEP_NOT_IMPLEMENTED;

@CustomLog
public class InformalNotificationValidationStrategy extends BaseNotificationValidationStrategy implements NotificationValidationStrategy {

    private static final int FIRST_VALIDATION_STEP = 1;
    private static final int SECOND_VALIDATION_STEP = 2;
    private static final int THIRD_VALIDATION_STEP = 3;
    private static final String NOTIFICATION_IS_NOT_VALID_MSG = "Notification is not valid - iun={} ex={}";
    private final NotificationService notificationService;
    private final SchedulerService schedulerService;
    private final AddressValidator addressValidator;
    private final NormalizeAddressHandler normalizeAddressHandler;
    private final AuditLogService auditLogService;
    private final PnDeliveryPushValidatorConfigs cfg;
    private final AttachmentUtils attachmentUtils;
    private final CampaignValidator campaignValidator;
    private final MessageValidator messageValidator;
    private final LookupAddressHandler lookupAddressHandler;

    public InformalNotificationValidationStrategy(NotificationValidationScheduler notificationValidationScheduler, SchedulerService schedulerService, NotificationService notificationService, SchedulerService schedulerService1, AddressValidator addressValidator, NormalizeAddressHandler normalizeAddressHandler, AuditLogService auditLogService, PnDeliveryPushValidatorConfigs cfg, AttachmentUtils attachmentUtils, CampaignValidator campaignValidator, MessageValidator messageValidator, LookupAddressHandler lookupAddressHandler) {
        super(notificationValidationScheduler, schedulerService, cfg);
        this.notificationService = notificationService;
        this.schedulerService = schedulerService1;
        this.addressValidator = addressValidator;
        this.normalizeAddressHandler = normalizeAddressHandler;
        this.auditLogService = auditLogService;
        this.cfg = cfg;
        this.attachmentUtils = attachmentUtils;
        this.campaignValidator = campaignValidator;
        this.messageValidator = messageValidator;
        this.lookupAddressHandler = lookupAddressHandler;
    }

    @Override
    public NotificationInt getNotification(String iun) {
        log.debug("Start getInformalNotification - iun={}", iun);
        return notificationService.getInformalNotificationByIun(iun);
    }

    @Override
    public void validate(NotificationInt notification, NotificationValidationActionDetails details) {

        log.debug("Start validateInformalNotification - iun={}", notification.getIun());
        PnAuditLogEvent logEvent = generateAuditLog(notification, FIRST_VALIDATION_STEP);

        try {
            attachmentUtils.validateAttachment(notification);
            Campaign campaign = campaignValidator.validateAndGetCampaign(notification);
            messageValidator.validate(notification);
            logEvent.generateSuccess().log();

            if (hasAnalogCampaign(campaign)) {
                NotificationInt refreshed = verifyLookUpAddressAndRefreshNotification(notification);
                MDCUtils.addMDCToContextAndExecute(addressValidator.requestValidateAndNormalizeAddresses(refreshed)).block();
            } else {
                generateSkipAuditLog(notification, SECOND_VALIDATION_STEP, "Lookup address validation will be skipped because campaign has no analog channel").generateSuccess().log();
                generateSkipAuditLog(notification, THIRD_VALIDATION_STEP, "Normalize address validation will be skipped because campaign has no analog channel").generateSuccess().log();
                scheduleEndValidationAction(notification.getIun());
            }

        } catch (PnValidationFileNotFoundException ex) {
            if (cfg.isSafeStorageFileNotFoundRetry())
                logEvent.generateWarning("Validation need to be rescheduled - iun={} ex=", notification.getIun(), ex).log();
            handlePnValidationFileNotFoundException(notification.getIun(), details, notification, ex, details.getStartWorkflowTime());
        } catch (PnLookupAddressValidationFailedException ex) {
            log.warn(String.format("Lookup address validation failed - iun=%s", notification.getIun()), ex);
            handleLookupAddressValidationError(notification, ex);
        } catch (PnValidationException ex) {
            logEvent.generateWarning(NOTIFICATION_IS_NOT_VALID_MSG, notification.getIun(), ex).log();
            handleValidationError(notification, ex);
        } catch (RuntimeException ex) {
            logEvent.generateWarning("Validation need to be rescheduled - iun={} ex=", notification.getIun(), ex).log();
            handleRuntimeException(notification.getIun(), details, notification, ex, details.getStartWorkflowTime());
        }
    }

    @Override
    public void handleValidateAndNormalizeAddressResponse(String iun, NormalizeItemsResultInt normalizeItemsResult) {
        NotificationInt notification = notificationService.getNotificationByIun(iun);
        PnAuditLogEvent logEvent = generateAuditLog(notification, THIRD_VALIDATION_STEP);

        try {
            addressValidator.handleAddressValidation(iun, normalizeItemsResult);
            normalizeAddressHandler.handleNormalizedAddressResponse(notification, normalizeItemsResult);
            logEvent.generateSuccess().log();
            this.scheduleEndValidationAction(iun);
        } catch (PnValidationNotValidAddressException ex) {
            logEvent.generateWarning(NOTIFICATION_IS_NOT_VALID_MSG, notification.getIun(), ex).log();
            handleValidationError(notification, ex);
        }
    }

    @Override
    public void handleValidateF24Response(PnF24MetadataValidationEndEventPayload payload) {
        throw new PnInternalException(ERROR_CODE_DELIVERYPUSH_VALIDATION_STEP_NOT_IMPLEMENTED, "F24 validation is not implemented for informal notification");
    }

    @Override
    public void handleValidateNotificationCost(String iun, PnNotificationCostValidationEventPayload event) {
        throw new PnInternalException(ERROR_CODE_DELIVERYPUSH_VALIDATION_STEP_NOT_IMPLEMENTED, "Notification cost validation is not implemented for informal notification");
    }

    @Override
    public void scheduleEndValidationAction(String iun) {
        Instant schedulingDate = Instant.now();
        log.debug("Scheduling end of informal notification validation: iun={}, schedulingDate={}", iun, schedulingDate);
        schedulerService.scheduleEvent(iun, schedulingDate, ActionType.POST_VALIDATION_COMPLETED, CommunicationType.INFORMAL);
    }

    private PnAuditLogEvent generateAuditLog(NotificationInt notification, int validationStep) {
        String message = "Informal notification validation step {} of 3. ";

        return auditLogService.buildAuditLogEvent(notification.getIun(), PnAuditLogEventType.AUD_COM_VALID, message, validationStep);
    }

    @NotNull
    private PnAuditLogEvent generateSkipAuditLog(NotificationInt notification, int validationStep, String detail) {
        String message = "Notification validation step {} of 3. " + detail;

        return auditLogService.buildAuditLogEvent(notification.getIun(), PnAuditLogEventType.AUD_COM_VALID, message, validationStep);
    }

    private NotificationInt verifyLookUpAddressAndRefreshNotification(NotificationInt notificationInt) {
        log.debug("Start verifyLookUpAddressAndNormalizeAddress - iun={}", notificationInt.getIun());

        if (notificationInt.getUsedServices() != null && Boolean.TRUE.equals(notificationInt.getUsedServices().getPhysicalAddressLookUp())) {
            lookupAddressHandler.performValidation(notificationInt);
            NotificationInt refreshedNotification = getNotification(notificationInt.getIun());
            generateAuditLog(notificationInt, SECOND_VALIDATION_STEP).generateSuccess().log();
            return refreshedNotification;

        } else {
            generateSkipAuditLog(notificationInt, SECOND_VALIDATION_STEP, "Lookup address validation will be skipped").generateSuccess().log();
        }
        return notificationInt;
    }

    private boolean hasAnalogCampaign(Campaign campaign) {
        return campaign.getWorkflow().stream().anyMatch(workflowEntity -> workflowEntity.getChannel().equals(Channel.ANALOG));
    }

}