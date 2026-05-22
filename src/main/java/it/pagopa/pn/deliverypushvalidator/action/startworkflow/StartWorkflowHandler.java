package it.pagopa.pn.deliverypushvalidator.action.startworkflow;


import it.pagopa.pn.deliverypushvalidator.action.startworkflow.notificationvalidation.NotificationValidationScheduler;
import it.pagopa.pn.deliverypushvalidator.dto.ext.delivery.notification.CommunicationType;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
@Slf4j
public class StartWorkflowHandler {
    private final NotificationValidationScheduler notificationValidationScheduler;
    
    /**
     * Start new Notification Workflow. For all notification recipient send courtesy message and start choose delivery type
     *
     * @param iun Notification unique identifier
     */
    public void startWorkflow(String iun, CommunicationType communicationType ) {
        notificationValidationScheduler.scheduleNotificationValidation(iun, communicationType);
    }

}
