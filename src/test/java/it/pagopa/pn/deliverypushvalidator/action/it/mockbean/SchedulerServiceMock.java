package it.pagopa.pn.deliverypushvalidator.action.it.mockbean;

import it.pagopa.pn.deliverypushvalidator.dto.ext.delivery.notification.CommunicationType;
import it.pagopa.pn.deliverypushvalidator.middleware.queue.producer.abstractions.actionspool.Action;
import it.pagopa.pn.deliverypushvalidator.middleware.queue.producer.abstractions.actionspool.ActionDetails;
import it.pagopa.pn.deliverypushvalidator.middleware.queue.producer.abstractions.actionspool.ActionType;
import it.pagopa.pn.deliverypushvalidator.service.SchedulerService;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;

@Slf4j
public class SchedulerServiceMock implements SchedulerService {
  private final ActionPoolMock actionPoolMock;

  public SchedulerServiceMock(ActionPoolMock actionPoolMock) {
    this.actionPoolMock = actionPoolMock;
  }

  @Override
  public void scheduleEvent(String iun, Integer recIndex, Instant dateToSchedule,
      ActionType actionType, String timelineId, ActionDetails actionDetails, CommunicationType communicationType) {

    log.info("[TEST] Start scheduling with timelineid - iun={} id={} actionType={} timelineid={} datetoschedule={}", iun, recIndex, actionType, timelineId, dateToSchedule);
    Action action = Action.builder()
            .iun(iun)
            .recipientIndex(recIndex)
            .notBefore(dateToSchedule)
            .type(actionType)
            .details(actionDetails)
            .timelineId(timelineId)
            .communicationType(communicationType)
            .build();
    actionPoolMock.addAction(action);
  }

  @Override
  public void scheduleEvent(String iun, Instant dateToSchedule, ActionType actionType, ActionDetails actionDetails, CommunicationType communicationType) {
    scheduleEvent(iun, null, dateToSchedule, actionType, null, actionDetails, communicationType);
  }

  @Override
  public void scheduleEvent(String iun, Instant dateToSchedule, ActionType actionType, ActionDetails actionDetails) {
    scheduleEvent(iun, null, dateToSchedule, actionType, null, actionDetails, null);  }

  @Override
  public void scheduleEvent(String iun, Instant dateToSchedule, ActionType actionType){
    scheduleEvent(iun, null, dateToSchedule, actionType, null, null, null);  }

  @Override
  public void scheduleEvent(String iun, Instant dateToSchedule, ActionType actionType, CommunicationType communicationType) {
    scheduleEvent(iun, null, dateToSchedule, actionType, null, null, communicationType);
  }

}