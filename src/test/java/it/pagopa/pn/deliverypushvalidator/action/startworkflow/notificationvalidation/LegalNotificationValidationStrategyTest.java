package it.pagopa.pn.deliverypushvalidator.action.startworkflow.notificationvalidation;

import it.pagopa.pn.api.dto.events.PnF24MetadataValidationEndEventPayload;
import it.pagopa.pn.api.dto.events.PnF24MetadataValidationIssue;
import it.pagopa.pn.api.dto.events.notificationcost.utils.ValidationStatus;
import it.pagopa.pn.api.dto.events.notificationcost.validation.PnNotificationCostValidationEventPayload;
import it.pagopa.pn.commons.abstractions.ParameterConsumer;
import it.pagopa.pn.commons.exceptions.PnInternalException;
import it.pagopa.pn.commons.exceptions.dto.ProblemError;
import it.pagopa.pn.commons.log.PnAuditLogEvent;
import it.pagopa.pn.commons.log.PnAuditLogEventType;
import it.pagopa.pn.deliverypushvalidator.action.details.NotificationRefusedActionDetails;
import it.pagopa.pn.deliverypushvalidator.action.details.NotificationValidationActionDetails;
import it.pagopa.pn.deliverypushvalidator.action.it.utils.TestUtils;
import it.pagopa.pn.deliverypushvalidator.action.startworkflow.LookupAddressHandler;
import it.pagopa.pn.deliverypushvalidator.action.startworkflow.NormalizeAddressHandler;
import it.pagopa.pn.deliverypushvalidator.action.utils.TimelineUtils;
import it.pagopa.pn.deliverypushvalidator.config.PnDeliveryPushValidatorConfigs;
import it.pagopa.pn.deliverypushvalidator.config.SendMoreThan20GramsParameterConsumer;
import it.pagopa.pn.deliverypushvalidator.dto.address.PhysicalAddressInt;
import it.pagopa.pn.deliverypushvalidator.dto.ext.addressmanager.NormalizeItemsResultInt;
import it.pagopa.pn.deliverypushvalidator.dto.ext.addressmanager.NormalizeResultInt;
import it.pagopa.pn.deliverypushvalidator.dto.ext.delivery.notification.*;
import it.pagopa.pn.deliverypushvalidator.dto.ext.safestorage.FileDownloadInfoInt;
import it.pagopa.pn.deliverypushvalidator.dto.ext.safestorage.FileDownloadResponseInt;
import it.pagopa.pn.deliverypushvalidator.dto.timeline.TimelineElementInternal;
import it.pagopa.pn.deliverypushvalidator.exception.*;
import it.pagopa.pn.deliverypushvalidator.legalfact.DocumentComposition;
import it.pagopa.pn.deliverypushvalidator.middleware.queue.producer.abstractions.actionspool.ActionType;
import it.pagopa.pn.deliverypushvalidator.service.*;
import it.pagopa.pn.deliverypushvalidator.utils.NotificationCostServiceFeatureFlagUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class LegalNotificationValidationStrategyTest {

    @Mock
    private AttachmentUtils attachmentUtils;
    @Mock
    private TaxIdPivaValidator taxIdPivaValidator;
    @Mock
    private TimelineService timelineService;
    @Mock
    private TimelineUtils timelineUtils;
    @Mock
    private NotificationService notificationService;
    @Mock
    private NotificationValidationScheduler notificationValidationScheduler;
    @Mock
    private AddressValidator addressValidator;

    @Mock
    private F24Validator f24Validator;
    @Mock
    private AuditLogService auditLogService;
    @Mock
    private NormalizeAddressHandler normalizeAddressHandler;
    @Mock
    private SchedulerService schedulerService;
    @Mock
    private PaymentValidator paymentValidator;

    private LegalNotificationValidationStrategy handler;
    @Mock
    private PnDeliveryPushValidatorConfigs cfg;

    @Mock
    private SafeStorageService safeStorageService;

    @Mock
    private DocumentComposition documentComposition;

    @Mock
    private LookupAddressHandler lookupAddressHandler;

    @Mock
    private NotificationCostService notificationCostService;

    @Mock
    private NotificationCostServiceFeatureFlagUtils notificationCostServiceFeatureFlagUtils;

    private static final String IUN = "TEST-IUN-001";


    @BeforeEach
    void setup() {
        //quickWorkAroundForPN-9116
        ParameterConsumer parameterConsumerMock = Mockito.mock(ParameterConsumer.class);
        SendMoreThan20GramsParameterConsumer sendMoreThan20GramsParameterConsumer = new SendMoreThan20GramsParameterConsumer(parameterConsumerMock, cfg);
        handler = new LegalNotificationValidationStrategy(notificationValidationScheduler, schedulerService, notificationService,
                auditLogService, cfg, timelineService, timelineUtils, lookupAddressHandler, addressValidator,
                normalizeAddressHandler, notificationCostService, paymentValidator, attachmentUtils, taxIdPivaValidator,
                f24Validator, notificationCostServiceFeatureFlagUtils,
                sendMoreThan20GramsParameterConsumer, safeStorageService, documentComposition);
    }

    @ExtendWith(SpringExtension.class)
    @Test
    void validateNotificationOK() {
        //GIVEN
        Mockito.when(cfg.isCheckCfEnabled())
                .thenReturn(true);
        // quickWorkAroundForPN-9116
        Mockito.when(cfg.isSendMoreThan20GramsDefaultValue())
                .thenReturn(true);

        NotificationInt notification = TestUtils.getNotification();
        Mockito.when(notificationService.getNotificationByIun(Mockito.anyString()))
                .thenReturn(notification);

        NotificationValidationActionDetails details = NotificationValidationActionDetails.builder()
                .retryAttempt(1)
                .build();

        PnAuditLogEvent auditLogEvent = Mockito.mock(PnAuditLogEvent.class);
        Mockito.when(auditLogService.buildAuditLogEvent(Mockito.eq(notification.getIun()), Mockito.eq(PnAuditLogEventType.AUD_NT_VALID), Mockito.anyString(), any()))
                .thenReturn(auditLogEvent);
        Mockito.when(auditLogEvent.generateSuccess()).thenReturn(auditLogEvent);

        Mockito.when(addressValidator.requestValidateAndNormalizeAddresses(notification)).thenReturn(Mono.empty());

        //WHEN
        handler.validate(IUN, details);

        //THEN
        Mockito.verify(attachmentUtils).validateAttachment(notification);
        Mockito.verify(auditLogEvent, times(3)).generateSuccess();
        Mockito.verify(notificationValidationScheduler, Mockito.never()).scheduleNotificationValidation(Mockito.eq(notification), Mockito.anyInt(), any(), Mockito.any(Instant.class));

    }

    // quickWorkAroundForPN-9116
    @ExtendWith(SpringExtension.class)
    @Test
    void validateNotificationKO() {
        //GIVEN
        Mockito.when(cfg.isCheckCfEnabled())
                .thenReturn(true);

        Mockito.when(cfg.isSendMoreThan20GramsDefaultValue())
                .thenReturn(false);

        NotificationInt notificationBefore = TestUtils.getNotification();
        NotificationInt notification = notificationBefore.toBuilder().documents(List.of(NotificationDocumentInt.builder()
                        .ref(NotificationDocumentInt.Ref.builder()
                                .key("key")
                                .build())
                        .build()))
                .build();
        Mockito.when(notificationService.getNotificationByIun(Mockito.anyString()))
                .thenReturn(notification);

        NotificationValidationActionDetails details = NotificationValidationActionDetails.builder()
                .retryAttempt(1)
                .build();

        PnAuditLogEvent auditLogEvent = Mockito.mock(PnAuditLogEvent.class);
        Mockito.when(auditLogService.buildAuditLogEvent(Mockito.eq(notification.getIun()), Mockito.eq(PnAuditLogEventType.AUD_NT_VALID), Mockito.anyString(), any()))
                .thenReturn(auditLogEvent);
        Mockito.when(auditLogEvent.generateSuccess()).thenReturn(auditLogEvent);

        Mockito.when(addressValidator.requestValidateAndNormalizeAddresses(notification)).thenReturn(Mono.empty());

        Mockito.when(safeStorageService.getFile(Mockito.anyString(), Mockito.anyBoolean())).thenReturn(Mono.just(FileDownloadResponseInt.builder()
                .key("key")
                .checksum("sha256")
                .contentLength(BigDecimal.TEN)
                .download(FileDownloadInfoInt.builder()
                        .url("url")
                        .build())
                .contentType("contentType")
                .build()));
        Mockito.when(safeStorageService.downloadPieceOfContent(Mockito.anyString(), Mockito.anyString(), Mockito.anyLong())).thenReturn(downloadPieceOfContent());

        Mockito.when(documentComposition.getNumberOfPageFromPdfBytes(Mockito.any())).thenReturn(5);

        PnAuditLogEvent pnAuditLogEventWarn = Mockito.mock(PnAuditLogEvent.class);
        Mockito.when(auditLogEvent.generateWarning(Mockito.anyString(), any(String.class), any())).thenReturn(pnAuditLogEventWarn);
        //WHEN
        handler.validate(IUN, details);

        Mockito.verify(notificationValidationScheduler, Mockito.never()).scheduleNotificationValidation(Mockito.eq(notification), Mockito.anyInt(), any(), Mockito.any(Instant.class));

    }

    // quickWorkAroundForPN-9116
    @ExtendWith(SpringExtension.class)
    @Test
    void validateNotificationOKWithPaymentNoAttachment() {
        //GIVEN
        Mockito.when(cfg.isCheckCfEnabled())
                .thenReturn(true);
        // quickWorkAroundForPN-9116
        Mockito.when(cfg.isSendMoreThan20GramsDefaultValue())
                .thenReturn(false);

        NotificationInt notificationBefore = TestUtils.getNotification();
        NotificationInt notification = notificationBefore.toBuilder()
                .documents(List.of(NotificationDocumentInt.builder()
                        .ref(NotificationDocumentInt.Ref.builder()
                                .key("key")
                                .build())
                        .build())
                )
                .recipients(List.of(NotificationRecipientInt.builder()
                        .payments(List.of(NotificationPaymentInfoInt.builder()
                                .pagoPA(PagoPaInt.builder()
                                        .creditorTaxId("creditorTaxId")
                                        .noticeCode("noticeCode")
                                        .build())
                                .build()))
                        .build()))
                .build();

        Mockito.when(notificationService.getNotificationByIun(Mockito.anyString()))
                .thenReturn(notification);

        NotificationValidationActionDetails details = NotificationValidationActionDetails.builder()
                .retryAttempt(1)
                .build();

        PnAuditLogEvent auditLogEvent = Mockito.mock(PnAuditLogEvent.class);
        Mockito.when(auditLogService.buildAuditLogEvent(Mockito.eq(notification.getIun()), Mockito.eq(PnAuditLogEventType.AUD_NT_VALID), Mockito.anyString(), any()))
                .thenReturn(auditLogEvent);
        Mockito.when(auditLogEvent.generateSuccess()).thenReturn(auditLogEvent);

        Mockito.when(addressValidator.requestValidateAndNormalizeAddresses(notification)).thenReturn(Mono.empty());

        Mockito.when(safeStorageService.getFile(Mockito.anyString(), Mockito.anyBoolean())).thenReturn(Mono.just(FileDownloadResponseInt.builder()
                .key("key")
                .checksum("sha256")
                .contentLength(BigDecimal.TEN)
                .download(FileDownloadInfoInt.builder()
                        .url("url")
                        .build())
                .contentType("contentType")
                .build()));
        Mockito.when(safeStorageService.downloadPieceOfContent(Mockito.anyString(), Mockito.anyString(), Mockito.anyLong())).thenReturn(downloadPieceOfContent());

        Mockito.when(documentComposition.getNumberOfPageFromPdfBytes(Mockito.any())).thenReturn(1);

        //WHEN
        handler.validate(IUN, details);

        //THEN
        Mockito.verify(attachmentUtils).validateAttachment(notification);
        Mockito.verify(auditLogEvent, times(3)).generateSuccess();
        Mockito.verify(notificationValidationScheduler, Mockito.never()).scheduleNotificationValidation(Mockito.eq(notification), Mockito.anyInt(), any(), Mockito.any(Instant.class));

    }

    // quickWorkAroundForPN-9116
    @ExtendWith(SpringExtension.class)
    @Test
    void validateNotificationKOWithPayment() {
        //GIVEN
        Mockito.when(cfg.isCheckCfEnabled())
                .thenReturn(true);

        Mockito.when(cfg.isSendMoreThan20GramsDefaultValue())
                .thenReturn(false);

        NotificationInt notificationBefore = TestUtils.getNotification();
        NotificationInt notification = notificationBefore.toBuilder()
                .recipients(List.of(NotificationRecipientInt.builder()
                        .payments(List.of(NotificationPaymentInfoInt.builder()
                                .pagoPA(PagoPaInt.builder()
                                        .creditorTaxId("creditorTaxId")
                                        .noticeCode("noticeCode")
                                        .attachment(NotificationDocumentInt.builder().build())
                                        .build())
                                .build()))
                        .build()))
                .build();
        Mockito.when(notificationService.getNotificationByIun(Mockito.anyString()))
                .thenReturn(notification);

        NotificationValidationActionDetails details = NotificationValidationActionDetails.builder()
                .retryAttempt(1)
                .build();

        PnAuditLogEvent auditLogEvent = Mockito.mock(PnAuditLogEvent.class);
        Mockito.when(auditLogService.buildAuditLogEvent(Mockito.eq(notification.getIun()), Mockito.eq(PnAuditLogEventType.AUD_NT_VALID), Mockito.anyString(), any()))
                .thenReturn(auditLogEvent);
        Mockito.when(auditLogEvent.generateSuccess()).thenReturn(auditLogEvent);

        PnAuditLogEvent pnAuditLogEventWarn = Mockito.mock(PnAuditLogEvent.class);
        Mockito.when(auditLogEvent.generateWarning(Mockito.anyString(), any(String.class), any())).thenReturn(pnAuditLogEventWarn);
        //WHEN
        handler.validate(IUN, details);

        Mockito.verify(notificationValidationScheduler, Mockito.never()).scheduleNotificationValidation(Mockito.eq(notification), Mockito.anyInt(), any(), Mockito.any(Instant.class));

    }

    // quickWorkAroundForPN-9116
    @ExtendWith(SpringExtension.class)
    @Test
    void validateNotificationKOWithF24() {
        //GIVEN
        Mockito.when(cfg.isCheckCfEnabled())
                .thenReturn(true);

        Mockito.when(cfg.isSendMoreThan20GramsDefaultValue())
                .thenReturn(false);

        NotificationInt notificationBefore = TestUtils.getNotification();
        NotificationInt notification = notificationBefore.toBuilder()
                .recipients(List.of(NotificationRecipientInt.builder()
                        .payments(List.of(NotificationPaymentInfoInt.builder()
                                .f24(F24Int.builder().build())
                                .build()))
                        .build()))
                .build();
        Mockito.when(notificationService.getNotificationByIun(Mockito.anyString()))
                .thenReturn(notification);

        NotificationValidationActionDetails details = NotificationValidationActionDetails.builder()
                .retryAttempt(1)
                .build();

        PnAuditLogEvent auditLogEvent = Mockito.mock(PnAuditLogEvent.class);
        Mockito.when(auditLogService.buildAuditLogEvent(Mockito.eq(notification.getIun()), Mockito.eq(PnAuditLogEventType.AUD_NT_VALID), Mockito.anyString(), any()))
                .thenReturn(auditLogEvent);
        Mockito.when(auditLogEvent.generateSuccess()).thenReturn(auditLogEvent);

        PnAuditLogEvent pnAuditLogEventWarn = Mockito.mock(PnAuditLogEvent.class);
        Mockito.when(auditLogEvent.generateWarning(Mockito.anyString(), any(String.class), any())).thenReturn(pnAuditLogEventWarn);
        //WHEN
        handler.validate(IUN, details);
        Mockito.verify(notificationValidationScheduler, Mockito.never()).scheduleNotificationValidation(Mockito.eq(notification), Mockito.anyInt(), any(), Mockito.any(Instant.class));

    }

    private Mono<byte[]> downloadPieceOfContent() {
        byte[] res = new byte[8];
        res[0] = 0x25;
        res[1] = 0x50;
        res[2] = 0x44;
        res[3] = 0x46;
        res[4] = 0x2D;
        res[5] = 0x2D;
        res[6] = 0x2D;
        res[7] = 0x2D;

        return Mono.just(res);
    }

    @ExtendWith(SpringExtension.class)
    @Test
    void validateNotificationOKF24() {
        //GIVEN
        Mockito.when(cfg.isCheckCfEnabled())
                .thenReturn(true);
        // quickWorkAroundForPN-9116
        Mockito.when(cfg.isSendMoreThan20GramsDefaultValue())
                .thenReturn(true);

        NotificationInt notification = TestUtils.getNotificationV2WithF24();
        Mockito.when(notificationService.getNotificationByIun(Mockito.anyString()))
                .thenReturn(notification);

        NotificationValidationActionDetails details = NotificationValidationActionDetails.builder()
                .retryAttempt(1)
                .build();

        PnAuditLogEvent auditLogEvent = Mockito.mock(PnAuditLogEvent.class);
        Mockito.when(auditLogService.buildAuditLogEvent(Mockito.eq(notification.getIun()), Mockito.eq(PnAuditLogEventType.AUD_NT_VALID), Mockito.anyString(), any()))
                .thenReturn(auditLogEvent);
        Mockito.when(auditLogEvent.generateSuccess()).thenReturn(auditLogEvent);

        Mockito.when(f24Validator.requestValidateF24(notification)).thenReturn(Mono.empty());

        //WHEN
        handler.validate(IUN, details);
        //THEN
        Mockito.verify(attachmentUtils).validateAttachment(notification);
        Mockito.verify(auditLogEvent).generateSuccess();
        Mockito.verify(notificationValidationScheduler, Mockito.never()).scheduleNotificationValidation(Mockito.eq(notification), Mockito.anyInt(), any(), Mockito.any(Instant.class));

    }

    @ExtendWith(SpringExtension.class)
    @Test
    void validateNotificationKONotFound_isSafeStorageFileNotFoundRetry_true() {
        //GIVEN
        Mockito.when(cfg.isCheckCfEnabled())
                .thenReturn(true);

        NotificationInt notification = TestUtils.getNotification();
        Mockito.when(cfg.isSafeStorageFileNotFoundRetry())
                .thenReturn(true);
        Mockito.when(notificationService.getNotificationByIun(Mockito.anyString()))
                .thenReturn(notification);
        PnValidationFileNotFoundException ex = new PnValidationFileNotFoundException("detail", new RuntimeException());
        doThrow(ex).when(attachmentUtils).validateAttachment(notification);

        NotificationValidationActionDetails details = NotificationValidationActionDetails.builder()
                .retryAttempt(1)
                .startWorkflowTime(Instant.now())
                .build();

        TimelineElementInternal timelineElementInternal = TimelineElementInternal.builder().build();
        Mockito.when(timelineUtils.buildRefusedRequestTimelineElement(any(NotificationInt.class), any(), any()))
                .thenReturn(timelineElementInternal);

        PnAuditLogEvent auditLogEvent = Mockito.mock(PnAuditLogEvent.class);
        Mockito.when(auditLogService.buildAuditLogEvent(Mockito.eq(notification.getIun()), Mockito.eq(PnAuditLogEventType.AUD_NT_VALID), Mockito.anyString(), any()))
                .thenReturn(auditLogEvent);
        Mockito.when(auditLogEvent.generateWarning(Mockito.anyString(), any(String.class), any())).thenReturn(auditLogEvent);

        //WHEN
        handler.validate(IUN, details);
        //THEN
        Mockito.verify(notificationValidationScheduler).scheduleNotificationValidation(notification, details.getRetryAttempt(), ex, details.getStartWorkflowTime());
        Mockito.verify(auditLogEvent).generateWarning(any(), any(String.class), any());
        Mockito.verify(addressValidator, Mockito.never()).requestValidateAndNormalizeAddresses(notification);
    }

    @ExtendWith(SpringExtension.class)
    @Test
    void validateNotificationKONotFound_isSafeStorageFileNotFoundRetry_false() {
        //GIVEN
        Mockito.when(cfg.isCheckCfEnabled())
                .thenReturn(true);
        NotificationInt notification = TestUtils.getNotification();
        Mockito.when(cfg.isSafeStorageFileNotFoundRetry())
                .thenReturn(false);
        Mockito.when(notificationService.getNotificationByIun(Mockito.anyString()))
                .thenReturn(notification);
        PnValidationFileNotFoundException ex = new PnValidationFileNotFoundException("detail", new RuntimeException());
        doThrow(ex).when(attachmentUtils).validateAttachment(notification);

        NotificationValidationActionDetails details = NotificationValidationActionDetails.builder()
                .retryAttempt(1)
                .startWorkflowTime(Instant.now())
                .build();

        TimelineElementInternal timelineElementInternal = TimelineElementInternal.builder().build();
        Mockito.when(timelineUtils.buildRefusedRequestTimelineElement(any(NotificationInt.class), any(), any()))
                .thenReturn(timelineElementInternal);

        PnAuditLogEvent auditLogEvent = Mockito.mock(PnAuditLogEvent.class);
        Mockito.when(auditLogService.buildAuditLogEvent(Mockito.eq(notification.getIun()), Mockito.eq(PnAuditLogEventType.AUD_NT_VALID), Mockito.anyString(), any()))
                .thenReturn(auditLogEvent);
        Mockito.when(auditLogEvent.generateWarning(Mockito.anyString(), any())).thenReturn(auditLogEvent);

        //WHEN
        handler.validate(IUN, details);
        //THEN
        Mockito.verify(addressValidator, Mockito.never()).requestValidateAndNormalizeAddresses(notification);
        Mockito.verify(notificationValidationScheduler, Mockito.never()).scheduleNotificationValidation(notification, details.getRetryAttempt(), ex, details.getStartWorkflowTime());
        Mockito.verify(auditLogEvent, Mockito.never()).generateWarning(any(), any());
    }

    @ExtendWith(SpringExtension.class)
    @Test
    void validateNotificationKOFileShaError() {
        //GIVEN
        Mockito.when(cfg.isCheckCfEnabled())
                .thenReturn(true);

        NotificationInt notification = TestUtils.getNotification();
        Mockito.when(notificationService.getNotificationByIun(Mockito.anyString()))
                .thenReturn(notification);
        doThrow(new PnValidationNotMatchingShaException("detail")).when(attachmentUtils).validateAttachment(notification);

        NotificationValidationActionDetails details = NotificationValidationActionDetails.builder()
                .retryAttempt(1)
                .build();

        PnAuditLogEvent auditLogEvent = Mockito.mock(PnAuditLogEvent.class);
        Mockito.when(auditLogService.buildAuditLogEvent(Mockito.eq(notification.getIun()), Mockito.eq(PnAuditLogEventType.AUD_NT_VALID), Mockito.anyString(), any()))
                .thenReturn(auditLogEvent);
        Mockito.when(auditLogEvent.generateWarning(Mockito.anyString(), any(String.class), any())).thenReturn(auditLogEvent);

        //WHEN
        handler.validate(IUN, details);
        //THEN
        Mockito.verify(addressValidator, Mockito.never()).requestValidateAndNormalizeAddresses(notification);
        Mockito.verify(schedulerService).scheduleEvent(Mockito.eq(notification.getIun()), Mockito.any(Instant.class),
                Mockito.eq(ActionType.NOTIFICATION_REFUSED), Mockito.any(NotificationRefusedActionDetails.class), eq(null));
        Mockito.verify(auditLogEvent).generateWarning(any(), any(String.class), any());
        Mockito.verify(notificationValidationScheduler, Mockito.never()).scheduleNotificationValidation(Mockito.eq(notification), Mockito.anyInt(), any(), Mockito.any(Instant.class));
    }

    @ExtendWith(SpringExtension.class)
    @Test
    void validateNotificationKOTaxIdNotValid() {
        //GIVEN
        Mockito.when(cfg.isCheckCfEnabled())
                .thenReturn(true);

        NotificationInt notification = TestUtils.getNotification();
        Mockito.when(notificationService.getNotificationByIun(Mockito.anyString()))
                .thenReturn(notification);
        doThrow(new PnValidationTaxIdNotValidException("detail")).when(taxIdPivaValidator).validateTaxIdPiva(notification);

        NotificationValidationActionDetails details = NotificationValidationActionDetails.builder()
                .retryAttempt(1)
                .build();

        PnAuditLogEvent auditLogEvent = Mockito.mock(PnAuditLogEvent.class);
        Mockito.when(auditLogService.buildAuditLogEvent(Mockito.eq(notification.getIun()), Mockito.eq(PnAuditLogEventType.AUD_NT_VALID), Mockito.anyString(), any()))
                .thenReturn(auditLogEvent);
        Mockito.when(auditLogEvent.generateWarning(Mockito.anyString(), any(String.class), any())).thenReturn(auditLogEvent);

        //WHEN
        handler.validate(IUN, details);
        //THEN
        Mockito.verify(addressValidator, Mockito.never()).requestValidateAndNormalizeAddresses(notification);
        Mockito.verify(schedulerService).scheduleEvent(Mockito.eq(notification.getIun()), Mockito.any(Instant.class),
                Mockito.eq(ActionType.NOTIFICATION_REFUSED), Mockito.any(NotificationRefusedActionDetails.class), eq(null));
        Mockito.verify(auditLogEvent).generateWarning(any(), any(String.class), any());
        Mockito.verify(notificationValidationScheduler, Mockito.never()).scheduleNotificationValidation(Mockito.eq(notification), Mockito.anyInt(), any(), Mockito.any(Instant.class));
    }

    @ExtendWith(SpringExtension.class)
    @Test
    void validateNotificationTaxIdSkipped_withoutPhysicalAddressLookUp() {
        UsedServicesInt usedServices = UsedServicesInt.builder()
                .physicalAddressLookUp(false)
                .build();
        //GIVEN
        Mockito.when(cfg.isCheckCfEnabled())
                .thenReturn(false);
        Mockito.when(cfg.isSendMoreThan20GramsDefaultValue())
                .thenReturn(true);

        NotificationInt notification = TestUtils.getNotificationV2(usedServices);
        Mockito.when(notificationService.getNotificationByIun(Mockito.anyString()))
                .thenReturn(notification);

        NotificationValidationActionDetails details = NotificationValidationActionDetails.builder()
                .retryAttempt(1)
                .build();

        TimelineElementInternal timelineElementInternal = TimelineElementInternal.builder().build();
        Mockito.when(timelineUtils.buildRefusedRequestTimelineElement(any(NotificationInt.class), any(), any()))
                .thenReturn(timelineElementInternal);

        PnAuditLogEvent auditLogEvent = Mockito.mock(PnAuditLogEvent.class);
        Mockito.when(auditLogEvent.generateSuccess()).thenReturn(auditLogEvent);
        Mockito.when(auditLogService.buildAuditLogEvent(Mockito.anyString(), any(), any(), any()))
                .thenReturn(auditLogEvent);

        Mockito.when(auditLogEvent.generateWarning(Mockito.anyString(), any())).thenReturn(auditLogEvent);

        Mockito.when(addressValidator.requestValidateAndNormalizeAddresses(notification)).thenReturn(Mono.empty());

        //WHEN
        handler.validate(IUN, details);
        //THEN
        Mockito.verify(addressValidator).requestValidateAndNormalizeAddresses(notification);
        Mockito.verify(timelineService, Mockito.never()).addTimelineElement(timelineElementInternal, notification);
        Mockito.verify(taxIdPivaValidator, Mockito.never()).validateTaxIdPiva(notification);
        Mockito.verify(notificationValidationScheduler, Mockito.never()).scheduleNotificationValidation(Mockito.eq(notification), Mockito.anyInt(), any(), Mockito.any(Instant.class));
    }

    @ExtendWith(SpringExtension.class)
    @Test
    void validateNotificationTaxIdSkipped_withPhysicalAddressLookUp() {
        UsedServicesInt usedServices = UsedServicesInt.builder()
                .physicalAddressLookUp(true)
                .build();
        //GIVEN
        Mockito.when(cfg.isCheckCfEnabled())
                .thenReturn(false);
        Mockito.when(cfg.isSendMoreThan20GramsDefaultValue())
                .thenReturn(true);

        NotificationInt notification = TestUtils.getNotificationV2(usedServices);
        Mockito.when(notificationService.getNotificationByIun(Mockito.anyString()))
                .thenReturn(notification);

        NotificationValidationActionDetails details = NotificationValidationActionDetails.builder()
                .retryAttempt(1)
                .build();

        PnAuditLogEvent auditLogEvent = Mockito.mock(PnAuditLogEvent.class);
        Mockito.when(auditLogEvent.generateSuccess()).thenReturn(auditLogEvent);
        Mockito.when(auditLogService.buildAuditLogEvent(Mockito.anyString(), any(), any(), any()))
                .thenReturn(auditLogEvent);

        Mockito.when(auditLogEvent.generateWarning(Mockito.anyString(), any())).thenReturn(auditLogEvent);

        Mockito.when(addressValidator.requestValidateAndNormalizeAddresses(notification)).thenReturn(Mono.empty());

        //WHEN
        handler.validate(IUN, details);
        //THEN
        Mockito.verify(addressValidator).requestValidateAndNormalizeAddresses(notification);
        Mockito.verify(taxIdPivaValidator, Mockito.never()).validateTaxIdPiva(notification);
        Mockito.verify(notificationValidationScheduler, Mockito.never()).scheduleNotificationValidation(Mockito.eq(notification), Mockito.anyInt(), any(), Mockito.any(Instant.class));
    }

    @ExtendWith(SpringExtension.class)
    @Test
    void validateNotificationErrorCheckRetry() {
        //GIVEN
        Mockito.when(cfg.isCheckCfEnabled())
                .thenReturn(true);

        NotificationInt notification = TestUtils.getNotification();
        Mockito.when(notificationService.getNotificationByIun(Mockito.anyString()))
                .thenReturn(notification);

        //Simulazione runtimeException generica (servizio non risponde ecc)
        RuntimeException ex = new RuntimeException();
        doThrow(ex).when(attachmentUtils).validateAttachment(notification);

        NotificationValidationActionDetails details = NotificationValidationActionDetails.builder()
                .retryAttempt(1)
                .startWorkflowTime(Instant.now())
                .build();

        TimelineElementInternal timelineElementInternal = TimelineElementInternal.builder().build();
        Mockito.when(timelineUtils.buildRefusedRequestTimelineElement(any(NotificationInt.class), any(), any()))
                .thenReturn(timelineElementInternal);

        PnAuditLogEvent auditLogEvent = Mockito.mock(PnAuditLogEvent.class);
        Mockito.when(auditLogService.buildAuditLogEvent(Mockito.eq(notification.getIun()), Mockito.eq(PnAuditLogEventType.AUD_NT_VALID), Mockito.anyString(), any()))
                .thenReturn(auditLogEvent);
        Mockito.when(auditLogEvent.generateWarning(Mockito.anyString(), any(String.class), any())).thenReturn(auditLogEvent);

        //WHEN
        handler.validate(IUN, details);
        //THEN
        Mockito.verify(addressValidator, Mockito.never()).requestValidateAndNormalizeAddresses(notification);
        Mockito.verify(notificationValidationScheduler).scheduleNotificationValidation(notification, details.getRetryAttempt(), ex, details.getStartWorkflowTime());
        Mockito.verify(auditLogEvent).generateWarning(any(), any(String.class), any());
    }

    @ExtendWith(SpringExtension.class)
    @Test
    void handleValidateF24Response_withPhysicalAddressLookUp() {
        UsedServicesInt usedServices = UsedServicesInt.builder()
                .physicalAddressLookUp(true)
                .build();
        NotificationInt notification = TestUtils.getNotificationV2WithF24(usedServices);
        Mockito.when(notificationService.getNotificationByIun(Mockito.anyString()))
                .thenReturn(notification);

        PnAuditLogEvent auditLogEvent = Mockito.mock(PnAuditLogEvent.class);
        Mockito.when(auditLogService.buildAuditLogEvent(Mockito.eq(notification.getIun()), Mockito.eq(PnAuditLogEventType.AUD_NT_VALID), Mockito.anyString(), any()))
                .thenReturn(auditLogEvent);
        Mockito.when(auditLogEvent.generateSuccess()).thenReturn(auditLogEvent);

        when(timelineUtils.buildValidateF24RequestTimelineElement(any()))
                .thenReturn(TimelineElementInternal.builder().build());
        when(addressValidator.requestValidateAndNormalizeAddresses(notification)).thenReturn(Mono.empty());

        PnF24MetadataValidationEndEventPayload pnF24MetadataValidationEndEventPayload = PnF24MetadataValidationEndEventPayload.builder()
                .setId(notification.getIun())
                .status("ok")
                .errors(Collections.emptyList())
                .build();
        //WHEN
        Assertions.assertDoesNotThrow(() -> handler.handleValidateF24Response(pnF24MetadataValidationEndEventPayload));
    }

    @ExtendWith(SpringExtension.class)
    @Test
    void handleValidateF24Response_withoutPhysicalAddressLookUp() {
        UsedServicesInt usedServices = UsedServicesInt.builder()
                .physicalAddressLookUp(false)
                .build();
        NotificationInt notification = TestUtils.getNotificationV2WithF24(usedServices);
        Mockito.when(notificationService.getNotificationByIun(Mockito.anyString()))
                .thenReturn(notification);

        PnAuditLogEvent auditLogEvent = Mockito.mock(PnAuditLogEvent.class);
        Mockito.when(auditLogService.buildAuditLogEvent(Mockito.eq(notification.getIun()), Mockito.eq(PnAuditLogEventType.AUD_NT_VALID), Mockito.anyString(), any()))
                .thenReturn(auditLogEvent);
        Mockito.when(auditLogEvent.generateSuccess()).thenReturn(auditLogEvent);

        when(timelineUtils.buildValidateF24RequestTimelineElement(any()))
                .thenReturn(TimelineElementInternal.builder().build());
        when(addressValidator.requestValidateAndNormalizeAddresses(notification)).thenReturn(Mono.empty());

        PnF24MetadataValidationEndEventPayload pnF24MetadataValidationEndEventPayload = PnF24MetadataValidationEndEventPayload.builder()
                .setId(notification.getIun())
                .status("ok")
                .errors(Collections.emptyList())
                .build();
        //WHEN
        Assertions.assertDoesNotThrow(() -> handler.handleValidateF24Response(pnF24MetadataValidationEndEventPayload));
    }

    @ExtendWith(SpringExtension.class)
    @Test
    void handleValidateF24ResponseError() {
        NotificationInt notification = TestUtils.getNotificationV2WithF24();
        Mockito.when(notificationService.getNotificationByIun(Mockito.anyString()))
                .thenReturn(notification);

        PnAuditLogEvent auditLogEvent = Mockito.mock(PnAuditLogEvent.class);
        Mockito.when(auditLogService.buildAuditLogEvent(Mockito.eq(notification.getIun()), Mockito.eq(PnAuditLogEventType.AUD_NT_VALID), Mockito.anyString(), any()))
                .thenReturn(auditLogEvent);
        Mockito.when(auditLogEvent.generateWarning(Mockito.anyString(), any(String.class), any())).thenReturn(auditLogEvent);

        PnF24MetadataValidationIssue validationIssue = PnF24MetadataValidationIssue.builder()
                .detail("error detail")
                .build();

        PnF24MetadataValidationEndEventPayload pnF24MetadataValidationEndEventPayload = PnF24MetadataValidationEndEventPayload.builder()
                .setId(notification.getIun())
                .status("ko")
                .errors(List.of(validationIssue))
                .build();

        //WHEN
        Assertions.assertDoesNotThrow(() -> handler.handleValidateF24Response(pnF24MetadataValidationEndEventPayload));
    }

    @ExtendWith(SpringExtension.class)
    @Test
    void validateNotificationPaymentRetry() {
        //GIVEN
        Mockito.when(cfg.isCheckCfEnabled())
                .thenReturn(true);

        NotificationInt notification = TestUtils.getNotification();
        Mockito.when(notificationService.getNotificationByIun(Mockito.anyString()))
                .thenReturn(notification);

        //Simulazione errore che necessita retry
        PnPaymentUpdateRetryException ex = new PnPaymentUpdateRetryException("error");
        doThrow(ex).when(paymentValidator).validatePayments(Mockito.any(NotificationInt.class), Mockito.any(Instant.class));

        NotificationValidationActionDetails details = NotificationValidationActionDetails.builder()
                .retryAttempt(1)
                .startWorkflowTime(Instant.now())
                .build();

        PnAuditLogEvent auditLogEvent = Mockito.mock(PnAuditLogEvent.class);
        Mockito.when(auditLogService.buildAuditLogEvent(Mockito.eq(notification.getIun()), Mockito.eq(PnAuditLogEventType.AUD_NT_VALID), Mockito.anyString(), any()))
                .thenReturn(auditLogEvent);
        Mockito.when(auditLogEvent.generateWarning(Mockito.anyString(), any(String.class), any())).thenReturn(auditLogEvent);

        //WHEN
        handler.validate(IUN, details);
        //THEN
        Mockito.verify(addressValidator, Mockito.never()).requestValidateAndNormalizeAddresses(notification);
        Mockito.verify(notificationValidationScheduler).scheduleNotificationValidation(notification, details.getRetryAttempt(), ex, details.getStartWorkflowTime());
        Mockito.verify(auditLogEvent).generateWarning(any(), any(String.class), any());
    }

    @ExtendWith(SpringExtension.class)
    @Test
    void validateNotificationPaymentRetryKO() {
        //GIVEN
        NotificationInt notification = TestUtils.getNotification();
        Mockito.when(notificationService.getNotificationByIun(Mockito.anyString()))
                .thenReturn(notification);

        //Simulazione validazione con errore
        PnValidationPaymentException ex = new PnValidationPaymentException("error");
        doThrow(ex).when(paymentValidator).validatePayments(Mockito.any(NotificationInt.class), Mockito.any(Instant.class));

        NotificationValidationActionDetails details = NotificationValidationActionDetails.builder()
                .retryAttempt(1)
                .startWorkflowTime(Instant.now())
                .build();

        PnAuditLogEvent auditLogEvent = Mockito.mock(PnAuditLogEvent.class);
        Mockito.when(auditLogService.buildAuditLogEvent(Mockito.eq(notification.getIun()), Mockito.eq(PnAuditLogEventType.AUD_NT_VALID), Mockito.anyString(), any()))
                .thenReturn(auditLogEvent);
        Mockito.when(auditLogEvent.generateWarning(Mockito.anyString(), any(String.class), any())).thenReturn(auditLogEvent);

        //WHEN
        handler.validate(IUN, details);
        //THEN
        Mockito.verify(addressValidator, Mockito.never()).requestValidateAndNormalizeAddresses(notification);
        Mockito.verify(schedulerService).scheduleEvent(Mockito.eq(notification.getIun()), Mockito.any(Instant.class),
                Mockito.eq(ActionType.NOTIFICATION_REFUSED), Mockito.any(NotificationRefusedActionDetails.class), eq(null));
        Mockito.verify(auditLogEvent).generateWarning(any(), any(String.class), any());
        Mockito.verify(notificationValidationScheduler, Mockito.never()).scheduleNotificationValidation(Mockito.eq(notification), Mockito.anyInt(), any(), Mockito.any(Instant.class));
    }

    @ExtendWith(SpringExtension.class)
    @Test
    void handleValidateF24Response_verifyVasLookUpAddressAndNormalizeAddress_catchBlock() {
        UsedServicesInt usedServices = UsedServicesInt.builder()
                .physicalAddressLookUp(true)
                .build();
        NotificationInt notification = TestUtils.getNotificationV2WithF24(usedServices);
        List<ProblemError> errors = List.of(ProblemError.builder()
                .code("code")
                .detail("detail")
                .element("0")
                .build());

        Mockito.when(notificationService.getNotificationByIun(Mockito.anyString()))
                .thenReturn(notification);

        PnAuditLogEvent auditLogEvent = Mockito.mock(PnAuditLogEvent.class);
        Mockito.when(auditLogService.buildAuditLogEvent(Mockito.eq(notification.getIun()), Mockito.eq(PnAuditLogEventType.AUD_NT_VALID), Mockito.anyString(), any()))
                .thenReturn(auditLogEvent);
        Mockito.when(auditLogEvent.generateSuccess()).thenReturn(auditLogEvent);

        when(timelineUtils.buildValidateF24RequestTimelineElement(any()))
                .thenReturn(TimelineElementInternal.builder().build());
        when(addressValidator.requestValidateAndNormalizeAddresses(notification)).thenReturn(Mono.empty());

        PnLookupAddressValidationFailedException exception = new PnLookupAddressValidationFailedException(errors);
        Mockito.doThrow(exception).when(lookupAddressHandler).performValidation(notification);

        PnF24MetadataValidationEndEventPayload pnF24MetadataValidationEndEventPayload = PnF24MetadataValidationEndEventPayload.builder()
                .setId(notification.getIun())
                .status("ok")
                .errors(Collections.emptyList())
                .build();

        Assertions.assertDoesNotThrow(() -> handler.handleValidateF24Response(pnF24MetadataValidationEndEventPayload));
    }

    @ExtendWith(SpringExtension.class)
    @Test
    void handleValidateAndNormalizeAddressResponse_withNotificationCostServiceEnabled() {
        //GIVEN
        String iun = "testIun";
        NotificationInt notification = TestUtils.getNotification();

        Mockito.when(notificationService.getNotificationByIun(iun))
                .thenReturn(notification);

        PnAuditLogEvent auditLogEvent = Mockito.mock(PnAuditLogEvent.class);
        Mockito.when(auditLogService.buildAuditLogEvent(
                        Mockito.eq(notification.getIun()),
                        Mockito.eq(PnAuditLogEventType.AUD_NT_VALID),
                        Mockito.anyString(),
                        any()))
                .thenReturn(auditLogEvent);
        Mockito.when(auditLogEvent.generateSuccess()).thenReturn(auditLogEvent);

        List<NormalizeResultInt> listNormResult = new ArrayList<>();
        NormalizeResultInt result1 = NormalizeResultInt.builder()
                .normalizedAddress(PhysicalAddressInt.builder()
                        .addressDetails("Via Roma 1")
                        .province("MI")
                        .municipality("Milano")
                        .zip("20100")
                        .build())
                .id("0")
                .build();
        listNormResult.add(result1);

        NormalizeItemsResultInt normalizeItemsResult = NormalizeItemsResultInt.builder()
                .correlationId("testCorrId")
                .resultItems(listNormResult)
                .build();

        Mockito.doNothing().when(addressValidator).handleAddressValidation(iun, normalizeItemsResult);
        Mockito.doNothing().when(normalizeAddressHandler).handleNormalizedAddressResponse(notification, normalizeItemsResult);
        Mockito.when(notificationCostServiceFeatureFlagUtils.checkNotificationCostServiceStartDate(notification))
                .thenReturn(true);
        Mockito.doNothing().when(notificationCostService).initializeAndValidateNotificationCost(notification);

        //WHEN
        Assertions.assertDoesNotThrow(() ->
                handler.handleValidateAndNormalizeAddressResponse(iun, normalizeItemsResult));

        //THEN
        Mockito.verify(notificationService).getNotificationByIun(iun);
        Mockito.verify(addressValidator).handleAddressValidation(iun, normalizeItemsResult);
        Mockito.verify(normalizeAddressHandler).handleNormalizedAddressResponse(notification, normalizeItemsResult);
        Mockito.verify(notificationCostServiceFeatureFlagUtils).checkNotificationCostServiceStartDate(notification);
        Mockito.verify(notificationCostService).initializeAndValidateNotificationCost(notification);
        Mockito.verify(schedulerService, Mockito.never()).scheduleEvent(
                any(),
                any(),
                Mockito.eq(ActionType.SCHEDULE_RECEIVED_LEGALFACT_GENERATION),
                Mockito.eq(CommunicationType.LEGAL)
        );
        Mockito.verify(auditLogEvent, times(1)).generateSuccess(); // Solo alla fine del metodo
    }

    @ExtendWith(SpringExtension.class)
    @Test
    void handleValidateAndNormalizeAddressResponse_withNotificationCostServiceDisabled() {
        //GIVEN
        String iun = "testIun";
        NotificationInt notification = TestUtils.getNotification();

        Mockito.when(notificationService.getNotificationByIun(iun))
                .thenReturn(notification);

        PnAuditLogEvent auditLogEvent = Mockito.mock(PnAuditLogEvent.class);
        Mockito.when(auditLogService.buildAuditLogEvent(
                        Mockito.eq(notification.getIun()),
                        Mockito.eq(PnAuditLogEventType.AUD_NT_VALID),
                        Mockito.anyString(),
                        any()))
                .thenReturn(auditLogEvent);
        Mockito.when(auditLogEvent.generateSuccess()).thenReturn(auditLogEvent);

        List<NormalizeResultInt> listNormResult = new ArrayList<>();
        NormalizeResultInt result1 = NormalizeResultInt.builder()
                .normalizedAddress(PhysicalAddressInt.builder()
                        .addressDetails("Via Roma 1")
                        .province("MI")
                        .municipality("Milano")
                        .zip("20100")
                        .build())
                .id("0")
                .build();
        listNormResult.add(result1);

        NormalizeItemsResultInt normalizeItemsResult = NormalizeItemsResultInt.builder()
                .correlationId("testCorrId")
                .resultItems(listNormResult)
                .build();

        Mockito.doNothing().when(addressValidator).handleAddressValidation(iun, normalizeItemsResult);
        Mockito.doNothing().when(normalizeAddressHandler).handleNormalizedAddressResponse(notification, normalizeItemsResult);
        Mockito.when(notificationCostServiceFeatureFlagUtils.checkNotificationCostServiceStartDate(notification))
                .thenReturn(false);

        //WHEN
        Assertions.assertDoesNotThrow(() ->
                handler.handleValidateAndNormalizeAddressResponse(iun, normalizeItemsResult));

        //THEN
        Mockito.verify(notificationService).getNotificationByIun(iun);
        Mockito.verify(addressValidator).handleAddressValidation(iun, normalizeItemsResult);
        Mockito.verify(normalizeAddressHandler).handleNormalizedAddressResponse(notification, normalizeItemsResult);
        Mockito.verify(notificationCostServiceFeatureFlagUtils).checkNotificationCostServiceStartDate(notification);
        Mockito.verify(notificationCostService, Mockito.never()).initializeAndValidateNotificationCost(notification);
        Mockito.verify(schedulerService).scheduleEvent(
                Mockito.eq(iun),
                Mockito.any(Instant.class),
                Mockito.eq(ActionType.SCHEDULE_RECEIVED_LEGALFACT_GENERATION),
                Mockito.eq(CommunicationType.LEGAL)
        );
        Mockito.verify(auditLogEvent, times(2)).generateSuccess();
    }

    @ExtendWith(SpringExtension.class)
    @Test
    void handleValidateNotificationCostOK() {
        //GIVEN
        String iun = "testIun";
        NotificationInt notification = TestUtils.getNotification();

        Mockito.when(notificationService.getNotificationByIun(iun))
                .thenReturn(notification);

        PnAuditLogEvent auditLogEvent = Mockito.mock(PnAuditLogEvent.class);
        Mockito.when(auditLogService.buildAuditLogEvent(
                        Mockito.eq(notification.getIun()),
                        Mockito.eq(PnAuditLogEventType.AUD_NT_VALID),
                        Mockito.anyString(),
                        any()))
                .thenReturn(auditLogEvent);
        Mockito.when(auditLogEvent.generateSuccess()).thenReturn(auditLogEvent);

        TimelineElementInternal timelineElement = TimelineElementInternal.builder().build();
        Mockito.when(timelineUtils.buildNotificationCostValidationResponse(notification))
                .thenReturn(timelineElement);

        PnNotificationCostValidationEventPayload event = PnNotificationCostValidationEventPayload.builder()
                .iun(iun)
                .status(ValidationStatus.OK)
                .build();

        //WHEN
        Assertions.assertDoesNotThrow(() ->
                handler.handleValidateNotificationCost(iun, event));

        //THEN
        Mockito.verify(notificationService).getNotificationByIun(iun);
        Mockito.verify(timelineService).addTimelineElement(timelineElement, notification);
        Mockito.verify(schedulerService).scheduleEvent(
                Mockito.eq(iun),
                Mockito.any(Instant.class),
                Mockito.eq(ActionType.SCHEDULE_RECEIVED_LEGALFACT_GENERATION),
                Mockito.eq(CommunicationType.LEGAL)
        );
        Mockito.verify(auditLogEvent).generateSuccess();
        Mockito.verify(auditLogEvent).log();
    }

    @ExtendWith(SpringExtension.class)
    @Test
    void handleValidateNotificationCostKO() {
        //GIVEN
        String iun = "testIun";
        NotificationInt notification = TestUtils.getNotification();

        Mockito.when(notificationService.getNotificationByIun(iun))
                .thenReturn(notification);

        PnAuditLogEvent auditLogEvent = Mockito.mock(PnAuditLogEvent.class);
        Mockito.when(auditLogService.buildAuditLogEvent(
                        Mockito.eq(notification.getIun()),
                        Mockito.eq(PnAuditLogEventType.AUD_NT_VALID),
                        Mockito.anyString(),
                        any()))
                .thenReturn(auditLogEvent);
        Mockito.when(auditLogEvent.generateWarning(Mockito.anyString(), any())).thenReturn(auditLogEvent);

        PnNotificationCostValidationEventPayload event = PnNotificationCostValidationEventPayload.builder()
                .iun(iun)
                .status(ValidationStatus.KO)
                .build();

        //WHEN
        PnInternalException exception = Assertions.assertThrows(PnInternalException.class, () ->
                handler.handleValidateNotificationCost(iun, event));

        //THEN
        Assertions.assertEquals("Internal Server Error", exception.getMessage());
        Assertions.assertEquals(PnDeliveryPushValidatorExceptionCodes.ERROR_CODE_DELIVERYPUSH_NOTIFICATION_COST_ERROR,
                exception.getProblem().getErrors().getFirst().getCode());
        Mockito.verify(notificationService).getNotificationByIun(iun);
        Mockito.verify(auditLogEvent).generateWarning(Mockito.anyString(), any());
        Mockito.verify(auditLogEvent).log();
        Mockito.verify(timelineService, Mockito.never()).addTimelineElement(any(), any());
        Mockito.verify(schedulerService, Mockito.never()).scheduleEvent(
                any(), any(), any(), any());
    }

}