package it.pagopa.pn.deliverypushvalidator.action.startworkflow.notificationvalidation;

import it.pagopa.pn.commons.exceptions.PnInternalException;
import it.pagopa.pn.deliverypushvalidator.dto.campaign.Campaign;
import it.pagopa.pn.deliverypushvalidator.dto.campaign.Channel;
import it.pagopa.pn.deliverypushvalidator.dto.campaign.WorkflowEntity;
import it.pagopa.pn.deliverypushvalidator.dto.ext.datavault.RecipientTypeInt;
import it.pagopa.pn.deliverypushvalidator.dto.ext.delivery.notification.NotificationInt;
import it.pagopa.pn.deliverypushvalidator.dto.ext.delivery.notification.NotificationRecipientInt;
import it.pagopa.pn.deliverypushvalidator.exception.PnValidationDigitalAddressMissingException;
import org.springframework.stereotype.Component;

import java.util.List;

import static it.pagopa.pn.deliverypushvalidator.exception.PnDeliveryPushValidatorExceptionCodes.ERROR_CODE_DELIVERYPUSH_NO_RECIPIENT_IN_NOTIFICATION;

@Component
public class DigitalAddressValidator {

    public void validateDigitalAddress(NotificationInt notificationInt, Campaign campaign) {
        boolean hasPecWorkflow = campaign.getWorkflow().stream()
                .map(WorkflowEntity::getChannel)
                .anyMatch(channel -> channel == Channel.PEC);

        if (!hasPecWorkflow) {
            return;
        }

        List<NotificationRecipientInt> recipients = notificationInt.getRecipients();
        if (recipients == null || recipients.isEmpty()) {
            throw new PnInternalException("Notification recipients list is null", ERROR_CODE_DELIVERYPUSH_NO_RECIPIENT_IN_NOTIFICATION);
        }

        List<Integer> recipientIndexesWithoutDigitalAddress =
                java.util.stream.IntStream.range(0, recipients.size())
                        .filter(i -> {
                            NotificationRecipientInt r = recipients.get(i);
                            return RecipientTypeInt.PG == r.getRecipientType() &&
                                    (r.getDigitalDomicile() == null ||
                                            r.getDigitalDomicile().getAddress() == null ||
                                            r.getDigitalDomicile().getAddress().isBlank());
                        })
                        .boxed()
                        .toList();

        if (!recipientIndexesWithoutDigitalAddress.isEmpty()) {
            throw new PnValidationDigitalAddressMissingException(recipientIndexesWithoutDigitalAddress);
        }

    }
}