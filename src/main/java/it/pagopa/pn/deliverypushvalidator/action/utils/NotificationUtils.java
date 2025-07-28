package it.pagopa.pn.deliverypushvalidator.action.utils;

import it.pagopa.pn.commons.exceptions.PnInternalException;
import it.pagopa.pn.commons.utils.LogUtils;

import it.pagopa.pn.deliverypushvalidator.dto.delivery.notification.NotificationInt;
import it.pagopa.pn.deliverypushvalidator.dto.delivery.notification.NotificationRecipientInt;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static it.pagopa.pn.deliverypushvalidator.exception.PnDeliveryPushValidatorExceptionCodes.ERROR_CODE_DELIVERYPUSH_NO_RECIPIENT_IN_NOTIFICATION;

@Component
@Slf4j
public class NotificationUtils {
    
    public static int getRecipientIndexFromTaxId(NotificationInt notification, String taxId){
        int index = 0;

        for (NotificationRecipientInt recipientNot : notification.getRecipients()) {
            if (recipientNot.getTaxId().equals(taxId)) {
                return index;
            }
            index++;
        }
        log.error("There isn't recipient in Notification - iun={} taxId={}", notification.getIun(), LogUtils.maskTaxId(taxId));
        throw new PnInternalException("There isn't recipient in Notification", ERROR_CODE_DELIVERYPUSH_NO_RECIPIENT_IN_NOTIFICATION);
    }
    
    public int getRecipientIndexFromInternalId(NotificationInt notification, String internalId){
        int index = 0;

        for(NotificationRecipientInt recipientNot : notification.getRecipients()){
            if(recipientNot.getInternalId().equals(internalId)){
                return index;
            }
            index ++;
        }

        throw new PnInternalException("There isn't internalId=" + internalId + " in Notification with iun=" + notification.getIun(), ERROR_CODE_DELIVERYPUSH_NO_RECIPIENT_IN_NOTIFICATION);
    }

    public NotificationRecipientInt getRecipientFromIndex(NotificationInt notification, int index){
        return notification.getRecipients().get(index);
    }

}
