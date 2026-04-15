package it.pagopa.pn.deliverypushvalidator.action.startworkflow.notificationvalidation;

import it.pagopa.pn.api.dto.events.PnF24MetadataValidationEndEventPayload;
import it.pagopa.pn.deliverypushvalidator.action.details.NotificationValidationActionDetails;
import it.pagopa.pn.deliverypushvalidator.dto.ext.addressmanager.NormalizeItemsResultInt;
import it.pagopa.pn.deliverypushvalidator.dto.ext.delivery.notification.NotificationInt;
import it.pagopa.pn.deliverypushvalidator.dto.ext.delivery.notification.NotificationType;
import it.pagopa.pn.deliverypushvalidator.dto.timeline.NotificationRefusedErrorInt;
import it.pagopa.pn.deliverypushvalidator.service.NotificationService;
import lombok.AllArgsConstructor;
import lombok.CustomLog;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Main entry point for notification validation.
 * Delegates to the appropriate strategy based on notification type (LEGAL / INFORMAL).
 */
@Component
@AllArgsConstructor
@CustomLog
public class NotificationValidationActionHandler {

    private final NotificationService notificationService;
    private final NotificationRefusedSchedulerHelper refusedSchedulerHelper;
    private final LegalNotificationValidationStrategy legalStrategy;
    private final InformalNotificationValidationStrategy informalStrategy;

    public void validateNotification(String iun, NotificationValidationActionDetails details) {
        log.debug("Start validateNotification - iun={}", iun);
        NotificationInt notification = notificationService.getNotificationByIun(iun);
        NotificationValidationStrategy strategy = resolveStrategy(notification);
        strategy.validate(notification, details);
    }

    public void handleValidateF24Response(PnF24MetadataValidationEndEventPayload metadataValidationEndEvent) {
        NotificationInt notification = notificationService.getNotificationByIun(metadataValidationEndEvent.getSetId());
        NotificationValidationStrategy strategy = resolveStrategy(notification);
        strategy.handleValidateF24Response(metadataValidationEndEvent);
    }

    public void handleValidateAndNormalizeAddressResponse(String iun, NormalizeItemsResultInt normalizeItemsResult) {
        NotificationInt notification = notificationService.getNotificationByIun(iun);
        NotificationValidationStrategy strategy = resolveStrategy(notification);
        strategy.handleValidateAndNormalizeAddressResponse(iun, normalizeItemsResult);
    }

    /**
     * Shared utility for scheduling notification refusal, used by both strategies.
     */
    public void scheduleNotificationRefused(String iun, List<NotificationRefusedErrorInt> errors) {
        refusedSchedulerHelper.scheduleNotificationRefused(iun, errors);
    }

    private NotificationValidationStrategy resolveStrategy(NotificationInt notification) {
        NotificationType type = NotificationType.resolveOrDefault(notification.getType());
        log.debug("Resolved validation strategy for type={} - iun={}", type, notification.getIun());
        return switch (type) {
            case INFORMAL -> informalStrategy;
            case LEGAL -> legalStrategy;
        };
    }
}
