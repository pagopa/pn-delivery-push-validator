package it.pagopa.pn.deliverypushvalidator.action.refused;

import it.pagopa.pn.commons.exceptions.PnInternalException;
import it.pagopa.pn.deliverypushvalidator.config.PnDeliveryPushValidatorConfigs;
import it.pagopa.pn.deliverypushvalidator.dto.ext.delivery.notification.CommunicationType;
import it.pagopa.pn.deliverypushvalidator.service.NotificationProcessCostService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.Map;

@Slf4j
@Component
public class RefusalCostPolicyProvider {

    private final Map<CommunicationType, RefusalCostPolicy> policies = new EnumMap<>(CommunicationType.class);

    public RefusalCostPolicyProvider(PnDeliveryPushValidatorConfigs configs,
                                     NotificationProcessCostService costService) {
        policies.put(CommunicationType.LEGAL, RefusalCostPolicy.parse(
                configs.getTechnicalRefusalCostMode(),
                costService.getSendFee()));

        policies.put(CommunicationType.INFORMAL, RefusalCostPolicy.parse(
                configs.getInformalTechnicalRefusalCostMode(),
                costService.getInformalSendFee()));

        policies.forEach((type, policy) -> log.info("Refusal cost policy for {}: {}", type, policy));
    }

    public RefusalCostPolicy get(CommunicationType type) {
        RefusalCostPolicy policy = policies.get(type);
        if (policy == null) {
            throw new PnInternalException("Missing refusal cost policy for " + type, "MISSING_REFUSAL_COST_POLICY");
        }
        return policy;
    }
}
