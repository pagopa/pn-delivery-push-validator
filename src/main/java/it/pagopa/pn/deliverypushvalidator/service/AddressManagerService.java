package it.pagopa.pn.deliverypushvalidator.service;

import it.pagopa.pn.deliverypushvalidator.dto.delivery.notification.NotificationInt;
import it.pagopa.pn.deliverypushvalidator.generated.openapi.msclient.addressmanager.model.AcceptedResponse;
import reactor.core.publisher.Mono;

public interface AddressManagerService {
    Mono<AcceptedResponse> normalizeAddresses(NotificationInt notification, String correlationId);
}
