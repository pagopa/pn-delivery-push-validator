package it.pagopa.pn.deliverypushvalidator.service.mapper;

import it.pagopa.pn.commons.utils.FileUtils;
import it.pagopa.pn.deliverypushvalidator.dto.address.LegalDigitalAddressInt;
import it.pagopa.pn.deliverypushvalidator.dto.ext.delivery.notification.NotificationDocumentInt;
import it.pagopa.pn.deliverypushvalidator.dto.ext.delivery.notification.NotificationInt;
import it.pagopa.pn.deliverypushvalidator.dto.ext.delivery.notification.NotificationPaymentInfoInt;
import it.pagopa.pn.deliverypushvalidator.dto.ext.delivery.notification.NotificationRecipientInt;
import it.pagopa.pn.deliverypushvalidator.generated.openapi.msclient.templatesengine.model.*;
import it.pagopa.pn.deliverypushvalidator.legalfact.CustomInstantWriter;
import it.pagopa.pn.deliverypushvalidator.legalfact.PhysicalAddressWriter;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TemplatesEngineMapper {

    private TemplatesEngineMapper() {
    }

    public static NotificationReceivedLegalFact notificationReceivedLegalFact(NotificationInt notification,
                                                                              PhysicalAddressWriter physicalAddressWriter,
                                                                              CustomInstantWriter instantWriter) {
        String physicalAddressAndDenomination;
        List<NotificationRecipientInt> recipients = Optional.of(notification)
                .map(NotificationInt::getRecipients)
                .orElse(new ArrayList<>());

        List<NotificationReceivedRecipient> receivedRecipients = new ArrayList<>();
        for (var recipientInt : recipients) {
            String denomination = recipientInt.getDenomination();
            physicalAddressAndDenomination = physicalAddressWriter.nullSafePhysicalAddressToString(
                    recipientInt.getPhysicalAddress(), denomination, "<br/>");
            NotificationReceivedRecipient notificationReceivedNotification =
                    notificationReceivedNotification(physicalAddressAndDenomination, recipientInt);
            receivedRecipients.add(notificationReceivedNotification);
        }

        NotificationReceivedNotification notificationReceivedNotification = new NotificationReceivedNotification()
                .iun(notification.getIun())
                .recipients(receivedRecipients)
                .sender(sender(notification));

        return new NotificationReceivedLegalFact()
                .sendDate(instantWriter.instantToDate(notification.getSentAt()))
                .subject(notification.getSubject())
                .notification(notificationReceivedNotification)
                .digests(extractNotificationAttachmentDigests(notification));
    }

    private static NotificationReceivedRecipient notificationReceivedNotification(String physicalAddressAndDenomination,
                                                                                  NotificationRecipientInt recipientInt) {
        return recipientInt != null ?
                new NotificationReceivedRecipient()
                        .physicalAddressAndDenomination(physicalAddressAndDenomination)
                        .denomination(recipientInt.getDenomination())
                        .taxId(recipientInt.getTaxId())
                        .digitalDomicile(digitalDomicile(recipientInt.getDigitalDomicile())) : null;
    }

    private static NotificationReceivedDigitalDomicile digitalDomicile(LegalDigitalAddressInt domicile) {
        return domicile != null ? new NotificationReceivedDigitalDomicile().address(domicile.getAddress()) : null;
    }

    private static NotificationReceivedSender sender(NotificationInt notification) {
        var senderInt = Optional.of(notification).map(NotificationInt::getSender).orElse(null);
        return senderInt != null ?
                new NotificationReceivedSender()
                        .paDenomination(senderInt.getPaDenomination())
                        .paTaxId(senderInt.getPaTaxId())
                : null;
    }

    /**
     * Extracts the SHA-256 digests of the attachments related to a notification.
     *
     * @param notification the {@link NotificationInt} object containing the details of the notification,
     *                     including its attached documents and recipients with payment information.
     * @return a {@link List} of {@link String} representing the SHA-256 digests (in hexadecimal uppercase)
     * of all relevant attachments from the notification.
     */
    private static List<String> extractNotificationAttachmentDigests(NotificationInt notification) {
        List<String> digests = new ArrayList<>();
        // - Documents digests
        for (NotificationDocumentInt attachment : notification.getDocuments()) {
            digests.add(FileUtils.convertBase64toHexUppercase(attachment.getDigests().getSha256()));
        }
        // F24 digests
        for (NotificationRecipientInt recipient : notification.getRecipients()) {
            //add digests for v21
            addDigestsForMultiPayments(recipient.getPayments(), digests);
        }
        return digests;
    }

    /**
     * Adds the SHA-256 digests of the attachments related to the payments made by the recipient.
     *
     * @param payments a {@link List} of {@link NotificationPaymentInfoInt} objects representing the payments
     *                 made by the recipient, potentially containing attachments.
     * @param digests  a {@link List} of {@link String} where the extracted digests will be added.
     */
    private static void addDigestsForMultiPayments(List<NotificationPaymentInfoInt> payments, List<String> digests) {
        if (!CollectionUtils.isEmpty(payments)) {
            payments.forEach(payment -> {
                if (payment.getPagoPA() != null && payment.getPagoPA().getAttachment() != null) {
                    digests.add(FileUtils.convertBase64toHexUppercase(payment.getPagoPA().getAttachment().getDigests().getSha256()));
                }
                if (payment.getF24() != null && payment.getF24().getMetadataAttachment() != null) {
                    digests.add(FileUtils.convertBase64toHexUppercase(payment.getF24().getMetadataAttachment().getDigests().getSha256()));
                }
            });
        }
    }
}