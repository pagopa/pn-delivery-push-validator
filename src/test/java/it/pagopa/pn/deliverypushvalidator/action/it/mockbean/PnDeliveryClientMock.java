package it.pagopa.pn.deliverypushvalidator.action.it.mockbean;


import it.pagopa.pn.deliverypushvalidator.dto.ext.delivery.notification.NotificationInt;
import it.pagopa.pn.deliverypushvalidator.generated.openapi.msclient.datavault_reactive.model.AnalogDomicile;
import it.pagopa.pn.deliverypushvalidator.generated.openapi.msclient.datavault_reactive.model.NotificationRecipientAddressesDto;
import it.pagopa.pn.deliverypushvalidator.generated.openapi.msclient.delivery.model.NotificationPhysicalAddress;
import it.pagopa.pn.deliverypushvalidator.generated.openapi.msclient.delivery.model.NotificationRecipientV24;
import it.pagopa.pn.deliverypushvalidator.generated.openapi.msclient.delivery.model.InformalNotificationRecipientV1;
import it.pagopa.pn.deliverypushvalidator.generated.openapi.msclient.delivery.model.InformalSentNotificationV1;
import it.pagopa.pn.deliverypushvalidator.generated.openapi.msclient.delivery.model.InformalNotificationPaymentItem;
import it.pagopa.pn.deliverypushvalidator.generated.openapi.msclient.delivery.model.PagoPaPaymentBase;
import it.pagopa.pn.deliverypushvalidator.generated.openapi.msclient.delivery.model.NotificationPaymentItem;
import it.pagopa.pn.deliverypushvalidator.generated.openapi.msclient.delivery.model.SentNotificationV25;
import it.pagopa.pn.deliverypushvalidator.middleware.externalclient.pnclient.delivery.PnDeliveryClient;
import it.pagopa.pn.deliverypushvalidator.service.mapper.NotificationMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

@Slf4j
public class PnDeliveryClientMock implements PnDeliveryClient {
    private CopyOnWriteArrayList<SentNotificationV25> notifications;

    private final PnDataVaultClientReactiveMock pnDataVaultClientReactiveMock;

    public SentNotificationV25 getNotification(String iun) {
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
        SentNotificationV25 sentNotification = NotificationMapper.internalToExternal(notification);
        this.notifications.add(sentNotification);
        log.info("ADDED_IUN:" + notification.getIun());
    }

    @Override
    public SentNotificationV25 getSentNotification(String iun) {
        Optional<SentNotificationV25> sentNotificationOpt = notifications.stream().filter(notification -> iun.equals(notification.getIun())).findFirst();
        if(sentNotificationOpt.isPresent()){
            SentNotificationV25 sentNotification = sentNotificationOpt.get();
            List<NotificationRecipientV24> listRecipient = sentNotification.getRecipients();
            
            int recIndex = 0;
            for (NotificationRecipientV24 recipient : listRecipient){
                
                NotificationRecipientAddressesDto recipientAddressesDto = pnDataVaultClientReactiveMock.getAddressFromRecipientIndex(iun, recIndex);
                
                if(recipientAddressesDto != null){
                    final AnalogDomicile normalizedAddress = recipientAddressesDto.getPhysicalAddress();

                    if(normalizedAddress != null){
                        NotificationPhysicalAddress physicalAddress = new NotificationPhysicalAddress()
                                .address(normalizedAddress.getAddress())
                                .addressDetails(normalizedAddress.getAddressDetails())
                                .zip(normalizedAddress.getCap())
                                .at(normalizedAddress.getAt())
                                .municipality(normalizedAddress.getMunicipality())
                                .foreignState(normalizedAddress.getState())
                                .municipalityDetails(normalizedAddress.getMunicipalityDetails())
                                .province(normalizedAddress.getProvince());

                        recipient.setPhysicalAddress(physicalAddress);
                    }
                }
                
                recIndex ++;
            }

            return sentNotificationOpt.get();
        }
        throw new RuntimeException("Test error, iun is not presente in getSentNotification IUN:" + iun);
    }

    @Override
    public InformalSentNotificationV1 getSentInformalNotification(String iun) {
        SentNotificationV25 sentNotification = getSentNotification(iun);

        InformalSentNotificationV1 informal = new InformalSentNotificationV1();
        informal.setIun(sentNotification.getIun());
        informal.setPaProtocolNumber(sentNotification.getPaProtocolNumber());
        informal.setSubject(sentNotification.getSubject());
        informal.setSentAt(sentNotification.getSentAt());
        informal.setSenderPaId(sentNotification.getSenderPaId());
        informal.setSenderTaxId(sentNotification.getSenderTaxId());
        informal.setSenderDenomination(sentNotification.getSenderDenomination());
        informal.setAdditionalLanguages(sentNotification.getAdditionalLanguages());
        informal.setGroup(sentNotification.getGroup());
        informal.setVersion(sentNotification.getVersion());
        informal.setIdempotenceToken(sentNotification.getIdempotenceToken());
        informal.setUsedServices(sentNotification.getUsedServices());
        informal.setDocuments(sentNotification.getDocuments());

        List<InformalNotificationRecipientV1> recipients = sentNotification.getRecipients().stream().map(recipient -> {
            InformalNotificationRecipientV1 informalRecipient = new InformalNotificationRecipientV1();
            informalRecipient.setTaxId(recipient.getTaxId());
            informalRecipient.setInternalId(recipient.getInternalId());
            informalRecipient.setDenomination(recipient.getDenomination());
            informalRecipient.setDigitalDomicile(recipient.getDigitalDomicile());
            informalRecipient.setPhysicalAddress(recipient.getPhysicalAddress());
            informalRecipient.setRecipientType(InformalNotificationRecipientV1.RecipientTypeEnum.valueOf(recipient.getRecipientType().name()));

            List<InformalNotificationPaymentItem> informalPayments = recipient.getPayments() == null ? null :
                    recipient.getPayments().stream().map(this::toInformalPayment).toList();
            informalRecipient.setPayments(informalPayments);
            return informalRecipient;
        }).toList();

        informal.setRecipients(recipients);
        return informal;
    }

    private InformalNotificationPaymentItem toInformalPayment(NotificationPaymentItem item) {
        InformalNotificationPaymentItem informalItem = new InformalNotificationPaymentItem();
        if (item != null && item.getPagoPa() != null) {
            PagoPaPaymentBase pagoPa = new PagoPaPaymentBase();
            pagoPa.setNoticeCode(item.getPagoPa().getNoticeCode());
            pagoPa.setCreditorTaxId(item.getPagoPa().getCreditorTaxId());
            pagoPa.setAttachment(item.getPagoPa().getAttachment());
            informalItem.setPagoPa(pagoPa);
        }
        return informalItem;
    }
}
