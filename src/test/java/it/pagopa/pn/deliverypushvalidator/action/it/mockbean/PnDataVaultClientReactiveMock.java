package it.pagopa.pn.deliverypushvalidator.action.it.mockbean;

import it.pagopa.pn.deliverypushvalidator.dto.address.PhysicalAddressInt;
import it.pagopa.pn.deliverypushvalidator.dto.ext.delivery.notification.NotificationInt;
import it.pagopa.pn.deliverypushvalidator.exception.PnMessageNotFoundException;
import it.pagopa.pn.deliverypushvalidator.generated.openapi.msclient.datavault_reactive.model.*;
import it.pagopa.pn.deliverypushvalidator.middleware.externalclient.pnclient.datavault.PnDataVaultClientReactive;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Slf4j
public class PnDataVaultClientReactiveMock implements PnDataVaultClientReactive {
    private ConcurrentMap<String, BaseRecipientDto> confidentialMap;
    private ConcurrentMap<String, NotificationRecipientAddressesDto> normalizedAddress;
    @Setter
    private PnDeliveryClientMock pnDeliveryClientMock;

    public void clear() {
        this.confidentialMap = new ConcurrentHashMap<>();
        this.normalizedAddress = new ConcurrentHashMap<>();
        this.messageMap = new ConcurrentHashMap<>();
    }
    
    public void insertBaseRecipientDto(BaseRecipientDto dto){
        confidentialMap.put(dto.getInternalId(), dto);
    }

    @Override
    public Flux<BaseRecipientDto> getRecipientsDenominationByInternalId(List<String> listInternalId) {
        return Flux.fromStream(listInternalId.stream()
                .filter( internalId -> confidentialMap.get(internalId) != null)
                .map(internalId -> confidentialMap.get(internalId)));
    }

    @Override
    public Flux<ConfidentialTimelineElementDto> getNotificationTimelines(List<ConfidentialTimelineElementId> confidentialTimelineElementId) {
        return null;
    }

    @Override
    public Mono<Void> updateNotificationAddressesByIun(String iun, Boolean normalized, List<NotificationRecipientAddressesDto> list) {
        if (normalized) {
            return Mono.fromRunnable( () -> {
                int recIndex = 0;
                for (NotificationRecipientAddressesDto recNormAddress : list ){
                    String key = getKey(iun, recIndex);
                    normalizedAddress.put(key, recNormAddress);
                    log.info("[TEST] normalized address insert is {}", recNormAddress);
                    recIndex ++;
                }
            }).flatMap( res-> Mono.empty());
        } else { // Used in the context of physical address lookup feature
            NotificationInt notificationInt = pnDeliveryClientMock.getNotification(iun);

            for (NotificationRecipientAddressesDto recAddress : list){
                log.info("[TEST] addresses to insert in notification are {}", recAddress);
                assert recAddress.getPhysicalAddress() != null;
                notificationInt.getRecipients().get(recAddress.getRecIndex())
                        .setPhysicalAddress(mapToNotificationPhysicalAddress(recAddress.getPhysicalAddress()));
            }
            return Mono.empty();
        }
    }

    private PhysicalAddressInt mapToNotificationPhysicalAddress(AnalogDomicile dto) {
        PhysicalAddressInt notificationPhysicalAddress = new PhysicalAddressInt();
        notificationPhysicalAddress.setAt(dto.getAt());
        notificationPhysicalAddress.setAddress(dto.getAddress());
        notificationPhysicalAddress.setProvince(dto.getProvince());
        notificationPhysicalAddress.setMunicipality(dto.getMunicipality());
        return notificationPhysicalAddress;
    }

    private static String getMessageKey(UUID messageId, UUID senderId) {
        return messageId + "_" + senderId;
    }

    private ConcurrentMap<String, MessageResponseDto> messageMap = new ConcurrentHashMap<>();

    public void insertMessage(MessageResponseDto dto) {
        messageMap.put(getMessageKey(dto.getMessageId(), UUID.fromString(dto.getSenderId())), dto);
    }

    @Override
    public Mono<MessageResponseDto> getMessageById(UUID messageId, UUID senderId) {
        MessageResponseDto dto = messageMap.get(getMessageKey(messageId, senderId));
        if (dto != null) {
            return Mono.just(dto);
        }
        return Mono.error(new PnMessageNotFoundException(
                "Message not found for messageId=" + messageId + ", senderId=" + senderId
        ));
    }



    @NotNull
    private static String getKey(String iun, int recIndex) {
        return iun + "_" +recIndex;
    }

    public NotificationRecipientAddressesDto getAddressFromRecipientIndex(String iun, int rexIndex){
        String key = getKey(iun, rexIndex);
        return normalizedAddress.get(key);
    }
}