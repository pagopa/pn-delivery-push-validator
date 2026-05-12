package it.pagopa.pn.deliverypushvalidator.action.refused;

import it.pagopa.pn.deliverypushvalidator.dto.ext.delivery.notification.NotificationInt;
import it.pagopa.pn.deliverypushvalidator.dto.timeline.NotificationRefusedErrorInt;

import java.time.Instant;
import java.util.List;

/**
 * Strategy interface that abstracts the notification refusal flow.
 * Implementations can differentiate behavior, especially for refusal cost handling.
 */
public interface NotificationRefusedStrategy {
    NotificationInt getNotification(String iun);

    /**
     * Executes the main refusal flow for a notification.
     */
    void handleNotificationRefused(String iun, List<NotificationRefusedErrorInt> errors, Instant notBefore);
}

