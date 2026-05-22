package it.pagopa.pn.deliverypushvalidator.action.refused;

import it.pagopa.pn.deliverypushvalidator.dto.ext.delivery.notification.CommunicationType;
import it.pagopa.pn.deliverypushvalidator.dto.timeline.NotificationRefusedErrorInt;
import lombok.AllArgsConstructor;
import lombok.CustomLog;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;


@Component
@AllArgsConstructor
@CustomLog
public class NotificationRefusedActionHandler {
    private final LegalNotificationRefusedStrategy legalNotificationRefusedStrategy;
    private final InformalNotificationRefusedStrategy informalNotificationRefusedStrategy;

    public void notificationRefusedHandler(String iun,
                                           List<NotificationRefusedErrorInt> errors,
                                           Instant schedulingTime,
                                           CommunicationType communicationType) {
        log.debug("Start notificationRefusedHandler - iun={}, communicationType={}", iun, communicationType);
        resolveStrategy(communicationType).handleNotificationRefused(iun, errors, schedulingTime);
    }

    private NotificationRefusedStrategy resolveStrategy(CommunicationType communicationType) {
        if (CommunicationType.INFORMAL.equals(communicationType)) {
            return informalNotificationRefusedStrategy;
        }
        return legalNotificationRefusedStrategy;
    }

}
