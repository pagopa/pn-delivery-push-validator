package it.pagopa.pn.deliverypushvalidator.action.startworkflow.notificationvalidation;

import it.pagopa.pn.api.dto.events.PnF24MetadataValidationEndEventPayload;
import it.pagopa.pn.api.dto.events.notificationcost.validation.PnNotificationCostValidationEventPayload;
import it.pagopa.pn.deliverypushvalidator.action.details.NotificationValidationActionDetails;
import it.pagopa.pn.deliverypushvalidator.dto.ext.addressmanager.NormalizeItemsResultInt;
import it.pagopa.pn.deliverypushvalidator.dto.ext.delivery.notification.NotificationInt;

public interface NotificationValidationStrategy {

    /**
     * Retrieve the notification detail to validate.
     */
    NotificationInt getNotification(String iun);

    /**
     * Executes the main validation flow for a notification.
     */
    void validate(NotificationInt notification, NotificationValidationActionDetails details);

    /**
     * Handles the async F24 metadata validation response.
     */
    void handleValidateF24Response(PnF24MetadataValidationEndEventPayload payload);

    /**
     * Handles the async address validation/normalization response.
     */
    void handleValidateAndNormalizeAddressResponse(String iun, NormalizeItemsResultInt normalizeItemsResult);

    /**
     * Handles the async notification cost validation response.
     */
    void handleValidateNotificationCost(String iun, PnNotificationCostValidationEventPayload event);

    /**
     * Schedule the action which will perform the required operations to accept the notification.
     */
    void scheduleEndValidationAction(String iun);
}

