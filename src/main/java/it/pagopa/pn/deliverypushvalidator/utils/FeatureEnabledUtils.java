package it.pagopa.pn.deliverypushvalidator.utils;

import it.pagopa.pn.deliverypushvalidator.config.PnDeliveryPushValidatorConfigs;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Optional;

@AllArgsConstructor
@Component
public class FeatureEnabledUtils {

    private final PnDeliveryPushValidatorConfigs configs;

    public boolean isPfNewWorkflowEnabled(Instant notificationSentAt) {
        boolean isEnabled = false;
        Instant startDate = Instant.parse(configs.getPfNewWorkflowStart());
        Instant endDate = Instant.parse(configs.getPfNewWorkflowStop());
        if (notificationSentAt.compareTo(startDate) >= 0 && notificationSentAt.compareTo(endDate) <= 0) {
            isEnabled = true;
        }
        return isEnabled;
    }

    public boolean isFeatureAAROnlyPECForRADDAndPFEnabled(){
        return Optional.ofNullable(configs.getAAROnlyPECForRADDAndPF())
                .map("true"::equalsIgnoreCase)
                .orElse(false);
    }

}
