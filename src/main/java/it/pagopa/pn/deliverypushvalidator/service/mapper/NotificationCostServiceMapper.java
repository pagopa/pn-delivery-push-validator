package it.pagopa.pn.deliverypushvalidator.service.mapper;

import it.pagopa.pn.deliverypushvalidator.dto.ext.delivery.notification.NotificationInt;
import it.pagopa.pn.deliverypushvalidator.generated.openapi.msclient.notificationcostservice.model.*;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static it.pagopa.pn.deliverypushvalidator.action.utils.PaymentUtils.getAllPaymentsInfoFromNotification;

@Component
public class NotificationCostServiceMapper {

    public NewNotificationCostRequest mapNotificationToRequest(NotificationInt notificationInt) {
        NewNotificationCostRequest newNotificationCostRequest = new NewNotificationCostRequest();
        if (notificationInt != null) {
            mapBasicInfo(notificationInt, newNotificationCostRequest);
            newNotificationCostRequest.setCostRecipients(mapRecipients(notificationInt));
        }
        return newNotificationCostRequest;
    }

    private static void mapBasicInfo(NotificationInt notificationInt, NewNotificationCostRequest newNotificationCostRequest) {
        newNotificationCostRequest.setSenderTaxId(notificationInt.getSender().getPaTaxId());
        newNotificationCostRequest.setSenderPaId(notificationInt.getSender().getPaId());
        newNotificationCostRequest.setVat(notificationInt.getVat());
        newNotificationCostRequest.setPaFee(notificationInt.getPaFee());

        if (notificationInt.getNotificationFeePolicy() != null) {
            newNotificationCostRequest.setNotificationFeePolicy(
                    NotificationFeePolicy.fromValue(notificationInt.getNotificationFeePolicy().getValue())
            );
        }

        if (notificationInt.getPagoPaIntMode() != null) {
            newNotificationCostRequest.setPagoPaIntMode(
                    PagoPaIntMode.fromValue(notificationInt.getPagoPaIntMode().getValue())
            );
        }
    }

    private static List<RecipientCostData> mapRecipients(NotificationInt notificationInt) {
        List<RecipientCostData> recipientCostDataList = new ArrayList<>();

        for (int i = 0; i < notificationInt.getRecipients().size(); i++) {
            RecipientCostData recipient = new RecipientCostData();
            recipient.setRecIndex(i);
            recipient.setRecipientInternalId(notificationInt.getRecipients().get(i).getInternalId());
            recipient.setPayments(mapPaymentsInfo(notificationInt, i));
            recipientCostDataList.add(recipient);
        }
        return recipientCostDataList;
    }

    private static List<PaymentData> mapPaymentsInfo(NotificationInt notificationInt, int recipientIndex) {
        return getAllPaymentsInfoFromNotification(notificationInt)
                .stream()
                .filter(paymentInt -> paymentInt.getRecIndex().equals(recipientIndex))
                .map(paymentInt -> {
                    PaymentData paymentData = new PaymentData();
                    paymentData.setIuv(paymentInt.getCreditorTaxId() + "##" + paymentInt.getNoticeCode());
                    paymentData.setApplyCost(paymentInt.isApplyCost());
                    return paymentData;
                }).toList();
    }

}
