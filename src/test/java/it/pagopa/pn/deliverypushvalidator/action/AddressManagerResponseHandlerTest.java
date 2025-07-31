package it.pagopa.pn.deliverypushvalidator.action;


import it.pagopa.pn.deliverypushvalidator.action.startworkflow.notificationvalidation.NotificationValidationActionHandler;
import it.pagopa.pn.deliverypushvalidator.action.utils.TimelineUtils;
import it.pagopa.pn.deliverypushvalidator.generated.openapi.msclient.addressmanager.model.NormalizeItemsResult;
import it.pagopa.pn.deliverypushvalidator.middleware.responsehandler.AddressManagerResponseHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit.jupiter.SpringExtension;

class AddressManagerResponseHandlerTest {
    @Mock
    private TimelineUtils timelineUtils;

    @Mock
    private NotificationValidationActionHandler notificationValidationActionHandler;

    private AddressManagerResponseHandler handler;

    @BeforeEach
    public void setup() {
        handler = new AddressManagerResponseHandler(notificationValidationActionHandler, timelineUtils);
    }

    @ExtendWith(SpringExtension.class)
    @Test
    void handleResponseCancelled() {
        //GIVEN
        Mockito.when(timelineUtils.checkIsNotificationCancellationRequested(Mockito.anyString())).thenReturn(true);

        NormalizeItemsResult normalizeItemsResult = new NormalizeItemsResult();
        //WHEN
        handler.handleResponseReceived(normalizeItemsResult);

        //THEN
        Mockito.verify(notificationValidationActionHandler, Mockito.never()).handleValidateAndNormalizeAddressResponse(Mockito.anyString(), Mockito.any());
    }
}
