package it.pagopa.pn.deliverypushvalidator.service;

import it.pagopa.pn.deliverypushvalidator.dto.documentcreation.DocumentCreationRequest;
import it.pagopa.pn.deliverypushvalidator.dto.documentcreation.DocumentCreationTypeInt;
import java.util.Optional;

public interface DocumentCreationRequestService {
    void addDocumentCreationRequest(String fileKey, String iun, DocumentCreationTypeInt documentType, String timelineId);

    Optional<DocumentCreationRequest> getDocumentCreationRequest(String fileKey);
}
