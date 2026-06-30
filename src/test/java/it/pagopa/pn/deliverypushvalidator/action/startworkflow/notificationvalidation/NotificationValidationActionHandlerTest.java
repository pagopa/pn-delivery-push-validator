package it.pagopa.pn.deliverypushvalidator.action.startworkflow.notificationvalidation;

import it.pagopa.pn.api.dto.events.PnF24MetadataValidationEndEventPayload;
import it.pagopa.pn.api.dto.events.notificationcost.validation.PnNotificationCostValidationEventPayload;
import it.pagopa.pn.deliverypushvalidator.action.details.NotificationValidationActionDetails;
import it.pagopa.pn.deliverypushvalidator.dto.ext.addressmanager.NormalizeItemsResultInt;
import it.pagopa.pn.deliverypushvalidator.dto.ext.delivery.notification.CommunicationType;
import it.pagopa.pn.deliverypushvalidator.utils.CommunicationTypeChecker;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationValidationActionHandlerTest {

    @Mock
    private InformalNotificationValidationStrategy informalStrategy;

    @Mock
    private LegalNotificationValidationStrategy legalStrategy;

    @Mock
    private CommunicationTypeChecker communicationTypeChecker;

    @InjectMocks
    private NotificationValidationActionHandler handler;

    private static final String IUN = "TEST-IUN-001";

    @Test
    void validateNotification_whenInformal_shouldUseInformalStrategy() {
        NotificationValidationActionDetails details = mock(NotificationValidationActionDetails.class);
        handler.validateNotification(IUN, details, CommunicationType.INFORMAL);

        verify(informalStrategy).validate(IUN, details);
        verifyNoInteractions(legalStrategy);
    }

    @Test
    void validateNotification_whenLegal_shouldUseLegalStrategy() {

        NotificationValidationActionDetails details = mock(NotificationValidationActionDetails.class);
        handler.validateNotification(IUN, details, CommunicationType.LEGAL);

        verify(legalStrategy).validate(IUN, details);
        verifyNoInteractions(informalStrategy);
    }

    @Test
    void handleValidateF24Response_whenInformal_shouldUseInformalStrategy() {

        PnF24MetadataValidationEndEventPayload event = mock(PnF24MetadataValidationEndEventPayload.class);
        when(event.getSetId()).thenReturn(IUN);

        handler.handleValidateF24Response(event, CommunicationType.INFORMAL);

        verify(informalStrategy).handleValidateF24Response(event);
        verifyNoInteractions(legalStrategy);
    }

    @Test
    void handleValidateF24Response_whenLegal_shouldUseLegalStrategy() {

        PnF24MetadataValidationEndEventPayload event = mock(PnF24MetadataValidationEndEventPayload.class);
        when(event.getSetId()).thenReturn(IUN);

        handler.handleValidateF24Response(event, CommunicationType.LEGAL);

        verify(legalStrategy).handleValidateF24Response(event);
        verifyNoInteractions(informalStrategy);
    }

    @Test
    void handleValidateAndNormalizeAddressResponse_whenInformal_shouldUseInformalStrategy() {

        NormalizeItemsResultInt result = mock(NormalizeItemsResultInt.class);

        handler.handleValidateAndNormalizeAddressResponse(IUN, result, CommunicationType.INFORMAL);

        verify(informalStrategy).handleValidateAndNormalizeAddressResponse(IUN, result);
        verifyNoInteractions(legalStrategy);
    }

    @Test
    void handleValidateAndNormalizeAddressResponse_whenLegal_shouldUseLegalStrategy() {

        NormalizeItemsResultInt result = mock(NormalizeItemsResultInt.class);

        handler.handleValidateAndNormalizeAddressResponse(IUN, result, CommunicationType.LEGAL);

        verify(legalStrategy).handleValidateAndNormalizeAddressResponse(IUN, result);
        verifyNoInteractions(informalStrategy);
    }

    @Test
    void handleValidateNotificationCost_whenInformal_shouldUseInformalStrategy() {

        PnNotificationCostValidationEventPayload event = mock(PnNotificationCostValidationEventPayload.class);

        handler.handleValidateNotificationCost(IUN, event, CommunicationType.INFORMAL);

        verify(informalStrategy).handleValidateNotificationCost(IUN, event);
        verifyNoInteractions(legalStrategy);
    }

    @Test
    void handleValidateNotificationCost_whenLegal_shouldUseLegalStrategy() {

        PnNotificationCostValidationEventPayload event = mock(PnNotificationCostValidationEventPayload.class);

        handler.handleValidateNotificationCost(IUN, event, CommunicationType.LEGAL);

        verify(legalStrategy).handleValidateNotificationCost(IUN, event);
        verifyNoInteractions(informalStrategy);
    }
}