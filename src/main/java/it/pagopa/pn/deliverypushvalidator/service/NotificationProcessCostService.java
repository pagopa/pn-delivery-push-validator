package it.pagopa.pn.deliverypushvalidator.service;


import it.pagopa.pn.deliverypushvalidator.dto.cost.PaymentsInfoForRecipientInt;
import it.pagopa.pn.deliverypushvalidator.dto.cost.UpdateCostPhaseInt;
import it.pagopa.pn.deliverypushvalidator.dto.cost.UpdateNotificationCostResponseInt;
import it.pagopa.pn.deliverypushvalidator.generated.openapi.msclient.delivery.model.NotificationFeePolicy;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.List;

public interface NotificationProcessCostService {
    Mono<UpdateNotificationCostResponseInt> setNotificationStepCost(int notificationStepCost,
                                                                    String iun,
                                                                    List<PaymentsInfoForRecipientInt> paymentsInfoForRecipients,
                                                                    Instant eventTimestamp,
                                                                    Instant eventStorageTimestamp,
                                                                    UpdateCostPhaseInt updateCostPhase);

    int getNotificationBaseCost(int paFee);
    int getSendFee();
    Mono<Integer> notificationProcessCostF24(String iun, int recIndex, NotificationFeePolicy notificationFeePolicy, Integer paFee, Integer vat, String version);
}
