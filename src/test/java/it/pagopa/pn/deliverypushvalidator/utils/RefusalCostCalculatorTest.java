package it.pagopa.pn.deliverypushvalidator.utils;

import it.pagopa.pn.commons.exceptions.PnInternalException;
import it.pagopa.pn.deliverypushvalidator.action.refused.RefusalCostPolicy;
import it.pagopa.pn.deliverypushvalidator.action.refused.RefusalCostPolicyProvider;
import it.pagopa.pn.deliverypushvalidator.dto.cost.RefusalCostMode;
import it.pagopa.pn.deliverypushvalidator.dto.ext.datavault.RecipientTypeInt;
import it.pagopa.pn.deliverypushvalidator.dto.ext.delivery.notification.NotificationInt;
import it.pagopa.pn.deliverypushvalidator.dto.ext.delivery.notification.NotificationRecipientInt;
import it.pagopa.pn.deliverypushvalidator.dto.timeline.NotificationRefusedErrorInt;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RefusalCostCalculatorTest {
    @Mock
    private RefusalCostPolicyProvider refusalCostPolicyProvider;

    private RefusalCostCalculator refusalCostCalculator;
    
    private static final int SEND_FEE = 100;
    private static final int TECHNICAL_REFUSAL_COST = 20;
    private static final int UNIFORM_TECHNICAL_REFUSAL_COST = 50;

    private static final String TECHNICAL_ERROR_CODE = "ADDRESS_SEARCH_FAILED";
    private static final String NON_TECHNICAL_ERROR_CODE = "ERROR_CODE";

    @BeforeEach
    void setUp() {
        refusalCostCalculator = new RefusalCostCalculator(refusalCostPolicyProvider);
    }

    @Test
    void calculateRefusalCost_NoTechnicalErrors() {
        NotificationInt notification = mockNotificationWithThreeRecipients();
        mockPolicy(new RefusalCostPolicy(RefusalCostMode.RECIPIENT_BASED, TECHNICAL_REFUSAL_COST, SEND_FEE));

        int result = refusalCostCalculator.calculateRefusalCost(notification, Collections.emptyList());

        Assertions.assertEquals(3 * SEND_FEE, result);
    }

    @Test
    void calculateRefusalCost_WithTechnicalErrors_UniformMode() {
        NotificationInt notification = mockNotificationWithThreeRecipients();
        mockPolicy(new RefusalCostPolicy(RefusalCostMode.UNIFORM, UNIFORM_TECHNICAL_REFUSAL_COST, SEND_FEE));

        int result = refusalCostCalculator.calculateRefusalCost(notification, getErrorsList());

        Assertions.assertEquals(UNIFORM_TECHNICAL_REFUSAL_COST, result);
    }

    @Test
    void calculateRefusalCost_WithTechnicalErrors_RecipientBasedMode() {
        NotificationInt notification = mockNotificationWithThreeRecipients();
        mockPolicy(new RefusalCostPolicy(RefusalCostMode.RECIPIENT_BASED, TECHNICAL_REFUSAL_COST, SEND_FEE));

        int result = refusalCostCalculator.calculateRefusalCost(notification, getErrorsList());

        // 1 recipient with technical error (20) + 2 recipients without (100 each)
        Assertions.assertEquals(220, result);
    }

    @Test
    void calculateRefusalCost_InvalidNumberOfRecipients() {
        NotificationInt notification = mockNotificationWithThreeRecipients();
        mockPolicy(new RefusalCostPolicy(RefusalCostMode.RECIPIENT_BASED, TECHNICAL_REFUSAL_COST, SEND_FEE));

        List<NotificationRefusedErrorInt> errors = new ArrayList<>();
        errors.add(NotificationRefusedErrorInt.builder().recIndex(0).errorCode(TECHNICAL_ERROR_CODE).build());
        errors.add(NotificationRefusedErrorInt.builder().recIndex(1).errorCode(TECHNICAL_ERROR_CODE).build());
        errors.add(NotificationRefusedErrorInt.builder().recIndex(2).errorCode(TECHNICAL_ERROR_CODE).build());
        errors.add(NotificationRefusedErrorInt.builder().recIndex(3).errorCode(TECHNICAL_ERROR_CODE).build()); // Extra error

        PnInternalException exception = Assertions.assertThrows(PnInternalException.class, () ->
                refusalCostCalculator.calculateRefusalCost(notification, errors)
        );

        Assertions.assertEquals("Invalid number of recipients not affected by technical errors", exception.getProblem().getDetail());
        Assertions.assertEquals("INVALID_NUMBER_OF_RECIPIENTS", exception.getProblem().getErrors().getFirst().getCode());
    }

    private NotificationInt mockNotificationWithThreeRecipients() {
        NotificationInt notification = mock(NotificationInt.class);
        when(notification.getRecipients()).thenReturn(getRecipientsList());
        return notification;
    }

    private void mockPolicy(RefusalCostPolicy policy) {
        when(refusalCostPolicyProvider.get(any())).thenReturn(policy);
    }

    private List<NotificationRecipientInt> getRecipientsList() {
        List<NotificationRecipientInt> recipients = new ArrayList<>();

        recipients.add(NotificationRecipientInt.builder()
                .taxId("taxId1")
                .recipientType(RecipientTypeInt.PF)
                .build());

        recipients.add(NotificationRecipientInt.builder()
                .taxId("taxId2")
                .recipientType(RecipientTypeInt.PG)
                .build());

        recipients.add(NotificationRecipientInt.builder()
                .taxId("taxId3")
                .recipientType(RecipientTypeInt.PG)
                .build());

        return recipients;
    }

    private List<NotificationRefusedErrorInt> getErrorsList() {
        List<NotificationRefusedErrorInt> errors = new ArrayList<>();
        errors.add(NotificationRefusedErrorInt.builder()
                .recIndex(0)
                .errorCode(TECHNICAL_ERROR_CODE)
                .build());
        errors.add(NotificationRefusedErrorInt.builder()
                .recIndex(1)
                .errorCode(NON_TECHNICAL_ERROR_CODE)
                .build());
        errors.add(NotificationRefusedErrorInt.builder()
                .recIndex(2)
                .errorCode(NON_TECHNICAL_ERROR_CODE)
                .build());
        return errors;
    }
}