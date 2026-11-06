package it.pagopa.pn.deliverypushvalidator.utils;

import it.pagopa.pn.commons.exceptions.PnInternalException;
import it.pagopa.pn.deliverypushvalidator.action.refused.RefusalCostPolicy;
import it.pagopa.pn.deliverypushvalidator.action.refused.RefusalCostPolicyProvider;
import it.pagopa.pn.deliverypushvalidator.dto.ext.delivery.notification.NotificationInt;
import it.pagopa.pn.deliverypushvalidator.dto.timeline.NotificationRefusedErrorInt;
import it.pagopa.pn.deliverypushvalidator.exception.PnDeliveryPushValidatorExceptionCodes;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@AllArgsConstructor
@Getter
@Component
@Slf4j
public class RefusalCostCalculator {
    private final RefusalCostPolicyProvider policyProvider;
    private static final String ERROR_MESSAGE = "Invalid number of recipients not affected by technical errors";
    private static final String ERROR_CODE = "INVALID_NUMBER_OF_RECIPIENTS";

    public int calculateRefusalCost(NotificationInt notification, List<NotificationRefusedErrorInt> errors) {
        RefusalCostPolicy policy = policyProvider.get(notification.getCommunicationType());

        int numOfRecipients = notification.getRecipients().size();
        int technical = countRecipientsWithTechnicalErrors(errors, notification.getIun());

        if (technical == 0) {
            return numOfRecipients * policy.sendFee();
        }

        return switch (policy.mode()) {
            case UNIFORM -> policy.technicalRefusalCost();
            case RECIPIENT_BASED -> {
                int notAffected = numOfRecipients - technical;
                if (notAffected < 0) {
                    throw new PnInternalException(ERROR_MESSAGE, ERROR_CODE);
                }
                yield technical * policy.technicalRefusalCost() + notAffected * policy.sendFee();
            }
        };
    }

    private int countRecipientsWithTechnicalErrors(List<NotificationRefusedErrorInt> errors, String iun) {
        // Calcolo del numero di destinatari che sono legati a un errore che produce un rifiuto tecnico
        int count = 0;
        for (NotificationRefusedErrorInt error : errors) {
            PnDeliveryPushValidatorExceptionCodes.NotificationRefusedErrorCodeInt refusedErrorCode =
                    PnDeliveryPushValidatorExceptionCodes.NotificationRefusedErrorCodeInt.fromValue(error.getErrorCode());
            if (refusedErrorCode != null && refusedErrorCode.getIsTechnicalRefusal()) {
                log.info("Notification with iun {} and recipient with index {} is affected by a technical error: {}", iun, error.getRecIndex(), error.getErrorCode());
                count++;
            }
        }
        return count;
    }

}
