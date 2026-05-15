package it.pagopa.pn.deliverypushvalidator.action.refused;

import it.pagopa.pn.deliverypushvalidator.action.utils.PaymentUtils;
import it.pagopa.pn.deliverypushvalidator.action.utils.TimelineUtils;
import it.pagopa.pn.deliverypushvalidator.dto.cost.PaymentsInfoForRecipientInt;
import it.pagopa.pn.deliverypushvalidator.dto.cost.UpdateCostPhaseInt;
import it.pagopa.pn.deliverypushvalidator.dto.cost.UpdateNotificationCostResponseInt;
import it.pagopa.pn.deliverypushvalidator.dto.ext.delivery.notification.NotificationInt;
import it.pagopa.pn.deliverypushvalidator.dto.ext.delivery.notification.PagoPaIntMode;
import it.pagopa.pn.deliverypushvalidator.dto.timeline.NotificationRefusedErrorInt;
import it.pagopa.pn.deliverypushvalidator.generated.openapi.msclient.delivery.model.NotificationFeePolicy;
import it.pagopa.pn.deliverypushvalidator.service.NotificationProcessCostService;
import it.pagopa.pn.deliverypushvalidator.service.NotificationService;
import it.pagopa.pn.deliverypushvalidator.service.TimelineService;
import it.pagopa.pn.deliverypushvalidator.utils.RefusalCostCalculator;
import lombok.AllArgsConstructor;
import lombok.CustomLog;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

import static it.pagopa.pn.deliverypushvalidator.action.utils.PaymentUtils.handleResponse;

@Component
@AllArgsConstructor
@CustomLog
public class LegalNotificationRefusedStrategy implements NotificationRefusedStrategy {
    public static final int NOTIFICATION_REFUSED_COST = 0;

    private final NotificationService notificationService;
    private final TimelineUtils timelineUtils;
    private final TimelineService timelineService;
    private final NotificationProcessCostService notificationProcessCostService;
    private final RefusalCostCalculator refusalCostCalculator;

    @Override
    public NotificationInt getNotification(String iun) {
        return notificationService.getNotificationByIun(iun);
    }

    @Override
    public void handleNotificationRefused(String iun, List<NotificationRefusedErrorInt> errors, Instant notBefore) {
        log.debug("Start handleNotificationRefused - iun={}", iun);

        NotificationInt notification = getNotification(iun);
        List<NotificationRefusedErrorInt> refusedErrors = castToRefusedErrors(errors);
        int notificationCost = refusalCostCalculator.calculateRefusalCost(notification, refusedErrors);

        if (NotificationFeePolicy.DELIVERY_MODE.equals(notification.getNotificationFeePolicy())
                && PagoPaIntMode.ASYNC.equals(notification.getPagoPaIntMode())) {
            handleUpdateNotificationCost(notBefore, notification);
        } else {
            log.debug("don't need to update notification cost - iun={}", iun);
        }

        timelineService.addTimelineElement(
                timelineUtils.buildRefusedRequestTimelineElement(notification, refusedErrors, notificationCost),
                notification
        );
    }

    private void handleUpdateNotificationCost(Instant schedulingTime, NotificationInt notification) {
        List<PaymentsInfoForRecipientInt> paymentsInfoForRecipients = PaymentUtils.getPaymentsInfoWithApplyCostFromNotification(notification);

        if (!paymentsInfoForRecipients.isEmpty()) {
            UpdateNotificationCostResponseInt updateNotificationCostResponse = notificationProcessCostService.setNotificationStepCost(
                    NOTIFICATION_REFUSED_COST,
                    notification.getIun(),
                    paymentsInfoForRecipients,
                    schedulingTime,
                    schedulingTime,
                    UpdateCostPhaseInt.REQUEST_REFUSED
            ).block();

            if (updateNotificationCostResponse != null && updateNotificationCostResponse.getUpdateResults() != null && !updateNotificationCostResponse.getUpdateResults().isEmpty()) {
                handleResponse(notification, updateNotificationCostResponse);
            }
        } else {
            log.debug("Don't need to update notification cost, paymentsInfoForRecipients is empty - iun={}", notification.getIun());
        }
    }

    private List<NotificationRefusedErrorInt> castToRefusedErrors(List<NotificationRefusedErrorInt> errors) {

        return errors;
    }
}

