package it.pagopa.pn.deliverypushvalidator.middleware.responsehandler;

import it.pagopa.pn.deliverypushvalidator.action.details.DocumentCreationResponseActionDetails;
import it.pagopa.pn.deliverypushvalidator.action.startworkflow.ReceivedLegalFactCreationResponseHandler;
import it.pagopa.pn.deliverypushvalidator.dto.documentcreation.DocumentCreationTypeInt;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
@Slf4j
@AllArgsConstructor
public class DocumentCreationResponseHandler {
    private final ReceivedLegalFactCreationResponseHandler receivedLegalFactHandler;

    public void handleResponseReceived( String iun, DocumentCreationResponseActionDetails details) {
        String fileKey = details.getKey();
        DocumentCreationTypeInt documentCreationType = details.getDocumentCreationType();

        if (Objects.requireNonNull(documentCreationType) == DocumentCreationTypeInt.SENDER_ACK) {
            receivedLegalFactHandler.handleReceivedLegalFactCreationResponse(iun, fileKey);
        } else {
            log.warn("DocumentCreationResponseHandler: documentCreationType={} not supported for iun={}", documentCreationType, iun);
        }
    }
}
