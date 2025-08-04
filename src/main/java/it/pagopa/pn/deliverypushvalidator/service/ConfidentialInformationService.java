package it.pagopa.pn.deliverypushvalidator.service;

import it.pagopa.pn.deliverypushvalidator.dto.ext.datavault.NotificationRecipientAddressesDtoInt;
import reactor.core.publisher.Mono;

import java.util.List;

public interface ConfidentialInformationService {

    Mono<Void> updateNotificationAddresses(String iun, Boolean normalized, List<NotificationRecipientAddressesDtoInt> list);
}
