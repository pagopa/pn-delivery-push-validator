package it.pagopa.pn.deliverypushvalidator.action.startworkflow.notificationvalidation;

import it.pagopa.pn.api.dto.events.PnF24MetadataValidationEndEventPayload;
import it.pagopa.pn.commons.exceptions.PnInternalException;
import it.pagopa.pn.commons.exceptions.PnValidationException;
import it.pagopa.pn.commons.log.PnAuditLogEvent;
import it.pagopa.pn.commons.log.PnAuditLogEventType;
import it.pagopa.pn.commons.utils.MDCUtils;
import it.pagopa.pn.deliverypushvalidator.action.details.NotificationValidationActionDetails;
import it.pagopa.pn.deliverypushvalidator.action.startworkflow.LookupAddressHandler;
import it.pagopa.pn.deliverypushvalidator.action.startworkflow.NormalizeAddressHandler;
import it.pagopa.pn.deliverypushvalidator.action.utils.TimelineUtils;
import it.pagopa.pn.deliverypushvalidator.config.PnDeliveryPushValidatorConfigs;
import it.pagopa.pn.deliverypushvalidator.dto.campaign.Campaign;
import it.pagopa.pn.deliverypushvalidator.dto.campaign.Channel;
import it.pagopa.pn.deliverypushvalidator.dto.ext.addressmanager.NormalizeItemsResultInt;
import it.pagopa.pn.deliverypushvalidator.dto.ext.delivery.notification.NotificationInt;
import it.pagopa.pn.deliverypushvalidator.dto.ext.delivery.notification.CommunicationType;
import it.pagopa.pn.deliverypushvalidator.dto.timeline.TimelineElementInternal;
import it.pagopa.pn.deliverypushvalidator.exception.PnDeliveryPushValidatorExceptionCodes;
import it.pagopa.pn.deliverypushvalidator.dto.ext.notificationcost.NotificationCostValidationResponse;
import it.pagopa.pn.deliverypushvalidator.exception.PnLookupAddressValidationFailedException;
import it.pagopa.pn.deliverypushvalidator.exception.PnValidationFileNotFoundException;
import it.pagopa.pn.deliverypushvalidator.exception.PnValidationNotValidAddressException;
import it.pagopa.pn.deliverypushvalidator.middleware.queue.producer.abstractions.actionspool.ActionType;
import it.pagopa.pn.deliverypushvalidator.service.*;
import it.pagopa.pn.deliverypushvalidator.utils.NotificationCostServiceFeatureFlagUtils;
import lombok.CustomLog;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.time.Instant;

import static it.pagopa.pn.deliverypushvalidator.exception.PnDeliveryPushValidatorExceptionCodes.ERROR_CODE_DELIVERYPUSH_VALIDATION_STEP_NOT_IMPLEMENTED;

@CustomLog
@Component
public class InformalNotificationValidationStrategy extends BaseNotificationValidationStrategy implements NotificationValidationStrategy {
    private static final int FIRST_VALIDATION_STEP = 1;
    private static final int SECOND_VALIDATION_STEP = 2;
    private static final int THIRD_VALIDATION_STEP = 3;
    private static final int FOURTH_VALIDATION_STEP = 4;
    private static final int NUMBER_OF_STEPS = 4;
    private static final String NOTIFICATION_IS_NOT_VALID_MSG = "Notification is not valid - iun={} ex={}";
    private static final String NOTIFICATION_IS_NOT_VALID_MSG_WITHOUT_EX = "Notification is not valid - iun={}";
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
    private final DigitalAddressValidator digitalAddressValidator;
    private final NotificationCostServiceFeatureFlagUtils notificationCostServiceFeatureFlagUtils;
    private final NotificationCostService notificationCostService;
    private final TimelineService timelineService;
    private final TimelineUtils timelineUtils;

    public InformalNotificationValidationStrategy(NotificationValidationScheduler notificationValidationScheduler, SchedulerService schedulerService, NotificationService notificationService, AddressValidator addressValidator, NormalizeAddressHandler normalizeAddressHandler, AuditLogService auditLogService, PnDeliveryPushValidatorConfigs cfg, AttachmentUtils attachmentUtils, CampaignValidator campaignValidator, MessageValidator messageValidator, LookupAddressHandler lookupAddressHandler, DigitalAddressValidator digitalAddressValidator, NotificationCostServiceFeatureFlagUtils notificationCostServiceFeatureFlagUtils, NotificationCostService notificationCostService, TimelineService timelineService, TimelineUtils timelineUtils) {
        super(notificationValidationScheduler, schedulerService, cfg);
        this.notificationService = notificationService;
        this.schedulerService = schedulerService;
        this.addressValidator = addressValidator;
        this.normalizeAddressHandler = normalizeAddressHandler;
        this.auditLogService = auditLogService;
        this.cfg = cfg;
        this.attachmentUtils = attachmentUtils;
        this.campaignValidator = campaignValidator;
        this.messageValidator = messageValidator;
        this.lookupAddressHandler = lookupAddressHandler;
        this.digitalAddressValidator = digitalAddressValidator;
        this.notificationCostServiceFeatureFlagUtils = notificationCostServiceFeatureFlagUtils;
        this.notificationCostService = notificationCostService;
        this.timelineService = timelineService;
        this.timelineUtils = timelineUtils;
    }

    @Override
    public NotificationInt getNotification(String iun) {
        log.debug("Start getInformalNotification - iun={}", iun);
        return notificationService.getInformalNotificationByIun(iun);
    }

    @Override
    public void validate(String iun, NotificationValidationActionDetails details) {
        log.debug("Start validateInformalNotification - iun={}", iun);
        NotificationInt notification = getNotification(iun);

        PnAuditLogEvent logEvent = generateAuditLog(notification, FIRST_VALIDATION_STEP);
        try {
            attachmentUtils.validateAttachment(notification);
            Campaign campaign = campaignValidator.validateAndGetCampaign(notification);
            MDCUtils.addMDCToContextAndExecute(messageValidator.validate(notification)).block();
            digitalAddressValidator.validateDigitalAddress(notification, campaign);
            logEvent.generateSuccess().log();

            if (hasAnalogCampaign(campaign)) {
                NotificationInt refreshed = verifyLookUpAddressAndRefreshNotification(notification);
                MDCUtils.addMDCToContextAndExecute(addressValidator.requestValidateAndNormalizeAddresses(refreshed)).block();
            } else {
                generateSkipAuditLog(notification, SECOND_VALIDATION_STEP, "Lookup address validation will be skipped because campaign has no analog channel").generateSuccess().log();
                generateSkipAuditLog(notification, THIRD_VALIDATION_STEP, "Normalize address validation will be skipped because campaign has no analog channel").generateSuccess().log();
                this.checkFeatureFlagAndInitializeAndValidateNotificationCost(iun, notification);
            }

        } catch (PnValidationFileNotFoundException ex) {
            if (cfg.isSafeStorageFileNotFoundRetry()) {
                logEvent.generateWarning("Validation need to be rescheduled - iun={} ex=", notification.getIun(), ex).log();
            }
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
        NotificationInt notification = getNotification(iun);
        PnAuditLogEvent logEvent = generateAuditLog(notification, THIRD_VALIDATION_STEP);

        try {
            addressValidator.handleAddressValidation(iun, normalizeItemsResult);
            normalizeAddressHandler.handleNormalizedAddressResponse(notification, normalizeItemsResult);
            logEvent.generateSuccess().log();
            this.checkFeatureFlagAndInitializeAndValidateNotificationCost(iun, notification);
        } catch (PnValidationNotValidAddressException ex) {
            logEvent.generateWarning(NOTIFICATION_IS_NOT_VALID_MSG, notification.getIun(), ex).log();
            handleValidationError(notification, ex);
        }
    }

    private void checkFeatureFlagAndInitializeAndValidateNotificationCost(String iun, NotificationInt notification) {
        if (notificationCostServiceFeatureFlagUtils.checkNotificationCostServiceStartDate(notification) && notificationCostService.shouldValidateInformalNotificationCost(notification)) {
            notificationCostService.initializeAndValidateNotificationCost(notification);
        } else {
            scheduleEndValidationAction(iun);
            generateSkipAuditLog(notification, FOURTH_VALIDATION_STEP,
                    "Notification cost validation will be skipped due to feature flag or absence of pagopa payments").generateSuccess().log();
        }
    }

    @Override
    public void handleValidateF24Response(PnF24MetadataValidationEndEventPayload payload) {
        throw new PnInternalException("F24 validation is not implemented for informal notification", ERROR_CODE_DELIVERYPUSH_VALIDATION_STEP_NOT_IMPLEMENTED);
    }

    @Override
    public void handleValidateNotificationCost(String iun, NotificationCostValidationResponse event) {
        NotificationInt notification = getNotification(iun);
        PnAuditLogEvent logEvent = generateAuditLog(notification, FOURTH_VALIDATION_STEP);
        processNotificationCostValidationResponse(iun, event, notification, logEvent);
    }

    private void processNotificationCostValidationResponse(String iun, NotificationCostValidationResponse event, NotificationInt notification, PnAuditLogEvent logEvent) {
        switch (event.getStatus()) {
            case OK -> {
                TimelineElementInternal buildNotificationCostValidationResponse = timelineUtils.buildNotificationCostValidationResponse(notification);
                timelineService.addTimelineElement(buildNotificationCostValidationResponse, notification);
                log.debug("Notification validated successfully - iun={}", iun);
                scheduleEndValidationAction(iun);
                logEvent.generateSuccess().log();
            }
            case KO -> {
                logEvent.generateWarning(NOTIFICATION_IS_NOT_VALID_MSG_WITHOUT_EX, notification.getIun()).log();
                throw new PnInternalException(
                        String.format("Error Notification Cost Validation for iun=%s, status=%s", iun, event.getStatus()),
                        PnDeliveryPushValidatorExceptionCodes.ERROR_CODE_DELIVERYPUSH_NOTIFICATION_COST_ERROR
                );
            }
        }
    }

    @Override
    public void scheduleEndValidationAction(String iun) {
        Instant schedulingDate = Instant.now();
        log.debug("Scheduling end of informal notification validation: iun={}, schedulingDate={}", iun, schedulingDate);
        schedulerService.scheduleEvent(iun, schedulingDate, ActionType.POST_VALIDATION_COMPLETED, CommunicationType.INFORMAL);
    }

    private PnAuditLogEvent generateAuditLog(NotificationInt notification, int validationStep) {
        String message = "Informal notification validation step {} of " + NUMBER_OF_STEPS + ".";

        return auditLogService.buildAuditLogEvent(notification.getIun(), PnAuditLogEventType.AUD_COM_VALID, message, validationStep);
    }

    @NotNull
    private PnAuditLogEvent generateSkipAuditLog(NotificationInt notification, int validationStep, String detail) {
        String message = "Notification validation step {} of " + NUMBER_OF_STEPS + ". " + detail;

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