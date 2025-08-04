package it.pagopa.pn.deliverypushvalidator.middleware.queue.producer.abstractions.actionspool;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import it.pagopa.pn.deliverypushvalidator.action.details.*;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        property = "actionType")
@JsonSubTypes({
        @JsonSubTypes.Type(value = NotHandledDetails.class, name = "ANALOG_WORKFLOW"),
        @JsonSubTypes.Type(value = NotHandledDetails.class, name = "REFINEMENT_NOTIFICATION"),
        @JsonSubTypes.Type(value = NotHandledDetails.class, name = "SENDER_ACK"),
        @JsonSubTypes.Type(value = DocumentCreationResponseActionDetails.class, name = "DOCUMENT_CREATION_RESPONSE"),
        @JsonSubTypes.Type(value = NotificationValidationActionDetails.class, name = "NOTIFICATION_VALIDATION"),
        @JsonSubTypes.Type(value = NotificationRefusedActionDetails.class, name = "NOTIFICATION_REFUSED")
})
public interface ActionDetails {

}
