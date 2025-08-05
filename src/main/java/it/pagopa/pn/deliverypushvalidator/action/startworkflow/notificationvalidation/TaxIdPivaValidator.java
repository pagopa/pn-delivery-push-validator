package it.pagopa.pn.deliverypushvalidator.action.startworkflow.notificationvalidation;

import it.pagopa.pn.deliverypushvalidator.dto.ext.delivery.notification.NotificationInt;
import it.pagopa.pn.deliverypushvalidator.dto.nationalregistries.CheckTaxIdOKInt;
import it.pagopa.pn.deliverypushvalidator.exception.PnValidationTaxIdNotValidException;
import it.pagopa.pn.deliverypushvalidator.service.NationalRegistriesService;
import lombok.AllArgsConstructor;
import lombok.CustomLog;
import org.springframework.stereotype.Component;

import static it.pagopa.pn.deliverypushvalidator.action.utils.NotificationUtils.getRecipientIndexFromTaxId;

@Component
@AllArgsConstructor
@CustomLog
public class TaxIdPivaValidator {
    private static final String VALIDATE_TAXID_PROCESS = "Validate taxId";

    private final NationalRegistriesService nationalRegistriesService;

    public void validateTaxIdPiva(NotificationInt notification){
        log.logChecking(VALIDATE_TAXID_PROCESS);

        notification.getRecipients().forEach( recipient -> {
            int recIndex = getRecipientIndexFromTaxId(notification, recipient.getTaxId());
            log.debug("Start taxIdValidation for specific recipient - iun={} id={}", notification.getIun(), recIndex);
            
            CheckTaxIdOKInt response = nationalRegistriesService.checkTaxId(recipient.getTaxId());
            if (Boolean.FALSE.equals(response.getIsValid()) ){
                log.debug("TaxId is not valid - iun={} id={}", notification.getIun(), recIndex);
                log.logCheckingOutcome(VALIDATE_TAXID_PROCESS, false, response.getErrorCode());

                throw new PnValidationTaxIdNotValidException(response.getErrorCode());
            }

            log.debug("TaxId is valid - iun={} id={}", notification.getIun(), recIndex);
        });

        log.logCheckingOutcome(VALIDATE_TAXID_PROCESS, true);
    }
}
