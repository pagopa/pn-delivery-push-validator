package it.pagopa.pn.deliverypushvalidator.middleware.queue.producer.abstractions.actionspool;

import it.pagopa.pn.deliverypushvalidator.action.details.*;
import lombok.Getter;

@Getter
public enum ActionType {

  NOTIFICATION_VALIDATION(NotificationValidationActionDetails.class) {
    @Override
    public String buildActionId(Action action) {
      NotificationValidationActionDetails details = (NotificationValidationActionDetails) action.getDetails();
      
      return String.format("notification_validation_iun_%s_retry=%d", 
              action.getIun(),
              details.getRetryAttempt());
    }
  },

  NOTIFICATION_REFUSED(NotificationRefusedActionDetails.class) {
    @Override
    public String buildActionId(Action action) {
      return String.format("notification_refused_iun_%s",
              action.getIun()
      );
    }
  },

  SCHEDULE_RECEIVED_LEGALFACT_GENERATION(NotHandledDetails.class) {
    @Override
    public String buildActionId(Action action) {
      return String.format("schedule_creation_received_iun_%s",
              action.getIun()
      );
    }
  },

  CHECK_ATTACHMENT_RETENTION(NotHandledDetails.class) {
    @Override
    public String buildActionId(Action action) {
      return String.format("check_attachment_retention_iun_%s_scheduling-date_%s",
              action.getIun(),
              action.getNotBefore()
      );
    }
  },
  
  SENDER_ACK(NotHandledDetails.class) {

    @Override
    public String buildActionId(Action action) {
      return String.format("%s_start", action.getIun());
    }
  },

  DOCUMENT_CREATION_RESPONSE(DocumentCreationResponseActionDetails.class) {
    @Override
    public String buildActionId(Action action) {
        return String.format("safe_storage_response_timelineId=%s",
                action.getTimelineId()
        );
    }
    
  },

  POST_ACCEPTED_PROCESSING_COMPLETED(NotHandledDetails.class) {
    @Override
    public String buildActionId(Action action) {
      return String.format("%s_post_accepted_processing",
              action.getIun());
    }
  };

  private final Class<? extends ActionDetails> detailsJavaClass;

  ActionType(Class<? extends ActionDetails> detailsJavaClass) {
    this.detailsJavaClass = detailsJavaClass;
  }

  public String buildActionId(Action action) {
    throw new UnsupportedOperationException("Must be implemented for each action type");
  }

}
