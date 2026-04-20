package it.pagopa.pn.deliverypushvalidator.utils;

import it.pagopa.pn.deliverypushvalidator.config.PnDeliveryPushValidatorConfigs;
import it.pagopa.pn.deliverypushvalidator.dto.ext.delivery.notification.NotificationInt;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Slf4j
@Getter
@Component
public class NotificationCostServiceFeatureFlagUtils {
    private final PnDeliveryPushValidatorConfigs configs;

    public NotificationCostServiceFeatureFlagUtils(PnDeliveryPushValidatorConfigs configs) {
        this.configs = configs;
    }

    public boolean checkNotificationCostServiceStartDate(NotificationInt notificationInt) {
        if (this.configs.getNotificationCostServiceStartDate() == null) {
            log.warn("NotificationCostService is DISABLED - startDate not configured");
            return false;
        }
        return checkStartDate(notificationInt);
    }

    private boolean checkStartDate(NotificationInt notificationInt) {
        if (this.configs.getNotificationCostServiceStartDate().isBefore(
                Objects.requireNonNull(notificationInt.getSentAt(), "Notification sentAt cannot be null"))) {
            log.info("NotificationCostService is ENABLED  notificationSentAt={}, serviceStartDate={}",
                    notificationInt.getSentAt(),
                    this.configs.getNotificationCostServiceStartDate());
            return true;
        } else {
            log.info("NotificationCostService is DISABLED, notificationSentAt={}, serviceStartDate={}",
                    notificationInt.getSentAt(),
                    this.configs.getNotificationCostServiceStartDate());
            return false;
        }
    }
}
