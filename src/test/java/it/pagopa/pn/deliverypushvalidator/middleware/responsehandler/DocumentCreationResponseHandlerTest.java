package it.pagopa.pn.deliverypushvalidator.middleware.responsehandler;

import it.pagopa.pn.deliverypushvalidator.action.details.DocumentCreationResponseActionDetails;
import it.pagopa.pn.deliverypushvalidator.action.startworkflow.ReceivedLegalFactCreationResponseHandler;
import it.pagopa.pn.deliverypushvalidator.dto.documentcreation.DocumentCreationTypeInt;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;

class DocumentCreationResponseHandlerTest {

    private ReceivedLegalFactCreationResponseHandler receivedLegalFactHandler;
    private DocumentCreationResponseHandler handler;

    @BeforeEach
    void setUp() {
        receivedLegalFactHandler = mock(ReceivedLegalFactCreationResponseHandler.class);
        handler = new DocumentCreationResponseHandler(receivedLegalFactHandler);
    }

    @Test
    void handleResponseReceived_senderAck_callsHandler() {
        DocumentCreationResponseActionDetails details = mock(DocumentCreationResponseActionDetails.class);
        when(details.getKey()).thenReturn("fileKey");
        when(details.getDocumentCreationType()).thenReturn(DocumentCreationTypeInt.SENDER_ACK.getValue());

        handler.handleResponseReceived("iunTest", details);

        verify(receivedLegalFactHandler, times(1))
                .handleReceivedLegalFactCreationResponse("iunTest", "fileKey");
    }

   @Test
   void handleResponseReceived_otherType_doesNotCallHandler() {
       DocumentCreationResponseActionDetails details = mock(DocumentCreationResponseActionDetails.class);
       when(details.getKey()).thenReturn("fileKey");
       when(details.getDocumentCreationType()).thenReturn(DocumentCreationTypeInt.NOTIFICATION_CANCELLED.getValue());

       handler.handleResponseReceived("iunTest", details);

       verify(receivedLegalFactHandler, never())
               .handleReceivedLegalFactCreationResponse(anyString(), anyString());
   }
}
