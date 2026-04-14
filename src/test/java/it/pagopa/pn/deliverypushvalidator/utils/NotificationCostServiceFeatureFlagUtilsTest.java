package it.pagopa.pn.deliverypushvalidator.utils;

import it.pagopa.pn.deliverypushvalidator.config.PnDeliveryPushValidatorConfigs;
import it.pagopa.pn.deliverypushvalidator.dto.ext.delivery.notification.NotificationInt;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationCostServiceFeatureFlagUtilsTest {

    @Mock
    private PnDeliveryPushValidatorConfigs configs;

    private NotificationCostServiceFeatureFlagUtils featureFlagUtils;

    @BeforeEach
    void setUp() {
        featureFlagUtils = new NotificationCostServiceFeatureFlagUtils(configs);
    }

    @Test
    void checkNotificationCostServiceStartDate_whenStartDateNotConfigured_shouldReturnFalse() {
        // Given
        NotificationInt notification = buildNotification(Instant.parse("2024-01-15T10:00:00Z"));
        when(configs.getNotificationCostServiceStartDate()).thenReturn(null);

        // When
        boolean result = featureFlagUtils.checkNotificationCostServiceStartDate(notification);

        // Then
        assertFalse(result, "Service should be disabled when start date is not configured");
    }

    @Test
    void checkNotificationCostServiceStartDate_whenNotificationSentAfterStartDate_shouldReturnTrue() {
        // Given
        Instant startDate = Instant.parse("2024-01-01T00:00:00Z");
        Instant sentAt = Instant.parse("2024-01-15T10:00:00Z");

        NotificationInt notification = buildNotification(sentAt);
        when(configs.getNotificationCostServiceStartDate()).thenReturn(startDate);

        // When
        boolean result = featureFlagUtils.checkNotificationCostServiceStartDate(notification);

        // Then
        assertTrue(result, "Service should be enabled when notification sent after start date");
    }

    @Test
    void checkNotificationCostServiceStartDate_whenNotificationSentOnStartDate_shouldReturnFalse() {
        // Given
        Instant startDate = Instant.parse("2024-01-01T00:00:00Z");
        Instant sentAt = Instant.parse("2024-01-01T00:00:00Z");

        NotificationInt notification = buildNotification(sentAt);
        when(configs.getNotificationCostServiceStartDate()).thenReturn(startDate);

        // When
        boolean result = featureFlagUtils.checkNotificationCostServiceStartDate(notification);

        // Then
        assertFalse(result, "Service should be disabled when notification sent exactly on start date (using isBefore logic)");
    }

    @Test
    void checkNotificationCostServiceStartDate_whenNotificationSentBeforeStartDate_shouldReturnFalse() {
        // Given
        Instant startDate = Instant.parse("2024-01-15T00:00:00Z");
        Instant sentAt = Instant.parse("2024-01-01T10:00:00Z");

        NotificationInt notification = buildNotification(sentAt);
        when(configs.getNotificationCostServiceStartDate()).thenReturn(startDate);

        // When
        boolean result = featureFlagUtils.checkNotificationCostServiceStartDate(notification);

        // Then
        assertFalse(result, "Service should be disabled when notification sent before start date");
    }

    @Test
    void checkNotificationCostServiceStartDate_whenNotificationSentAtIsNull_shouldThrowException() {
        // Given
        Instant startDate = Instant.parse("2024-01-01T00:00:00Z");
        NotificationInt notification = buildNotification(null);
        when(configs.getNotificationCostServiceStartDate()).thenReturn(startDate);

        // When & Then
        NullPointerException exception = assertThrows(NullPointerException.class,
                () -> featureFlagUtils.checkNotificationCostServiceStartDate(notification));

        assertEquals("Notification sentAt cannot be null", exception.getMessage());
    }

    @Test
    void checkNotificationCostServiceStartDate_boundaryCase_oneMillisecondAfterStart_shouldReturnTrue() {
        // Given
        Instant startDate = Instant.parse("2024-01-01T00:00:00Z");
        Instant sentAt = startDate.plusMillis(1);

        NotificationInt notification = buildNotification(sentAt);
        when(configs.getNotificationCostServiceStartDate()).thenReturn(startDate);

        // When
        boolean result = featureFlagUtils.checkNotificationCostServiceStartDate(notification);

        // Then
        assertTrue(result, "Service should be enabled when notification sent one millisecond after start");
    }

    @Test
    void checkNotificationCostServiceStartDate_boundaryCase_oneMillisecondBeforeStart_shouldReturnFalse() {
        // Given
        Instant startDate = Instant.parse("2024-01-01T00:00:00Z");
        Instant sentAt = startDate.minusMillis(1);

        NotificationInt notification = buildNotification(sentAt);
        when(configs.getNotificationCostServiceStartDate()).thenReturn(startDate);

        // When
        boolean result = featureFlagUtils.checkNotificationCostServiceStartDate(notification);

        // Then
        assertFalse(result, "Service should be disabled when notification sent one millisecond before start");
    }

    private NotificationInt buildNotification(Instant sentAt) {
        return NotificationInt.builder()
                .iun("TEST-IUN-123")
                .sentAt(sentAt)
                .build();
    }
}
