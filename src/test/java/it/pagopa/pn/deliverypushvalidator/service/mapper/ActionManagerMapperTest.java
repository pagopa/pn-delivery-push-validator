package it.pagopa.pn.deliverypushvalidator.service.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.pagopa.pn.deliverypushvalidator.action.details.DocumentCreationResponseActionDetails;
import it.pagopa.pn.deliverypushvalidator.dto.documentcreation.DocumentCreationTypeInt;
import it.pagopa.pn.deliverypushvalidator.dto.ext.delivery.notification.CommunicationType;
import it.pagopa.pn.deliverypushvalidator.middleware.queue.producer.abstractions.actionspool.Action;
import it.pagopa.pn.deliverypushvalidator.middleware.queue.producer.abstractions.actionspool.ActionType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SpringExtension.class)
class ActionManagerMapperTest {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void fromActionInternalToActionDto_shouldMapFieldsAndSerializeDetails() {

        DocumentCreationResponseActionDetails details = DocumentCreationResponseActionDetails.builder()
                .key("key1")
                .documentCreationType(DocumentCreationTypeInt.SENDER_ACK.getValue())
                .timelineId("timeline1")
                .build();

        Action action = Action.builder()
                .actionId("id1")
                .iun("iun1")
                .notBefore(Instant.now())
                .type(ActionType.SENDER_ACK)
                .recipientIndex(2)
                .details(details)
                .timelineId("timeline1")
                .communicationType(CommunicationType.INFORMAL)
                .build();

        ActionManagerMapper mapper = new ActionManagerMapper(objectMapper);
        var dto = mapper.fromActionInternalToActionDto(action);

        assertEquals("id1", dto.getActionId());
        assertEquals("iun1", dto.getIun());
        assertEquals("timeline1", dto.getTimelineId());
        assertEquals(2, dto.getRecipientIndex());
        assertEquals("SENDER_ACK", dto.getType().name());
        Assertions.assertNotNull(dto.getCommunicationType());
        assertEquals("INFORMAL", dto.getCommunicationType().name());
    }

    @Test
    void fromActionInternalToActionDto_shouldHandleNullDetails() {
        Action action = Action.builder()
                .actionId("id2")
                .type(ActionType.SENDER_ACK)
                .communicationType(CommunicationType.INFORMAL)
                .build();

        ActionManagerMapper mapper = new ActionManagerMapper(objectMapper);
        var dto = mapper.fromActionInternalToActionDto(action);

        assertEquals("", dto.getDetails());
    }

    @Test
    void fromActionInternalToActionDto_shouldHandleNullCommunicationType() {
        Action action = Action.builder()
                .actionId("id3")
                .type(ActionType.SENDER_ACK)
                .build();

        ActionManagerMapper mapper = new ActionManagerMapper(objectMapper);
        var dto = mapper.fromActionInternalToActionDto(action);

        assertEquals(it.pagopa.pn.deliverypushvalidator.generated.openapi.msclient.actionmanager.model.CommunicationType.LEGAL, dto.getCommunicationType());
    }
}
