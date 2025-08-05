package it.pagopa.pn.deliverypushvalidator.utils;

import it.pagopa.pn.commons.exceptions.PnInternalException;
import it.pagopa.pn.deliverypushvalidator.dto.ext.delivery.notification.NotificationInt;
import it.pagopa.pn.deliverypushvalidator.dto.timeline.NotificationRefusedErrorInt;
import it.pagopa.pn.deliverypushvalidator.exception.PnDeliveryPushValidatorExceptionCodes;
import it.pagopa.pn.deliverypushvalidator.service.NotificationProcessCostService;
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
    private final PnTechnicalRefusalCostMode pnTechnicalRefusalCostMode;
    private final NotificationProcessCostService notificationProcessCostService;
    private static final String errorMessage = "Invalid number of recipients not affected by technical errors";
    private static final String errorCode = "INVALID_NUMBER_OF_RECIPIENTS";

    public int calculateRefusalCost(NotificationInt notification, List<NotificationRefusedErrorInt> errors) {
        // Numero totale di destinatari della notifica
        int numOfRecipients = notification.getRecipients().size();
        int numOfRecipientsAffectedFromTechnicalError = countRecipientsWithTechnicalErrors(errors, notification.getIun());

        // Se non ci sono destinatari legati a errori tecnici, applica la logica attuale
        if (numOfRecipientsAffectedFromTechnicalError == 0) {
            return numOfRecipients * notificationProcessCostService.getSendFee();
        }

        // Calcolo del costo di rifiuto della notifica in base alla modalità di costo del rifiuto tecnico
        return switch (pnTechnicalRefusalCostMode.getMode()) {
            case UNIFORM -> pnTechnicalRefusalCostMode.getCost();
            case RECIPIENT_BASED -> {
                int numOfRecipientsNotAffectedFromTechnicalError = numOfRecipients - numOfRecipientsAffectedFromTechnicalError;
                if (numOfRecipientsNotAffectedFromTechnicalError < 0) {
                    throw new PnInternalException(errorMessage, errorCode);
                }
                yield (numOfRecipientsAffectedFromTechnicalError * pnTechnicalRefusalCostMode.getCost()) +
                        (numOfRecipientsNotAffectedFromTechnicalError * notificationProcessCostService.getSendFee());
            }
        };
    }

    private int countRecipientsWithTechnicalErrors(List<NotificationRefusedErrorInt> errors, String iun) {
        // Calcolo del numero di destinatari che sono legati a un errore che produce un rifiuto tecnico
        int count = 0;
        for (NotificationRefusedErrorInt error : errors) {
            PnDeliveryPushValidatorExceptionCodes.NotificationRefusedErrorCodeInt errorCode =
                    PnDeliveryPushValidatorExceptionCodes.NotificationRefusedErrorCodeInt.fromValue(error.getErrorCode());
            if (errorCode != null && errorCode.getIsTechnicalRefusal()) {
                log.info("Notification with iun {} and recipient with index {} is affected by a technical error: {}", iun, error.getRecIndex(), error.getErrorCode());
                count++;
            }
        }
        return count;
    }

}
