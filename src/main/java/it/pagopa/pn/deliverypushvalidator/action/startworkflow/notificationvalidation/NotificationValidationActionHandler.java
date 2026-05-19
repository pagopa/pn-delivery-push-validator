package it.pagopa.pn.deliverypushvalidator.action.startworkflow.notificationvalidation;

import it.pagopa.pn.api.dto.events.PnF24MetadataValidationEndEventPayload;
import it.pagopa.pn.api.dto.events.notificationcost.validation.PnNotificationCostValidationEventPayload;
import it.pagopa.pn.deliverypushvalidator.action.details.NotificationValidationActionDetails;
import it.pagopa.pn.deliverypushvalidator.dto.ext.addressmanager.NormalizeItemsResultInt;
import it.pagopa.pn.deliverypushvalidator.dto.timeline.CommunicationType;
import lombok.AllArgsConstructor;
import lombok.CustomLog;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
@CustomLog
public class NotificationValidationActionHandler {
    private final InformalNotificationValidationStrategy informalStrategy;
    private final LegalNotificationValidationStrategy legalStrategy;

    public void validateNotification(String iun, NotificationValidationActionDetails details, CommunicationType communicationType) {
        log.debug("Start validateNotification - iun={}", iun);
        NotificationValidationStrategy strategy = resolveStrategy(communicationType, iun);
        strategy.validate(iun, details);
    }

    public void handleValidateF24Response(PnF24MetadataValidationEndEventPayload metadataValidationEndEvent, CommunicationType communicationType) {
        NotificationValidationStrategy strategy = resolveStrategy(communicationType, metadataValidationEndEvent.getSetId());
        strategy.handleValidateF24Response(metadataValidationEndEvent);
    }

    public void handleValidateAndNormalizeAddressResponse(String iun, NormalizeItemsResultInt normalizeItemsResult, CommunicationType communicationType) {
        NotificationValidationStrategy strategy = resolveStrategy(communicationType, iun);
        strategy.handleValidateAndNormalizeAddressResponse(iun, normalizeItemsResult);
    }

    public void handleValidateNotificationCost(String iun, PnNotificationCostValidationEventPayload event, CommunicationType communicationType) {
        NotificationValidationStrategy strategy = resolveStrategy(communicationType, iun);
        strategy.handleValidateNotificationCost(iun, event);
    }

    private NotificationValidationStrategy resolveStrategy(CommunicationType communicationType, String iun) {
        log.debug("Resolved validation strategy for type={} - iun={}", communicationType, iun);
        return switch (communicationType) {
            case INFORMAL -> informalStrategy;
            case LEGAL -> legalStrategy;
            case null -> legalStrategy;
        };
    }

}
