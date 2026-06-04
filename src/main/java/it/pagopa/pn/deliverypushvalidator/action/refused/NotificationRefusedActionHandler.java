package it.pagopa.pn.deliverypushvalidator.action.refused;

import it.pagopa.pn.deliverypushvalidator.dto.ext.delivery.notification.CommunicationType;
import it.pagopa.pn.deliverypushvalidator.dto.timeline.NotificationRefusedErrorInt;
import it.pagopa.pn.deliverypushvalidator.utils.CommunicationTypeChecker;
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
    private final CommunicationTypeChecker communicationTypeChecker;

    public void notificationRefusedHandler(String iun,
                                           List<NotificationRefusedErrorInt> errors,
                                           Instant schedulingTime,
                                           CommunicationType communicationType) {
        log.debug("Start notificationRefusedHandler - iun={}, communicationType={}", iun, communicationType);
        resolveStrategy(communicationType, iun).handleNotificationRefused(iun, errors, schedulingTime);
    }

    private NotificationRefusedStrategy resolveStrategy(CommunicationType communicationType, String iun) {
        log.debug("Resolve validation strategy for type={} - iun={}", communicationType, iun);
        communicationTypeChecker.checkAgainstIun(communicationType, iun);
        return switch (communicationType) {
            case INFORMAL -> informalNotificationRefusedStrategy;
            case LEGAL -> legalNotificationRefusedStrategy;
        };
    }

}
