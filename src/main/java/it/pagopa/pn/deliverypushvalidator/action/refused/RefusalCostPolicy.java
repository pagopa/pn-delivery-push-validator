package it.pagopa.pn.deliverypushvalidator.action.refused;

import it.pagopa.pn.commons.exceptions.PnInternalException;
import it.pagopa.pn.deliverypushvalidator.dto.cost.RefusalCostMode;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

@Slf4j
public record RefusalCostPolicy(RefusalCostMode mode, int technicalRefusalCost, int sendFee) {
    private static final String ERROR_MESSAGE = "Invalid configuration for technical refusal cost mode";
    private static final String ERROR_CODE = "INVALID_TECHNICAL_REFUSAL_COST_CONFIGURATION";
    private static final String SEPARATOR = ";";
    private static final int INDEX_REFUSAL_COST_MODE = 0;
    private static final int INDEX_REFUSAL_COST = 1;

    // Esempio di configurazione: RECIPIENT_BASED;50
    public static RefusalCostPolicy parse(String rawConfig, int sendFee) {
        // Default: RECIPIENT_BASED con costo tecnico = sendFee del dominio
        if (StringUtils.isBlank(rawConfig)) {
            log.info("No configuration provided for technical refusal cost mode, using default: RECIPIENT_BASED with cost = sendFee");
            return new RefusalCostPolicy(RefusalCostMode.RECIPIENT_BASED, sendFee, sendFee);
        }
        String[] values = rawConfig.split(SEPARATOR);
        if (values.length != 2) {
            throw new PnInternalException(ERROR_MESSAGE, ERROR_CODE);
        }

        RefusalCostMode mode = RefusalCostMode.valueOf(values[INDEX_REFUSAL_COST_MODE]);
        int cost = Integer.parseInt(values[INDEX_REFUSAL_COST]);

        return new RefusalCostPolicy(mode, cost, sendFee);
    }
}
