package it.pagopa.pn.deliverypushvalidator.action.it.mockbean;


import it.pagopa.pn.deliverypushvalidator.dto.ext.delivery.notification.NotificationInt;
import it.pagopa.pn.deliverypushvalidator.generated.openapi.msclient.datavault_reactive.model.AnalogDomicile;
import it.pagopa.pn.deliverypushvalidator.generated.openapi.msclient.datavault_reactive.model.NotificationRecipientAddressesDto;
import it.pagopa.pn.deliverypushvalidator.generated.openapi.msclient.delivery.model.*;
import it.pagopa.pn.deliverypushvalidator.middleware.externalclient.pnclient.delivery.PnDeliveryClient;
import it.pagopa.pn.deliverypushvalidator.service.mapper.NotificationMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Slf4j
public class PnDeliveryClientMock implements PnDeliveryClient {
    private CopyOnWriteArrayList<NotificationInt> notifications;

    private final PnDataVaultClientReactiveMock pnDataVaultClientReactiveMock;

    public NotificationInt getNotification(String iun) {
        return this.notifications.stream()
                .filter(notification -> iun.equals(notification.getIun()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Test error, iun is not present in getNotification IUN:" + iun));
    }

    public PnDeliveryClientMock( @Lazy PnDataVaultClientReactiveMock pnDataVaultClientReactiveMock) {
        this.pnDataVaultClientReactiveMock = pnDataVaultClientReactiveMock;
    }

    public void clear() {
        this.notifications = new CopyOnWriteArrayList<>();
    }

    public void addNotification(NotificationInt notification) {
        this.notifications.add(notification);
        log.info("ADDED_IUN:" + notification.getIun());
    }

    @Override
    public SentNotificationV26 getSentNotification(String iun) {
        SentNotificationV26 sentNotification = NotificationMapper.internalToExternal(getNotification(iun));
        List<NotificationRecipientV24> listRecipient = sentNotification.getRecipients();

        int recIndex = 0;
        for (NotificationRecipientV24 recipient : listRecipient){
            NotificationPhysicalAddress physicalAddress = retrieveNormalizedAddress(iun, recIndex);
            if(physicalAddress != null) {
                recipient.setPhysicalAddress(retrieveNormalizedAddress(iun, recIndex));
            }
            recIndex ++;
        }

        return sentNotification;
    }

    @Override
    public InformalSentNotificationV1 getSentInformalNotification(String iun) {
        InformalSentNotificationV1 informalSentNotificationV1 = NotificationMapper.internalToExternalInformal(getNotification(iun));
        List<FullInformalNotificationRecipientV1> listRecipient = informalSentNotificationV1.getRecipients();

        int recIndex = 0;
        for (FullInformalNotificationRecipientV1 recipient : listRecipient){
            NotificationPhysicalAddress physicalAddress = retrieveNormalizedAddress(iun, recIndex);
            if(physicalAddress != null) {
                recipient.setPhysicalAddress(retrieveNormalizedAddress(iun, recIndex));
            }
            recIndex ++;
        }

        return informalSentNotificationV1;
    }

    private NotificationPhysicalAddress retrieveNormalizedAddress(String iun, int recIndex) {
        NotificationPhysicalAddress physicalAddress = null;
        NotificationRecipientAddressesDto recipientAddressesDto = pnDataVaultClientReactiveMock.getAddressFromRecipientIndex(iun, recIndex);

        if(recipientAddressesDto != null){
            final AnalogDomicile normalizedAddress = recipientAddressesDto.getPhysicalAddress();

            if(normalizedAddress != null){
                physicalAddress = new NotificationPhysicalAddress()
                        .address(normalizedAddress.getAddress())
                        .addressDetails(normalizedAddress.getAddressDetails())
                        .zip(normalizedAddress.getCap())
                        .at(normalizedAddress.getAt())
                        .municipality(normalizedAddress.getMunicipality())
                        .foreignState(normalizedAddress.getState())
                        .municipalityDetails(normalizedAddress.getMunicipalityDetails())
                        .province(normalizedAddress.getProvince());
            }
        }
        return physicalAddress;
    }

}
