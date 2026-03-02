package it.pagopa.pn.deliverypushvalidator.middleware.dao.documentcreationdao.dynamo.mapper;


import it.pagopa.pn.deliverypushvalidator.dto.documentcreation.DocumentCreationRequest;
import it.pagopa.pn.deliverypushvalidator.middleware.dao.documentcreationdao.dynamo.entity.DocumentCreationRequestEntity;
import org.springframework.stereotype.Component;

@Component
public class EntityToDtoDocumentCreationRequestMapper {

    public DocumentCreationRequest entityToDto(DocumentCreationRequestEntity entity) {
        return DocumentCreationRequest.builder()
                .key(entity.getKey())
                .iun(entity.getIun())
                .recIndex(entity.getRecIndex())
                .documentCreationType(entity.getDocumentType())
                .timelineId(entity.getTimelineId())
                .build();
    }
}

