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
import it.pagopa.pn.deliverypushvalidator.action.utils.TimelineUtils;
import it.pagopa.pn.deliverypushvalidator.config.PnDeliveryPushValidatorConfigs;
import it.pagopa.pn.deliverypushvalidator.config.SendMoreThan20GramsParameterConsumer;
import it.pagopa.pn.deliverypushvalidator.dto.ext.addressmanager.NormalizeItemsResultInt;
import it.pagopa.pn.deliverypushvalidator.dto.ext.delivery.notification.*;
import it.pagopa.pn.deliverypushvalidator.dto.ext.safestorage.FileDownloadResponseInt;
import it.pagopa.pn.deliverypushvalidator.dto.timeline.TimelineElementInternal;
import it.pagopa.pn.deliverypushvalidator.exception.*;
import it.pagopa.pn.deliverypushvalidator.legalfact.DocumentComposition;
import it.pagopa.pn.deliverypushvalidator.middleware.queue.producer.abstractions.actionspool.ActionType;
import it.pagopa.pn.deliverypushvalidator.service.*;
import it.pagopa.pn.deliverypushvalidator.utils.NotificationCostServiceFeatureFlagUtils;
import lombok.CustomLog;
import org.jetbrains.annotations.NotNull;
import org.springframework.util.CollectionUtils;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

@CustomLog
public class LegalNotificationValidationStrategy extends BaseNotificationValidationStrategy implements NotificationValidationStrategy {

    private static final int FIRST_VALIDATION_STEP = 1;
    private static final int SECOND_VALIDATION_STEP = 2;
    private static final int THIRD_VALIDATION_STEP = 3;
    private static final int FOURTH_VALIDATION_STEP = 4;
    private static final int FIFTH_VALIDATION_STEP = 5;
    private static final String NOTIFICATION_IS_NOT_VALID_MSG = "Notification is not valid - iun={} ex={}";
    private static final String NOTIFICATION_IS_NOT_VALID_MSG_WITHOUT_EX = "Notification is not valid - iun={}";
    private final NotificationService notificationService;
    private final AuditLogService auditLogService;
    private final PnDeliveryPushValidatorConfigs cfg;
    private final TimelineService timelineService;
    private final TimelineUtils timelineUtils;
    private final LookupAddressHandler lookupAddressHandler;
    private final AddressValidator addressValidator;
    private final NormalizeAddressHandler normalizeAddressHandler;
    private final NotificationCostService notificationCostService;
    private final SchedulerService schedulerService;
    private final PaymentValidator paymentValidator;
    private final AttachmentUtils attachmentUtils;
    private final TaxIdPivaValidator taxIdPivaValidator;
    private final F24Validator f24Validator;
    private final NotificationValidationScheduler notificationValidationScheduler;

    //quickWorkaroundFor PN-9116
    private final NotificationCostServiceFeatureFlagUtils notificationCostServiceFeatureFlagUtils;
    private final SendMoreThan20GramsParameterConsumer parameterConsumer;
    private final SafeStorageService safeStorageService;
    private final DocumentComposition documentComposition;


    public LegalNotificationValidationStrategy(NotificationValidationScheduler notificationValidationScheduler, SchedulerService schedulerService, NotificationService notificationService, AuditLogService auditLogService, PnDeliveryPushValidatorConfigs cfg, TimelineService timelineService, TimelineUtils timelineUtils, LookupAddressHandler lookupAddressHandler, AddressValidator addressValidator, NormalizeAddressHandler normalizeAddressHandler, NotificationCostService notificationCostService, PaymentValidator paymentValidator, AttachmentUtils attachmentUtils, TaxIdPivaValidator taxIdPivaValidator, F24Validator f24Validator, NotificationCostServiceFeatureFlagUtils notificationCostServiceFeatureFlagUtils, SendMoreThan20GramsParameterConsumer parameterConsumer, SafeStorageService safeStorageService, DocumentComposition documentComposition) {
        super(notificationValidationScheduler, schedulerService);
        this.notificationService = notificationService;
        this.auditLogService = auditLogService;
        this.cfg = cfg;
        this.timelineService = timelineService;
        this.timelineUtils = timelineUtils;
        this.lookupAddressHandler = lookupAddressHandler;
        this.addressValidator = addressValidator;
        this.normalizeAddressHandler = normalizeAddressHandler;
        this.notificationCostService = notificationCostService;
        this.schedulerService = schedulerService;
        this.paymentValidator = paymentValidator;
        this.attachmentUtils = attachmentUtils;
        this.taxIdPivaValidator = taxIdPivaValidator;
        this.f24Validator = f24Validator;
        this.notificationValidationScheduler = notificationValidationScheduler;
        this.notificationCostServiceFeatureFlagUtils = notificationCostServiceFeatureFlagUtils;
        this.parameterConsumer = parameterConsumer;
        this.safeStorageService = safeStorageService;
        this.documentComposition = documentComposition;
    }


    @Override
    public NotificationInt getNotification(String iun) {
        log.debug("Start getNotification - iun={}", iun);
        return notificationService.getNotificationByIun(iun);
    }

    @Override
    public void validate(NotificationInt notification, NotificationValidationActionDetails details) {

        log.debug("Start validateNotification - iun={}", notification.getIun());
        PnAuditLogEvent logEvent = generateAuditLog(notification, FIRST_VALIDATION_STEP);

        try {
            paymentValidator.validatePayments(notification, details.getStartWorkflowTime());

            attachmentUtils.validateAttachment(notification);

            if (cfg.isCheckCfEnabled()) {
                taxIdPivaValidator.validateTaxIdPiva(notification);
            }

            //quickWorkAroundForPN-9116
            quickWorkAroundForPN9116(notification);

            if (f24Exists(notification)) {
                //La validazione del F24 è async
                MDCUtils.addMDCToContextAndExecute(
                        f24Validator.requestValidateF24(notification)
                ).block();
            } else {
                String detail = " F24 does not exists, so F24 validation will be skipped";
                generateSkipAuditLog(notification, SECOND_VALIDATION_STEP, detail).generateSuccess().log();

                verifyLookUpAddressAndNormalizeAddress(notification);
            }

            logEvent.generateSuccess().log();

        } catch (PnValidationFileNotFoundException ex) {
            if (cfg.isSafeStorageFileNotFoundRetry())
                logEvent.generateWarning("Validation need to be rescheduled - iun={} ex=", notification.getIun(), ex).log();
            handlePnValidationFileNotFoundException(notification.getIun(), details, notification, ex, details.getStartWorkflowTime());
        } catch (PnValidationException ex) {
            logEvent.generateWarning(NOTIFICATION_IS_NOT_VALID_MSG, notification.getIun(), ex).log();
            handleValidationError(notification, ex);
        } catch (RuntimeException ex) {
            logEvent.generateWarning("Validation need to be rescheduled - iun={} ex=", notification.getIun(), ex).log();
            handleRuntimeException(notification.getIun(), details, notification, ex, details.getStartWorkflowTime());
        }
    }

    @Override
    public void handleValidateF24Response(PnF24MetadataValidationEndEventPayload payload) {
        NotificationInt notification = notificationService.getNotificationByIun(payload.getSetId());
        PnAuditLogEvent logEvent = generateAuditLog(notification, SECOND_VALIDATION_STEP);
        try {
            if (!CollectionUtils.isEmpty(payload.getErrors())) {
                List<String> errors = payload.getErrors().stream()
                        .map(error -> "ERROR: " + error.getCode() + " \n" +
                                "ON ELEMENT: " + error.getElement() + " \n" +
                                "MESSAGE: " + error.getDetail())
                        .toList();
                throw new PnValidationNotValidF24Exception(errors);
            } else {
                timelineService.addTimelineElement(
                        timelineUtils.buildValidatedF24TimelineElement(notification),
                        notification
                );

                verifyLookUpAddressAndNormalizeAddress(notification);

                logEvent.generateSuccess().log();
            }
        } catch (PnValidationException e) {
            logEvent.generateWarning(NOTIFICATION_IS_NOT_VALID_MSG, notification.getIun(), e).log();
            handleValidationError(notification, e);
        }
    }

    @Override
    public void handleValidateAndNormalizeAddressResponse(String iun, NormalizeItemsResultInt normalizeItemsResult) {

        NotificationInt notification = getNotification(iun);
        PnAuditLogEvent logEvent = generateAuditLog(notification, FOURTH_VALIDATION_STEP);

        try {
            addressValidator.handleAddressValidation(iun, normalizeItemsResult);
            normalizeAddressHandler.handleNormalizedAddressResponse(notification, normalizeItemsResult);
            checkFeatureFlagAndInitializeAndValidateNotificationCost(iun, notification);
            logEvent.generateSuccess().log();

        } catch (PnValidationNotValidAddressException ex) {
            logEvent.generateWarning(NOTIFICATION_IS_NOT_VALID_MSG, notification.getIun(), ex).log();
            handleValidationError(notification, ex);
        }
    }

    @Override
    public void handleValidateNotificationCost(String iun, PnNotificationCostValidationEventPayload event) {

        NotificationInt notification = getNotification(iun);
        PnAuditLogEvent logEvent = generateAuditLog(notification, FIFTH_VALIDATION_STEP);
        processNotificationCostValidationResponse(iun, event, notification, logEvent);
    }

    @Override
    public void scheduleEndValidationAction(String iun) {

        Instant schedulingDate = Instant.now();
        log.debug("Scheduling received legalFact generation, schedulingDate={} - iun={}", schedulingDate, iun);
        schedulerService.scheduleEvent(iun, schedulingDate, ActionType.SCHEDULE_RECEIVED_LEGALFACT_GENERATION);
    }

    private PnAuditLogEvent generateAuditLog(NotificationInt notification, int validationStep) {
        String message = "Notification validation step {} of 5.";

        if (!cfg.isCheckCfEnabled()) {
            message += " TaxId validation will be skipped";
        }

        return auditLogService.buildAuditLogEvent(
                notification.getIun(),
                PnAuditLogEventType.AUD_NT_VALID,
                message,
                validationStep
        );
    }

    private void verifyLookUpAddressAndNormalizeAddress(NotificationInt notificationInt) {
        log.debug("Start verifyLookUpAddressAndNormalizeAddress - iun={}", notificationInt.getIun());

        if (timelineUtils.checkIsNotificationCancellationRequested(notificationInt.getIun())) {
            log.warn("Process blocked: cancellation requested for iun {}", notificationInt.getIun());
            return;
        }

        if (notificationInt.getUsedServices() != null && Boolean.TRUE.equals(notificationInt.getUsedServices().getPhysicalAddressLookUp())) {
            try {
                lookupAddressHandler.performValidation(notificationInt);
                NotificationInt refreshedNotification = notificationService.getNotificationByIun(notificationInt.getIun());
                MDCUtils.addMDCToContextAndExecute(
                        addressValidator.requestValidateAndNormalizeAddresses(refreshedNotification)
                ).block();
                generateAuditLog(notificationInt, THIRD_VALIDATION_STEP).generateSuccess().log();
            } catch (PnLookupAddressValidationFailedException ex) {
                log.warn(String.format("Lookup address validation failed - iun=%s", notificationInt.getIun()), ex);
                handleLookupAddressValidationError(notificationInt, ex);
            }
        } else {
            generateSkipAuditLog(notificationInt, THIRD_VALIDATION_STEP, "Lookup address validation will be skipped").generateSuccess().log();
            MDCUtils.addMDCToContextAndExecute(
                    addressValidator.requestValidateAndNormalizeAddresses(notificationInt)
            ).block();
        }
    }

    @NotNull
    private PnAuditLogEvent generateSkipAuditLog(NotificationInt notification, int validationStep, String detail) {
        String message = "Notification validation step {} of 5. " + detail;

        return auditLogService.buildAuditLogEvent(
                notification.getIun(),
                PnAuditLogEventType.AUD_NT_VALID,
                message,
                validationStep
        );
    }

    private void checkFeatureFlagAndInitializeAndValidateNotificationCost(String iun, NotificationInt notification) {
        if (notificationCostServiceFeatureFlagUtils.checkNotificationCostServiceStartDate(notification)) {
            notificationCostService.initializeAndValidateNotificationCost(notification);
        } else {
            scheduleEndValidationAction(iun);
            generateSkipAuditLog(notification, FIFTH_VALIDATION_STEP,
                    "Notification cost validation will be skipped due to feature flag").generateSuccess().log();
        }
    }

    private void processNotificationCostValidationResponse(String iun, PnNotificationCostValidationEventPayload event, NotificationInt notification, PnAuditLogEvent logEvent) {
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

    /**
     * quickWorkAroundForPN-9116
     */
    private void quickWorkAroundForPN9116(NotificationInt notification) {
        if (!canSendMoreThan20Grams(notification.getSender().getPaTaxId())) {
            final String errorDetail = String.format("Validation failed, sender paTaxId=%s can't send mail with more than 3 sheets (20 grams).", notification.getSender().getPaTaxId());
            if (haveSomePaymentsAttachment(notification)) {
                throw new PnValidationMoreThan20GramsException(errorDetail + " Payment attachments are disabled");
            }
            int numberOfDocuments = notification.getDocuments().size();
            switch (numberOfDocuments) {
                case 1 ->
                        checkDocumentsMaxPageNumber(notification, 4, "The attachment document exceed 4 pages. [paTaxId=%s, document=%s, actualPages=%s, maxPages=%s]");
                case 2 ->
                        checkDocumentsMaxPageNumber(notification, 2, "One of two documents exceed one sheet. [paTaxId=%s, document=%s, actualPages=%s, maxPages=%s]");
                default -> throw new PnValidationMoreThan20GramsException(errorDetail
                        + " " + numberOfDocuments +
                        " documents and an AAR exceed 3 sheets");
            }
        }
    }

    private boolean canSendMoreThan20Grams(String paTaxId) {
        return parameterConsumer.isPaEnabledToSendMoreThan20Grams(paTaxId);
    }

    private boolean haveSomePaymentsAttachment(NotificationInt notification) {
        return notification.getRecipients().stream()
                .filter(recipient -> !CollectionUtils.isEmpty(recipient.getPayments()))
                .anyMatch(recipient -> haveSomeF24Payments(recipient.getPayments()) || haveSomePagoPaPaymentsAttachment(recipient.getPayments()));
    }

    private boolean haveSomeF24Payments(List<NotificationPaymentInfoInt> payments) {
        return payments.stream().anyMatch(payment -> Objects.nonNull(payment.getF24()));
    }

    private boolean haveSomePagoPaPaymentsAttachment(List<NotificationPaymentInfoInt> payments) {
        return payments.stream().anyMatch(payment -> havePagoPaAttachment(payment.getPagoPA()));
    }

    private boolean havePagoPaAttachment(PagoPaInt pagoPaInt) {
        return Objects.nonNull(pagoPaInt.getAttachment());
    }

    private void checkDocumentsMaxPageNumber(NotificationInt notification, int maxPages, String messageFormat) {
        for (NotificationDocumentInt doc : notification.getDocuments()) {
            NotificationDocumentInt.Ref ref = doc.getRef();
            FileDownloadResponseInt fd = MDCUtils.addMDCToContextAndExecute(
                            safeStorageService.getFile(ref.getKey(), false)
                                    .onErrorResume(PnFileNotFoundException.class, this::handleNotFoundError))
                    .block();
            byte[] pieceOfContent = safeStorageService.downloadPieceOfContent(Objects.requireNonNull(fd).getKey(), fd.getDownload().getUrl(), -1).block();
            int actualPages = documentComposition.getNumberOfPageFromPdfBytes(pieceOfContent);
            if (actualPages > maxPages) {
                final String errorDetail = String.format(messageFormat,
                        notification.getSender().getPaTaxId(),
                        fd.getKey(),
                        actualPages,
                        maxPages
                );
                throw new PnValidationMoreThan20GramsException(errorDetail);
            }
        }
    }

    @NotNull
    private Mono<FileDownloadResponseInt> handleNotFoundError(PnFileNotFoundException ex) {
        return Mono.error(
                new PnValidationFileNotFoundException(
                        ex.getMessage(),
                        ex.getCause()
                )
        );
    }

    private boolean f24Exists(NotificationInt notification) {
        return notification.getRecipients()
                .stream()
                .map(NotificationRecipientInt::getPayments)
                .anyMatch(notificationPaymentInfoIntV2s -> !CollectionUtils.isEmpty(notificationPaymentInfoIntV2s)
                        && notificationPaymentInfoIntV2s
                        .stream()
                        .anyMatch(paymentInfoIntV2 -> paymentInfoIntV2.getF24() != null));
    }

    private void handlePnValidationFileNotFoundException(String iun, NotificationValidationActionDetails details, NotificationInt notification, PnValidationFileNotFoundException ex, Instant startWorkflowTime) {
    /* Per la PnValidationFileNotFoundException la notifica non viene portata in rifiutata MA è prevista una gestione ad hoc. Questo avviene
       perchè al momento non c'è possibilità di distinguere un 404 dovuto ad un mancato caricamento file da parte della PA (che dovrebbe portare
       regolarmente la notifica in rifiutata) e un 404 dovuto ad un ritardo nel caricamento del file nel bucket corretto da parte di
       safeStorage (in questo caso si di deve procedere con i ritentativi). Si sceglie dunque per ore di ritentare in entrambi i casi
    */
        log.warn(String.format("File not found exception in validateNotification - iun=%s", iun), ex);
        if (cfg.isSafeStorageFileNotFoundRetry()) {
            log.info("Notification validation need to be rescheduled  - iun={}", iun);
            notificationValidationScheduler.scheduleNotificationValidation(notification, details.getRetryAttempt(), ex, startWorkflowTime);
        } else {
            handleValidationError(notification, ex);
        }
    }
}
