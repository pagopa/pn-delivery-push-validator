package it.pagopa.pn.deliverypushvalidator.service.impl;

import it.pagopa.pn.deliverypushvalidator.dto.documentcreation.DocumentCreationTypeInt;
import it.pagopa.pn.deliverypushvalidator.middleware.dao.documentcreationdao.DocumentCreationRequestDao;
import it.pagopa.pn.deliverypushvalidator.service.DocumentCreationRequestService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;


@ExtendWith(MockitoExtension.class)
class DocumentCreationRequestServiceImplTest {
    private DocumentCreationRequestService documentCreationRequestService;
    private DocumentCreationRequestDao documentCreationRequestDao;

    @BeforeEach
    void setup() {
        this.documentCreationRequestDao = Mockito.mock(DocumentCreationRequestDao.class);
        this.documentCreationRequestService = new DocumentCreationRequestServiceImpl(documentCreationRequestDao);
    }

    @Test
    void addDocumentCreationRequestTest() {
        String fileKey = "fileKey";
        String iun = "iun";
        String timelineId = "timelineId";
        DocumentCreationTypeInt documentType = DocumentCreationTypeInt.DIGITAL_DELIVERY;

        documentCreationRequestService.addDocumentCreationRequest(fileKey, iun, documentType, timelineId);

        Mockito.verify(documentCreationRequestDao, Mockito.times(1))
                .addDocumentCreationRequest(Mockito.argThat(request ->
                        request.getKey().equals(fileKey) &&
                                request.getIun().equals(iun) &&
                                request.getDocumentCreationType().equals(documentType) &&
                                request.getTimelineId().equals(timelineId)
                ));
    }

    @Test
    void getDocumentCreationRequestTest() {
        String fileKey = "fileKey";
        documentCreationRequestService.getDocumentCreationRequest(fileKey);
        Mockito.verify(documentCreationRequestDao, Mockito.times(1))
                .getDocumentCreationRequest(fileKey);
    }
}
