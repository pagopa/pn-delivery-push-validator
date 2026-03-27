package it.pagopa.pn.deliverypushvalidator.action.utils;

import it.pagopa.pn.deliverypushvalidator.dto.cost.PaymentsInfoForRecipientInt;
import it.pagopa.pn.deliverypushvalidator.dto.cost.UpdateNotificationCostResponseInt;
import it.pagopa.pn.deliverypushvalidator.dto.ext.delivery.notification.NotificationInt;
import it.pagopa.pn.deliverypushvalidator.dto.ext.delivery.notification.NotificationPaymentInfoInt;
import it.pagopa.pn.deliverypushvalidator.dto.ext.delivery.notification.PagoPaInt;
import it.pagopa.pn.deliverypushvalidator.exception.PnPaymentUpdateRetryException;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

@Slf4j
public class PaymentUtils {
    private PaymentUtils(){}

    @NotNull
    public static List<PaymentsInfoForRecipientInt> getAllPaymentsInfoFromNotification(NotificationInt notification) {
        return getPaymentsInfoFiltered(notification, payment -> true);
    }

    @NotNull
    public static List<PaymentsInfoForRecipientInt> getPaymentsInfoWithApplyCostFromNotification(NotificationInt notification) {
        return getPaymentsInfoFiltered(notification,
                p -> p.getPagoPA() != null && Boolean.TRUE.equals(p.getPagoPA().getApplyCost()));
    }

    private static List<PaymentsInfoForRecipientInt> getPaymentsInfoFiltered(
            NotificationInt notification,
            Predicate<NotificationPaymentInfoInt> filter) {

        List<PaymentsInfoForRecipientInt> paymentsInfoForRecipients = new ArrayList<>();

        notification.getRecipients().forEach(recipient -> {
            int recIndex = NotificationUtils.getRecipientIndexFromTaxId(notification, recipient.getTaxId());
            log.debug("Start add validation for recipient index {}", recIndex);
            if (recipient.getPayments() != null) {
                recipient.getPayments().stream()
                        .filter(filter)
                        .forEach(payment -> {
                            final PagoPaInt pagoPa = payment.getPagoPA();
                            if (pagoPa != null) {
                                log.debug("Add validation for creditorTaxId={} noticeCode={} recIndex={}", pagoPa.getCreditorTaxId(), pagoPa.getNoticeCode(), recIndex);
                                paymentsInfoForRecipients.add(PaymentsInfoForRecipientInt.builder()
                                        .recIndex(recIndex)
                                        .noticeCode(pagoPa.getNoticeCode())
                                        .creditorTaxId(pagoPa.getCreditorTaxId())
                                        .applyCost(Objects.requireNonNull(pagoPa.getApplyCost()))
                                        .build());
                            }
                        });
            } else {
                log.debug("Don't need to add payments for iun={} recIndex={}", notification.getIun(), recIndex);
            }
        });

        return paymentsInfoForRecipients;
    }

    public static void handleResponse(NotificationInt notification, UpdateNotificationCostResponseInt updateNotificationCostResponse) {
        log.debug("Start handle update cost response");

        updateNotificationCostResponse.getUpdateResults().forEach(response -> {
            PaymentsInfoForRecipientInt paymentsInfo = response.getPaymentsInfoForRecipient();

            log.debug("Start handle update response for iun={} recIndex={} creditorTaxId={} noticeCode={}",
                    notification.getIun(), paymentsInfo.getRecIndex(), paymentsInfo.getCreditorTaxId(), paymentsInfo.getNoticeCode());

            switch (response.getResult()) {
                case OK -> log.debug("Update cost OK for iun={} recIndex={} creditorTaxId={} noticeCode={}",
                        notification.getIun(), paymentsInfo.getRecIndex(), paymentsInfo.getCreditorTaxId(), paymentsInfo.getNoticeCode());
                case KO -> log.error("Payment information is not valid. Can't update notification cost" +
                        " - creditorTaxId={} noticeCode={}", paymentsInfo.getCreditorTaxId(), paymentsInfo.getNoticeCode());
                case RETRY -> {
                    final String errorDetail = String.format("Update notification fee error, can't have response from service. iun=%s recIndex=%s creditorTaxId=%s noticeCode=%s",
                            notification.getIun(), paymentsInfo.getRecIndex(), paymentsInfo.getCreditorTaxId(), paymentsInfo.getNoticeCode());
                    handleRetryError(errorDetail);
                }
                default -> {
                    final String errorDetail = String.format("Update notification fee error. Response received is not handled for iun=%s recIndex=%s creditorTaxId=%s noticeCode=%s",
                            notification.getIun(), paymentsInfo.getRecIndex(), paymentsInfo.getCreditorTaxId(), paymentsInfo.getNoticeCode());
                    handleRetryError(errorDetail);
                }
            }
        });
    }

    private static void handleRetryError(String errorDetail) {
        log.info(errorDetail);
        throw new PnPaymentUpdateRetryException(errorDetail);
    }


}
