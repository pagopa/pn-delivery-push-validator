package it.pagopa.pn.deliverypushvalidator.service;



import it.pagopa.pn.deliverypushvalidator.dto.timeline.CommunicationType;
import it.pagopa.pn.deliverypushvalidator.middleware.queue.producer.abstractions.actionspool.ActionDetails;
import it.pagopa.pn.deliverypushvalidator.middleware.queue.producer.abstractions.actionspool.ActionType;

import java.time.Instant;

public interface SchedulerService {
    void scheduleEvent(String iun, Instant dateToSchedule, ActionType actionType);
    
    void scheduleEvent(String iun, Instant dateToSchedule, ActionType actionType, ActionDetails actionDetails);

    void scheduleEvent(String iun, Integer recIndex, Instant dateToSchedule, ActionType actionType, String timelineEventId, ActionDetails actionDetails, CommunicationType communicationType);

    void scheduleEvent(String iun, Instant dateToSchedule, ActionType actionType, ActionDetails actionDetails, CommunicationType communicationType);
}
