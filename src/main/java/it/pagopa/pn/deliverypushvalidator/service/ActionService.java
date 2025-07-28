package it.pagopa.pn.deliverypushvalidator.service;


import it.pagopa.pn.deliverypushvalidator.middleware.queue.producer.abstractions.actionspool.Action;

public interface ActionService {
    void addOnlyActionIfAbsent(Action action);
    void unSchedule(String actionId);
}
