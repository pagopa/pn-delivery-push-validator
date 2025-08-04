package it.pagopa.pn.deliverypushvalidator.middleware.queue.producer.abstractions.actionspool;


public interface ActionsPool {
    void addOnlyAction(Action action);
    void unscheduleFutureAction( String actionId );
}
