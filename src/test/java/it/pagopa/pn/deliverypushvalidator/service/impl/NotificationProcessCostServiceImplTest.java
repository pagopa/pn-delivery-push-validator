package it.pagopa.pn.deliverypushvalidator.service.impl;

import it.pagopa.pn.commons.exceptions.PnInternalException;
import it.pagopa.pn.deliverypushvalidator.config.PnDeliveryPushValidatorConfigs;
import it.pagopa.pn.deliverypushvalidator.dto.cost.PaymentsInfoForRecipientInt;
import it.pagopa.pn.deliverypushvalidator.dto.cost.UpdateCostPhaseInt;
import it.pagopa.pn.deliverypushvalidator.dto.cost.UpdateNotificationCostResponseInt;
import it.pagopa.pn.deliverypushvalidator.dto.cost.UpdateNotificationCostResultInt;
import it.pagopa.pn.deliverypushvalidator.generated.openapi.msclient.delivery.model.NotificationFeePolicy;
import it.pagopa.pn.deliverypushvalidator.generated.openapi.msclient.deliverypush.model.NotificationProcessCostResponse;
import it.pagopa.pn.deliverypushvalidator.generated.openapi.msclient.externalregistry_reactive.model.UpdateNotificationCostRequest;
import it.pagopa.pn.deliverypushvalidator.generated.openapi.msclient.externalregistry_reactive.model.UpdateNotificationCostResponse;
import it.pagopa.pn.deliverypushvalidator.generated.openapi.msclient.externalregistry_reactive.model.UpdateNotificationCostResult;
import it.pagopa.pn.deliverypushvalidator.middleware.externalclient.pnclient.deliverypush.PnDeliveryPushClientReactive;
import it.pagopa.pn.deliverypushvalidator.middleware.externalclient.pnclient.externalregistry.PnExternalRegistriesClientReactive;
import it.pagopa.pn.deliverypushvalidator.service.NotificationProcessCostService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static reactor.core.publisher.Mono.when;

class NotificationProcessCostServiceImplTest {
    private PnExternalRegistriesClientReactive pnExternalRegistriesClientReactive;
    private PnDeliveryPushValidatorConfigs cfg;
    private PnDeliveryPushClientReactive pnDeliveryPushClientReactive;
    
    private NotificationProcessCostService service;

    Integer notificationCost = 100;
    Integer notificationFee = 99;
    Integer notificationVat = 22;
    @BeforeEach
    void setUp() {
        this.pnExternalRegistriesClientReactive = Mockito.mock(PnExternalRegistriesClientReactive.class);
        this.pnDeliveryPushClientReactive = Mockito.mock(PnDeliveryPushClientReactive.class);
        this.cfg = Mockito.mock(PnDeliveryPushValidatorConfigs.class);

        Mockito.when(cfg.getPagoPaNotificationBaseCost()).thenReturn(notificationCost);

        service = new NotificationProcessCostServiceImpl(pnExternalRegistriesClientReactive, cfg, pnDeliveryPushClientReactive);
    }

    @Test
    @ExtendWith(SpringExtension.class)
    void setNotificationStepCostOK() {
        //GIVEN
        int notificationStepCost = 100;
        String iun = "testIun";

        PaymentsInfoForRecipientInt paymentsInfoForRecipient = PaymentsInfoForRecipientInt.builder()
                .creditorTaxId("testCred")
                .noticeCode("testNotice")
                .recIndex(0)
                .build();
        List<PaymentsInfoForRecipientInt> paymentsInfoForRecipients = Collections.singletonList(paymentsInfoForRecipient);
        Instant eventTimestamp = Instant.now().minus(2, ChronoUnit.HOURS);
        Instant eventStorageTimestamp = Instant.now().minus(1, ChronoUnit.HOURS);
        UpdateCostPhaseInt updateCostPhase = UpdateCostPhaseInt.VALIDATION;
        
        UpdateNotificationCostResponse updateNotificationCostResponse = new UpdateNotificationCostResponse();
        updateNotificationCostResponse.addUpdateResultsItem(
                new UpdateNotificationCostResult()
                        .creditorTaxId(paymentsInfoForRecipient.getCreditorTaxId())
                        .noticeCode(paymentsInfoForRecipient.getNoticeCode())
                        .recIndex(paymentsInfoForRecipient.getRecIndex())
                        .result(UpdateNotificationCostResult.ResultEnum.KO)
        );
        Mockito.when(pnExternalRegistriesClientReactive.updateNotificationCost(any(UpdateNotificationCostRequest.class))).thenReturn(Mono.just(updateNotificationCostResponse));
        
        //WHEN
        UpdateNotificationCostResponseInt updateNotificationCostResponseInt = service.setNotificationStepCost(notificationStepCost,iun,paymentsInfoForRecipients,eventTimestamp,
                eventStorageTimestamp, updateCostPhase).block();
        
        //THEN
        Assertions.assertNotNull(updateNotificationCostResponseInt);
        Assertions.assertNotNull(updateNotificationCostResponseInt.getUpdateResults());
        Assertions.assertNotNull(updateNotificationCostResponseInt.getUpdateResults().get(0));
        
        UpdateNotificationCostResultInt updateNotificationCostResultInt = updateNotificationCostResponseInt.getUpdateResults().get(0);
        final UpdateNotificationCostResult updateNotificationCostResponseExpected = updateNotificationCostResponse.getUpdateResults().get(0);

        Assertions.assertEquals(updateNotificationCostResponseExpected.getResult().getValue(), updateNotificationCostResultInt.getResult().getValue());
        Assertions.assertEquals(updateNotificationCostResponseExpected.getNoticeCode(), updateNotificationCostResultInt.getPaymentsInfoForRecipient().getNoticeCode());
        Assertions.assertEquals(updateNotificationCostResponseExpected.getCreditorTaxId(), updateNotificationCostResultInt.getPaymentsInfoForRecipient().getCreditorTaxId());
        Assertions.assertEquals(updateNotificationCostResponseExpected.getRecIndex(), updateNotificationCostResultInt.getPaymentsInfoForRecipient().getRecIndex());
    }

    @Test
    @ExtendWith(SpringExtension.class)
    void notificationProcessCostF24_vat_paFee_version23_withTotalCost() {
        //GIVEN
        String iun = "testIun";
        int recIndex = 0;
        int paFee = 0;
        int vat = 22;
        String version = "2.3";
        int notificationProcessTotalCostExpected = 100;
        NotificationProcessCostResponse notificationProcessCostResponse = new NotificationProcessCostResponse();
        notificationProcessCostResponse.setTotalCost(notificationProcessTotalCostExpected);

        Mockito.when(pnDeliveryPushClientReactive.getNotificationProcessCost(any(), any(), any(), any(), any(), any()))
                .thenReturn(Mono.just(notificationProcessCostResponse));

        //WHEN
        Integer notificationCost = service.notificationProcessCostF24(
                iun,
                recIndex,
                NotificationFeePolicy.DELIVERY_MODE,
                paFee,
                vat,
                version
        ).block();

        //THEN
        Assertions.assertNotNull(notificationCost);
        Assertions.assertEquals(notificationProcessTotalCostExpected, notificationCost);
    }

    @Test
    @ExtendWith(SpringExtension.class)
    void notificationProcessCostF24_vat_paFee_version22_withPartialCost() {
        //GIVEN
        String iun = "testIun";
        int recIndex = 0;
        int paFee = 0;
        int vat = 22;
        String version = "2.2";
        int notificationProcessTotalCostExpected = 50;
        NotificationProcessCostResponse notificationProcessCostResponse = new NotificationProcessCostResponse();
        notificationProcessCostResponse.setPartialCost(notificationProcessTotalCostExpected);

        Mockito.when(pnDeliveryPushClientReactive.getNotificationProcessCost(iun, recIndex, NotificationFeePolicy.DELIVERY_MODE, true, paFee, vat))
                .thenReturn(Mono.just(notificationProcessCostResponse));

        //WHEN
        Integer notificationCost = service.notificationProcessCostF24(
                iun,
                recIndex,
                NotificationFeePolicy.DELIVERY_MODE,
                paFee,
                vat,
                version
        ).block();

        //THEN
        Assertions.assertNotNull(notificationCost);
        Assertions.assertEquals(notificationProcessTotalCostExpected, notificationCost);
    }

    @Test
    @ExtendWith(SpringExtension.class)
    void notificationProcessCostF24_vat_paFee_version23_withPartialCost() {
        //GIVEN
        String iun = "testIun";
        int recIndex = 0;
        int paFee = 0;
        int vat = 22;
        String version = "2.3";
        int notificationProcessTotalCostExpected = 50;
        NotificationProcessCostResponse notificationProcessCostResponse = new NotificationProcessCostResponse();
        notificationProcessCostResponse.setPartialCost(notificationProcessTotalCostExpected);

        Mockito.when(pnDeliveryPushClientReactive.getNotificationProcessCost(any(), any(), any(), any(), any(), any()))
                .thenReturn(Mono.just(notificationProcessCostResponse));

        Mono<Integer> response = service.notificationProcessCostF24(
                iun,
                recIndex,
                NotificationFeePolicy.DELIVERY_MODE,
                paFee,
                vat,
                version
        );
        //WHEN
        StepVerifier.create(response)
                .expectError(PnInternalException.class)
                .verify();
    }
}