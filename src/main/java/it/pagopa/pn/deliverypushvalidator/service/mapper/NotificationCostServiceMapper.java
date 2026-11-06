package it.pagopa.pn.deliverypushvalidator.service.mapper;

import it.pagopa.pn.deliverypushvalidator.dto.cost.PaymentsInfoForRecipientInt;
import it.pagopa.pn.deliverypushvalidator.dto.ext.delivery.notification.NotificationInt;
import it.pagopa.pn.deliverypushvalidator.generated.openapi.msclient.notificationcostservice.model.*;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static it.pagopa.pn.deliverypushvalidator.action.utils.PaymentUtils.getAllPaymentsInfoFromNotification;

@Component
public class NotificationCostServiceMapper {

    public NewNotificationCostRequest mapNotificationToNewNotificationCostRequest(NotificationInt notificationInt) {
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

        Map<Integer, List<PaymentsInfoForRecipientInt>> paymentsByRecipient =
                getAllPaymentsInfoFromNotification(notificationInt)
                        .stream()
                        .collect(Collectors.groupingBy(PaymentsInfoForRecipientInt::getRecIndex));

        for (int i = 0; i < notificationInt.getRecipients().size(); i++) {
            RecipientCostData recipient = new RecipientCostData();
            recipient.setRecIndex(i);
            recipient.setRecipientInternalId(notificationInt.getRecipients().get(i).getInternalId());
            recipient.setPayments(mapPaymentsInfo(paymentsByRecipient.getOrDefault(i, List.of())));
            recipientCostDataList.add(recipient);
        }
        return recipientCostDataList;
    }

    private static List<PaymentData> mapPaymentsInfo(List<PaymentsInfoForRecipientInt> paymentsForRecipient) {
        return paymentsForRecipient
                .stream()
                .map(paymentInt -> {
                    PaymentData paymentData = new PaymentData();
                    paymentData.setIuv(paymentInt.getCreditorTaxId() + "##" + paymentInt.getNoticeCode());
                    paymentData.setApplyCost(paymentInt.isApplyCost());
                    return paymentData;
                }).toList();
    }

    public NewInformalNotificationCostRequest mapNotificationToNewInformalNotificationCostRequest(NotificationInt notificationInt) {
        NewInformalNotificationCostRequest newInformalNotificationCostRequest = new NewInformalNotificationCostRequest();
        if (notificationInt != null) {
            newInformalNotificationCostRequest.setSenderTaxId(notificationInt.getSender().getPaTaxId());
            newInformalNotificationCostRequest.setSenderPaId(notificationInt.getSender().getPaId());
            newInformalNotificationCostRequest.setCostRecipients(mapInformalRecipients(notificationInt));
        }
        return newInformalNotificationCostRequest;
    }

    private static List<InformalRecipientCostData> mapInformalRecipients(NotificationInt notificationInt) {
        List<InformalRecipientCostData> recipientCostDataList = new ArrayList<>();

        Map<Integer, List<PaymentsInfoForRecipientInt>> paymentsByRecipient =
                getAllPaymentsInfoFromNotification(notificationInt)
                        .stream()
                        .collect(Collectors.groupingBy(PaymentsInfoForRecipientInt::getRecIndex));

        for (int i = 0; i < notificationInt.getRecipients().size(); i++) {
            InformalRecipientCostData recipient = new InformalRecipientCostData();
            recipient.setRecIndex(i);
            recipient.setRecipientInternalId(notificationInt.getRecipients().get(i).getInternalId());
            recipient.setPayments(mapInformalPaymentsInfo(paymentsByRecipient.getOrDefault(i, List.of())));
            recipientCostDataList.add(recipient);
        }
        return recipientCostDataList;
    }

    private static List<InformalPaymentData> mapInformalPaymentsInfo(List<PaymentsInfoForRecipientInt> paymentsForRecipient) {
        return paymentsForRecipient
                .stream()
                .map(paymentInt -> {
                    InformalPaymentData paymentData = new InformalPaymentData();
                    paymentData.setIuv(paymentInt.getCreditorTaxId() + "##" + paymentInt.getNoticeCode());
                    return paymentData;
                }).toList();
    }

}
