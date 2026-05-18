package it.pagopa.pn.deliverypushvalidator.action.refused;

import it.pagopa.pn.deliverypushvalidator.dto.timeline.CommunicationType;
import it.pagopa.pn.deliverypushvalidator.dto.timeline.NotificationRefusedErrorInt;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * Unit tests for NotificationRefusedActionHandler strategy dispatch logic.
 * Ensures that resolveStrategy correctly selects INFORMAL vs LEGAL (including null/default case),
 * and that notificationRefusedHandler delegates to the correct strategy implementation.
 */
@ExtendWith(MockitoExtension.class)
class NotificationRefusedActionHandlerTest {

    @Mock
    private LegalNotificationRefusedStrategy legalNotificationRefusedStrategy;

    @Mock
    private InformalNotificationRefusedStrategy informalNotificationRefusedStrategy;

    @InjectMocks
    private NotificationRefusedActionHandler handler;

    // ===== Tests for resolveStrategy() method via reflection or usage =====

    @Test
    void resolveStrategyReturnsLegalWhenCommunicationTypeIsLegal() {
        // Given
        String iun = "IUN_LEGAL";
        List<NotificationRefusedErrorInt> errors = List.of(
                NotificationRefusedErrorInt.builder().errorCode("E01").detail("detail").build()
        );
        Instant schedulingTime = Instant.now();

        // When
        handler.notificationRefusedHandler(iun, errors, schedulingTime, CommunicationType.LEGAL);

        // Then - Verify that legalNotificationRefusedStrategy is called
        verify(legalNotificationRefusedStrategy).handleNotificationRefused(iun, errors, schedulingTime);
        verify(informalNotificationRefusedStrategy, never()).handleNotificationRefused(any(), any(), any());
    }

    @Test
    void resolveStrategyReturnsInformalWhenCommunicationTypeIsInformal() {
        // Given
        String iun = "IUN_INFORMAL";
        List<NotificationRefusedErrorInt> errors = List.of(
                NotificationRefusedErrorInt.builder().errorCode("E02").detail("informal detail").build()
        );
        Instant schedulingTime = Instant.now();

        // When
        handler.notificationRefusedHandler(iun, errors, schedulingTime, CommunicationType.INFORMAL);

        // Then - Verify that informalNotificationRefusedStrategy is called
        verify(informalNotificationRefusedStrategy).handleNotificationRefused(iun, errors, schedulingTime);
        verify(legalNotificationRefusedStrategy, never()).handleNotificationRefused(any(), any(), any());
    }

    @Test
    void resolveStrategyReturnsLegalWhenCommunicationTypeIsNull() {
        // Given - null communicationType should default to LEGAL
        String iun = "IUN_NULL";
        List<NotificationRefusedErrorInt> errors = List.of(
                NotificationRefusedErrorInt.builder().errorCode("E03").detail("null detail").build()
        );
        Instant schedulingTime = Instant.now();

        // When
        handler.notificationRefusedHandler(iun, errors, schedulingTime, null);

        // Then - Verify that legalNotificationRefusedStrategy (default) is called
        verify(legalNotificationRefusedStrategy).handleNotificationRefused(iun, errors, schedulingTime);
        verify(informalNotificationRefusedStrategy, never()).handleNotificationRefused(any(), any(), any());
    }

    @Test
    void notificationRefusedHandlerDelegatesWithCorrectArgumentsForInformal() {
        // Given
        String iun = "IUN_INFORMAL_ARGS";
        List<NotificationRefusedErrorInt> errors = List.of(
                NotificationRefusedErrorInt.builder().errorCode("E04").detail("error details").build(),
                NotificationRefusedErrorInt.builder().errorCode("E05").detail("more errors").build()
        );
        Instant schedulingTime = Instant.parse("2026-05-18T10:30:00Z");
        CommunicationType communicationType = CommunicationType.INFORMAL;

        // When
        handler.notificationRefusedHandler(iun, errors, schedulingTime, communicationType);

        // Then - Verify exact arguments passed to strategy
        verify(informalNotificationRefusedStrategy).handleNotificationRefused(
                eq(iun),
                eq(errors),
                eq(schedulingTime)
        );
    }

    @Test
    void notificationRefusedHandlerDelegatesWithCorrectArgumentsForLegal() {
        // Given
        String iun = "IUN_LEGAL_ARGS";
        List<NotificationRefusedErrorInt> errors = List.of(
                NotificationRefusedErrorInt.builder().errorCode("E06").detail("legal error").build()
        );
        Instant schedulingTime = Instant.parse("2026-05-18T15:45:00Z");
        CommunicationType communicationType = CommunicationType.LEGAL;

        // When
        handler.notificationRefusedHandler(iun, errors, schedulingTime, communicationType);

        // Then - Verify exact arguments passed to strategy
        verify(legalNotificationRefusedStrategy).handleNotificationRefused(
                eq(iun),
                eq(errors),
                eq(schedulingTime)
        );
    }

    @Test
    void notificationRefusedHandlerWithEmptyErrorsList() {
        // Given - Edge case: empty errors list
        String iun = "IUN_EMPTY_ERRORS";
        List<NotificationRefusedErrorInt> errors = List.of();
        Instant schedulingTime = Instant.now();

        // When
        handler.notificationRefusedHandler(iun, errors, schedulingTime, CommunicationType.LEGAL);

        // Then
        verify(legalNotificationRefusedStrategy).handleNotificationRefused(
                eq(iun),
                eq(errors),
                eq(schedulingTime)
        );
    }

    @Test
    void strategyDispatchIsNotSwappedInformalVsLegal() {
        // Given - Regression test: ensure strategies are not accidentally swapped
        String iun1 = "IUN_INFORMAL_CHECK";
        String iun2 = "IUN_LEGAL_CHECK";
        List<NotificationRefusedErrorInt> errors = List.of(
                NotificationRefusedErrorInt.builder().errorCode("ERR").detail("test").build()
        );
        Instant time = Instant.now();

        // When - Call with INFORMAL
        handler.notificationRefusedHandler(iun1, errors, time, CommunicationType.INFORMAL);

        // Then - INFORMAL strategy must be called for first
        verify(informalNotificationRefusedStrategy).handleNotificationRefused(eq(iun1), any(), any());

        // When - Call with LEGAL
        handler.notificationRefusedHandler(iun2, errors, time, CommunicationType.LEGAL);

        // Then - LEGAL strategy must be called for second
        verify(legalNotificationRefusedStrategy).handleNotificationRefused(eq(iun2), any(), any());

        // Ensure no cross-contamination
        verify(informalNotificationRefusedStrategy, never()).handleNotificationRefused(eq(iun2), any(), any());
        verify(legalNotificationRefusedStrategy, never()).handleNotificationRefused(eq(iun1), any(), any());
    }
}


