package it.pagopa.pn.deliverypushvalidator.service.impl;


import it.pagopa.pn.deliverypushvalidator.dto.ext.datavault.NotificationRecipientAddressesDtoInt;
import it.pagopa.pn.deliverypushvalidator.generated.openapi.msclient.datavault_reactive.model.NotificationRecipientAddressesDto;
import it.pagopa.pn.deliverypushvalidator.middleware.externalclient.pnclient.datavault.PnDataVaultClientReactive;
import it.pagopa.pn.deliverypushvalidator.service.ConfidentialInformationService;
import it.pagopa.pn.deliverypushvalidator.service.mapper.NotificationRecipientAddressesDtoMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConfidentialInformationServiceImpl implements ConfidentialInformationService {

    private final PnDataVaultClientReactive pnDataVaultClientReactive;


    @Override
    public Mono<Void> updateNotificationAddresses(String iun, Boolean normalized, List<NotificationRecipientAddressesDtoInt> listAddressDtoInt) {
        log.debug("Start updateNotificationAddresses - iun={}", iun);

        List<NotificationRecipientAddressesDto> listAddressExt = listAddressDtoInt.stream().map(
                NotificationRecipientAddressesDtoMapper::internalToExternal
        ).toList();

        return pnDataVaultClientReactive.updateNotificationAddressesByIun(iun, normalized, listAddressExt);
    }
}
