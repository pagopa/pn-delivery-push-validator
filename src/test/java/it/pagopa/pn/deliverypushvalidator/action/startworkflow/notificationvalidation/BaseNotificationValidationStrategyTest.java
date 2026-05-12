package it.pagopa.pn.deliverypushvalidator.action.startworkflow.notificationvalidation;

import it.pagopa.pn.common.rest.error.v1.dto.Problem;
import it.pagopa.pn.common.rest.error.v1.dto.ProblemError;
import it.pagopa.pn.commons.exceptions.PnValidationException;
import it.pagopa.pn.deliverypushvalidator.action.details.NotificationRefusedActionDetails;
import it.pagopa.pn.deliverypushvalidator.dto.ext.delivery.notification.NotificationInt;
import it.pagopa.pn.deliverypushvalidator.dto.timeline.NotificationRefusedErrorInt;
import it.pagopa.pn.deliverypushvalidator.exception.PnLookupAddressValidationFailedException;
import it.pagopa.pn.deliverypushvalidator.middleware.queue.producer.abstractions.actionspool.ActionType;
import it.pagopa.pn.deliverypushvalidator.service.SchedulerService;
import it.pagopa.pn.deliverypushvalidator.action.details.NotificationValidationActionDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BaseNotificationValidationStrategyTest {

    private NotificationValidationScheduler notificationValidationScheduler;
    private SchedulerService schedulerService;
    private BaseNotificationValidationStrategy strategy;

    @BeforeEach
    void setUp() {
        notificationValidationScheduler = mock(NotificationValidationScheduler.class);
        schedulerService = mock(SchedulerService.class);
        strategy = new BaseNotificationValidationStrategy(notificationValidationScheduler, schedulerService);
    }

    @Test
    void handleValidationError_withProblem_shouldExtractErrors() {
        NotificationInt notification = mock(NotificationInt.class);
        when(notification.getIun()).thenReturn("iun");
        when(notification.getCommunicationType()).thenReturn(null);
        ProblemError error = ProblemError.builder().code("ERR").detail("detail").build();
        Problem problem = Problem.builder().errors(List.of(error)).build();
        PnValidationException ex = mock(PnValidationException.class);
        when(ex.getProblem()).thenReturn(problem);

        assertDoesNotThrow(() -> strategy.handleValidationError(notification, ex));
        verify(schedulerService).scheduleEvent(eq("iun"), any(Instant.class), eq(ActionType.NOTIFICATION_REFUSED), any(NotificationRefusedActionDetails.class));
    }

    @Test
    void handleValidationError_withNullProblem_shouldNotFail() {
        NotificationInt notification = mock(NotificationInt.class);
        when(notification.getIun()).thenReturn("iun");
        when(notification.getCommunicationType()).thenReturn(null);
        PnValidationException ex = mock(PnValidationException.class);
        when(ex.getProblem()).thenReturn(null);

        assertDoesNotThrow(() -> strategy.handleValidationError(notification, ex));
        verify(schedulerService).scheduleEvent(eq("iun"), any(Instant.class), eq(ActionType.NOTIFICATION_REFUSED), any(NotificationRefusedActionDetails.class));
    }

    @Test
    void handleRuntimeException_shouldScheduleNotificationValidation() {
        NotificationInt notification = mock(NotificationInt.class);
        NotificationValidationActionDetails details = NotificationValidationActionDetails.builder().retryAttempt(2).build();
        RuntimeException ex = new RuntimeException();
        Instant now = Instant.now();

        strategy.handleRuntimeException("iun", details, notification, ex, now);
        verify(notificationValidationScheduler).scheduleNotificationValidation(notification, 2, ex, now);
    }

    @Test
    void handleLookupAddressValidationError_shouldScheduleNotificationRefused() {
        NotificationInt notification = mock(NotificationInt.class);
        when(notification.getIun()).thenReturn("iun");
        ProblemError error = ProblemError.builder().code("ERR").detail("detail").element("1").build();
        Problem problem = Problem.builder().errors(List.of(error)).build();
        PnLookupAddressValidationFailedException ex = mock(PnLookupAddressValidationFailedException.class);
        when(ex.getProblem()).thenReturn(problem);

        strategy.handleLookupAddressValidationError(notification, ex);

        ArgumentCaptor<NotificationRefusedActionDetails> captor = ArgumentCaptor.forClass(NotificationRefusedActionDetails.class);
        verify(schedulerService).scheduleEvent(eq("iun"), any(Instant.class), eq(ActionType.NOTIFICATION_REFUSED), captor.capture());
        NotificationRefusedActionDetails details = captor.getValue();
        assertEquals(1, details.getErrors().size());
        assertEquals("ERR", details.getErrors().getFirst().getErrorCode());
        assertEquals("detail", details.getErrors().getFirst().getDetail());
        assertEquals(1, details.getErrors().getFirst().getRecIndex());
    }

    @Test
    void scheduleNotificationRefused_shouldCallSchedulerService() {
        String iun = "iun";
        NotificationRefusedErrorInt error = NotificationRefusedErrorInt.builder().errorCode("ERR").detail("detail").build();

        strategy.scheduleNotificationRefused(iun, List.of(error), null);
        verify(schedulerService).scheduleEvent(eq(iun), any(Instant.class), eq(ActionType.NOTIFICATION_REFUSED), any(NotificationRefusedActionDetails.class));
    }
}