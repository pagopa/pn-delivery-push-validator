package it.pagopa.pn.deliverypushvalidator.service.impl;

import it.pagopa.pn.commons.exceptions.PnInternalException;
import it.pagopa.pn.deliverypushvalidator.dto.ext.delivery.notification.NotificationInt;
import it.pagopa.pn.deliverypushvalidator.generated.openapi.msclient.delivery.model.SentNotificationV25;
import it.pagopa.pn.deliverypushvalidator.middleware.externalclient.pnclient.delivery.PnDeliveryClient;
import it.pagopa.pn.deliverypushvalidator.service.NotificationService;
import it.pagopa.pn.deliverypushvalidator.service.mapper.NotificationMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

import static it.pagopa.pn.deliverypushvalidator.exception.PnDeliveryPushValidatorExceptionCodes.ERROR_CODE_DELIVERYPUSH_NOTIFICATIONFAILED;


@Service
@Slf4j
public class NotificationServiceImpl implements NotificationService {
    private final PnDeliveryClient pnDeliveryClient;

    public NotificationServiceImpl(PnDeliveryClient pnDeliveryClient) {
        this.pnDeliveryClient = pnDeliveryClient;
    }

    @Override
    public NotificationInt getNotificationByIun(String iun) {
        SentNotificationV25 sentNotification = pnDeliveryClient.getSentNotification(iun);
        log.debug("Get notification OK for - iun {}", iun);

        if (sentNotification != null) {
            return NotificationMapper.externalToInternal(sentNotification);
        } else {
            log.error("Get notification is not valid for - iun {}", iun);
            throw new PnInternalException("Get notification is not valid for - iun " + iun, ERROR_CODE_DELIVERYPUSH_NOTIFICATIONFAILED);
        }        
    }
}
