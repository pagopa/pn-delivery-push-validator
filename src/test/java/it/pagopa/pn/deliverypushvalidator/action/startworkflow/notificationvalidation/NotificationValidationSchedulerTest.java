package it.pagopa.pn.deliverypushvalidator.action.startworkflow.notificationvalidation;

import it.pagopa.pn.deliverypushvalidator.action.details.NotificationRefusedActionDetails;
import it.pagopa.pn.deliverypushvalidator.action.it.utils.TestUtils;
import it.pagopa.pn.deliverypushvalidator.action.utils.InstantNowSupplier;
import it.pagopa.pn.deliverypushvalidator.config.PnDeliveryPushValidatorConfigs;
import it.pagopa.pn.deliverypushvalidator.dto.ext.delivery.notification.NotificationInt;
import it.pagopa.pn.deliverypushvalidator.dto.timeline.CommunicationType;
import it.pagopa.pn.deliverypushvalidator.exception.PnValidationFileNotFoundException;
import it.pagopa.pn.deliverypushvalidator.middleware.queue.producer.abstractions.actionspool.ActionDetails;
import it.pagopa.pn.deliverypushvalidator.middleware.queue.producer.abstractions.actionspool.ActionType;
import it.pagopa.pn.deliverypushvalidator.service.SchedulerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.Duration;
import java.time.Instant;

import static it.pagopa.pn.deliverypushvalidator.action.startworkflow.notificationvalidation.NotificationValidationScheduler.DEFAULT_INTERVAL;
import static org.junit.jupiter.api.Assertions.assertThrows;


class NotificationValidationSchedulerTest {
    @Mock
    private SchedulerService schedulerService;
    @Mock
    private PnDeliveryPushValidatorConfigs configs;
    @Mock
    private InstantNowSupplier instantNowSupplier;

    private NotificationValidationScheduler notificationValidationScheduler;

    @BeforeEach
    public void setup() {
        notificationValidationScheduler = new NotificationValidationScheduler(schedulerService, configs, instantNowSupplier);
    }

    @ExtendWith(SpringExtension.class)
    @Test
    void scheduleNotificationValidation() {
        //GIVEN
        String iun = "test";
        //WHEN
        notificationValidationScheduler.scheduleNotificationValidation(iun, null);
        //THEN
        Mockito.verify(schedulerService).scheduleEvent(Mockito.eq(iun), Mockito.any(Instant.class), Mockito.eq(ActionType.NOTIFICATION_VALIDATION), Mockito.any(ActionDetails.class), Mockito.isNull());
    }

    @ExtendWith(SpringExtension.class)
    @Test
    void testScheduleNotificationValidation() {
        //GIVEN
        NotificationInt notification = TestUtils.getNotification();

        Duration [] intervalsDuration = { Duration.ofSeconds(2), Duration.ofSeconds(3) };
        Mockito.when(configs.getValidationRetryIntervals()).thenReturn(intervalsDuration);

        Instant now = Instant.now();
        Mockito.when(instantNowSupplier.get()).thenReturn(now);
        
        //WHEN
        int retryAttempt = 0;
        notificationValidationScheduler.scheduleNotificationValidation(notification, retryAttempt, null, Instant.now());
        
        //THEN
        Instant schedulingDate = now.plus(intervalsDuration[retryAttempt]);
        
        Mockito.verify(schedulerService).scheduleEvent(Mockito.eq(notification.getIun()), Mockito.eq(schedulingDate), Mockito.eq(ActionType.NOTIFICATION_VALIDATION), Mockito.any(ActionDetails.class), Mockito.isNull());
    }

    @ExtendWith(SpringExtension.class)
    @Test
    void testScheduleNotificationValidationInfinite() {
        //GIVEN
        NotificationInt notification = TestUtils.getNotification();
        Duration [] intervalsDuration = { Duration.ofSeconds(2), Duration.ofSeconds(3), Duration.ofSeconds(-1) };
        Mockito.when(configs.getValidationRetryIntervals()).thenReturn(intervalsDuration);

        Instant now = Instant.now();
        Mockito.when(instantNowSupplier.get()).thenReturn(now);

        //WHEN
        int retryAttempt = 2;
        notificationValidationScheduler.scheduleNotificationValidation(notification, retryAttempt, null, Instant.now());

        //THEN
        Instant schedulingDate = now.plus(intervalsDuration[retryAttempt - 1]);
        Mockito.verify(schedulerService).scheduleEvent(Mockito.eq(notification.getIun()), Mockito.eq(schedulingDate), Mockito.eq(ActionType.NOTIFICATION_VALIDATION), Mockito.any(ActionDetails.class), Mockito.isNull());
    }

    @ExtendWith(SpringExtension.class)
    @Test
    void testScheduleNotificationValidationInfiniteOneInterval() {
        //GIVEN
        NotificationInt notification = TestUtils.getNotification();
        Duration [] intervalsDuration = { Duration.ofSeconds(-1) };
        Mockito.when(configs.getValidationRetryIntervals()).thenReturn(intervalsDuration);

        Instant now = Instant.now();
        Mockito.when(instantNowSupplier.get()).thenReturn(now);

        //WHEN
        int retryAttempt = 2;
        notificationValidationScheduler.scheduleNotificationValidation(notification, retryAttempt,null, Instant.now());

        //THEN
        Instant schedulingDate = now.plus(DEFAULT_INTERVAL);
        Mockito.verify(schedulerService).scheduleEvent(Mockito.eq(notification.getIun()), Mockito.eq(schedulingDate), Mockito.eq(ActionType.NOTIFICATION_VALIDATION), Mockito.any(ActionDetails.class), Mockito.isNull());
    }

    @ExtendWith(SpringExtension.class)
    @Test
    void testScheduleNotificationValidationRefused() {
        //GIVEN
        NotificationInt notification = TestUtils.getNotification();

        Duration [] intervalsDuration = { Duration.ofSeconds(2), Duration.ofSeconds(3) };
        Mockito.when(configs.getValidationRetryIntervals()).thenReturn(intervalsDuration);

        Instant now = Instant.now();
        Mockito.when(instantNowSupplier.get()).thenReturn(now);
        
        //WHEN
        int retryAttempt = 2;
        notificationValidationScheduler.scheduleNotificationValidation(notification, retryAttempt,null, Instant.now());

        //THEN
        Mockito.verify(schedulerService).scheduleEvent(Mockito.eq(notification.getIun()), Mockito.any(Instant.class),
                Mockito.eq(ActionType.NOTIFICATION_REFUSED), Mockito.any(NotificationRefusedActionDetails.class), Mockito.isNull());
    }

    @ExtendWith(SpringExtension.class)
    @Test
    void testScheduleNotificationValidationNotFoundRefused() {
        //GIVEN
        NotificationInt notification = TestUtils.getNotification();

        Duration [] intervalsDuration = { Duration.ofSeconds(2), Duration.ofSeconds(3) };
        Mockito.when(configs.getValidationRetryIntervals()).thenReturn(intervalsDuration);

        Instant now = Instant.now();
        Mockito.when(instantNowSupplier.get()).thenReturn(now);

        //WHEN
        int retryAttempt = 2;
        PnValidationFileNotFoundException ex = new PnValidationFileNotFoundException( "file non trovato", new Throwable() );
        notificationValidationScheduler.scheduleNotificationValidation(notification, retryAttempt, ex, Instant.now());

        //THEN
        Mockito.verify(schedulerService).scheduleEvent(Mockito.eq(notification.getIun()), Mockito.any(Instant.class),
                Mockito.eq(ActionType.NOTIFICATION_REFUSED), Mockito.any(NotificationRefusedActionDetails.class), Mockito.isNull());
    }

    @ExtendWith(SpringExtension.class)
    @Test
    void scheduleInformalNotificationValidation() {
        //GIVEN
        String iun = "test-informal";

        //WHEN
        ArgumentCaptor<CommunicationType> communicationTypeCaptor = ArgumentCaptor.forClass(CommunicationType.class);
        notificationValidationScheduler.scheduleNotificationValidation(iun, CommunicationType.INFORMAL);

        //THEN
        Mockito.verify(schedulerService).scheduleEvent(
                Mockito.eq(iun),
                Mockito.any(Instant.class),
                Mockito.eq(ActionType.NOTIFICATION_VALIDATION),
                Mockito.any(ActionDetails.class),
                communicationTypeCaptor.capture()
        );
    }

    @ExtendWith(SpringExtension.class)
    @Test
    void scheduleInformalNotificationValidation_schedulerThrowsException() {
        //GIVEN
        String iun = "test-informal";
        Mockito.doThrow(new RuntimeException("scheduler error"))
                .when(schedulerService).scheduleEvent(
                        Mockito.eq(iun),
                        Mockito.any(Instant.class),
                        Mockito.eq(ActionType.NOTIFICATION_VALIDATION),
                        Mockito.any(ActionDetails.class),
                        Mockito.any(CommunicationType.class)
                );

        //WHEN + THEN
        assertThrows(RuntimeException.class, () ->
                notificationValidationScheduler.scheduleNotificationValidation(iun, CommunicationType.INFORMAL)
        );
    }
}