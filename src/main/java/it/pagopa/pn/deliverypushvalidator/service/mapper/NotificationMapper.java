package it.pagopa.pn.deliverypushvalidator.service.mapper;

import it.pagopa.pn.commons.utils.DateFormatUtils;

import it.pagopa.pn.deliverypushvalidator.dto.address.LegalDigitalAddressInt;
import it.pagopa.pn.deliverypushvalidator.dto.address.PhysicalAddressInt;
import it.pagopa.pn.deliverypushvalidator.dto.ext.datavault.RecipientTypeInt;
import it.pagopa.pn.deliverypushvalidator.dto.ext.delivery.notification.*;
import it.pagopa.pn.deliverypushvalidator.dto.ext.delivery.notification.CommunicationType;
import it.pagopa.pn.deliverypushvalidator.generated.openapi.msclient.delivery.model.*;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class NotificationMapper {
    private NotificationMapper(){}

    public static NotificationInt externalToInternal(SentNotificationV25 sentNotification) {

        List<NotificationRecipientInt> listNotificationRecipientInt = mapNotificationRecipient(sentNotification.getRecipients());
        List<NotificationDocumentInt> listNotificationDocumentInt = mapNotificationDocument(sentNotification.getDocuments());

        ServiceLevelTypeInt lvl =  ServiceLevelTypeInt.valueOf( sentNotification.getPhysicalCommunicationType().name());
        
        Instant paymentExpirationDate = null;
        if( sentNotification.getPaymentExpirationDate() != null ){
            ZonedDateTime dateTime = DateFormatUtils.parseDate(sentNotification.getPaymentExpirationDate());
            paymentExpirationDate = dateTime.toInstant();
        }
        
        return NotificationInt.builder()
                .iun(sentNotification.getIun())
                .subject(sentNotification.getSubject())
                .paProtocolNumber(sentNotification.getPaProtocolNumber())
                .physicalCommunicationType( lvl )
                .sentAt(sentNotification.getSentAt())
                .sender(
                        NotificationSenderInt.builder()
                                .paTaxId( sentNotification.getSenderTaxId() )
                                .paId(sentNotification.getSenderPaId())
                                .paDenomination(sentNotification.getSenderDenomination())
                                .build()
                )
                .paFee(sentNotification.getPaFee())
                .vat(sentNotification.getVat())
                .documents(listNotificationDocumentInt)
                .recipients(listNotificationRecipientInt)
                .notificationFeePolicy(NotificationFeePolicy.fromValue(sentNotification.getNotificationFeePolicy().getValue()))
                .amount(sentNotification.getAmount())
                .group(sentNotification.getGroup())
                .paymentExpirationDate(paymentExpirationDate)
                .pagoPaIntMode(sentNotification.getPagoPaIntMode() != null ? PagoPaIntMode.valueOf(sentNotification.getPagoPaIntMode().getValue()) : null)
                .version(sentNotification.getVersion())
                .additionalLanguages(sentNotification.getAdditionalLanguages())
                .usedServices(UsedServicesMapper.externalToInternal(sentNotification.getUsedServices()))
                .idempotenceToken(sentNotification.getIdempotenceToken())
                .communicationType(CommunicationType.LEGAL)
                .build();
    }

    public static NotificationInt externalToInternal(InformalSentNotificationV1 sentInformalNotification) {
        List<NotificationRecipientInt> listNotificationRecipientInt = mapNotificationRecipientInformal(sentInformalNotification.getRecipients());
        List<NotificationDocumentInt> listNotificationDocumentIntInt = mapNotificationDocument(sentInformalNotification.getDocuments());

        return NotificationInt.builder()
                .iun(sentInformalNotification.getIun())
                .subject(sentInformalNotification.getSubject())
                .paProtocolNumber(sentInformalNotification.getPaProtocolNumber())
                .sentAt(sentInformalNotification.getSentAt())
                .sender(
                        NotificationSenderInt.builder()
                                .paTaxId(sentInformalNotification.getSenderTaxId())
                                .paId(sentInformalNotification.getSenderPaId())
                                .paDenomination(sentInformalNotification.getSenderDenomination())
                                .build()
                )
                .documents(listNotificationDocumentIntInt)
                .recipients(listNotificationRecipientInt)
                .group(sentInformalNotification.getGroup())
                .version(sentInformalNotification.getVersion())
                .additionalLanguages(sentInformalNotification.getAdditionalLanguages())
                .usedServices(UsedServicesMapper.externalToInternal(sentInformalNotification.getUsedServices()))
                .idempotenceToken(sentInformalNotification.getIdempotenceToken())
                .communicationType(CommunicationType.INFORMAL)
                .build();
    }

    private static List<NotificationDocumentInt> mapNotificationDocument(List<NotificationDocument> documents) {
        List<NotificationDocumentInt> list = new ArrayList<>();

        if (documents == null) {
            return list;
        }

        for (NotificationDocument document : documents){
            NotificationDocumentInt notificationDocumentInt = NotificationDocumentInt.builder()
                    .digests(
                            NotificationDocumentInt.Digests.builder()
                                    .sha256(document.getDigests().getSha256())
                                    .build()
                    )
                    .ref(
                            NotificationDocumentInt.Ref.builder()
                                    .key(document.getRef().getKey())
                                    .versionToken(document.getRef().getVersionToken())
                                    .build()
                    )
                    .build();

            list.add(notificationDocumentInt);
        }

        return list;
    }

    private static List<NotificationRecipientInt> mapNotificationRecipient(List<NotificationRecipientV24> recipients) {
        List<NotificationRecipientInt> list = new ArrayList<>();

        if (recipients == null) {
            return list;
        }

        for (NotificationRecipientV24 recipient : recipients){
            NotificationRecipientInt recipientInt = RecipientMapper.externalToInternal(recipient);
            list.add(recipientInt);
        }
        
        return list;
    }

    private static List<NotificationRecipientInt> mapNotificationRecipientInformal(List<InformalNotificationRecipientV1> recipients) {
        List<NotificationRecipientInt> list = new ArrayList<>();

        if (recipients == null) {
            return list;
        }

        for (InformalNotificationRecipientV1 recipient : recipients) {
            NotificationRecipientInt.NotificationRecipientIntBuilder recipientIntBuilder = NotificationRecipientInt.builder()
                    .taxId(recipient.getTaxId())
                    .internalId(recipient.getInternalId())
                    .denomination(recipient.getDenomination())
                    .recipientType(RecipientTypeInt.valueOf(recipient.getRecipientType().name()));

            NotificationDigitalAddress digitalDomicile = recipient.getDigitalDomicile();
            if (digitalDomicile != null) {
                recipientIntBuilder.digitalDomicile(
                        LegalDigitalAddressInt.builder()
                                .address(digitalDomicile.getAddress())
                                .type(LegalDigitalAddressInt.LEGAL_DIGITAL_ADDRESS_TYPE.valueOf(digitalDomicile.getType().name()))
                                .build());
            }

            NotificationPhysicalAddress physicalAddress = recipient.getPhysicalAddress();
            if (physicalAddress != null) {
                recipientIntBuilder.physicalAddress(
                        PhysicalAddressInt.builder()
                                .fullname(recipient.getDenomination())
                                .at(physicalAddress.getAt())
                                .address(physicalAddress.getAddress())
                                .addressDetails(physicalAddress.getAddressDetails())
                                .foreignState(physicalAddress.getForeignState())
                                .municipality(physicalAddress.getMunicipality())
                                .municipalityDetails(physicalAddress.getMunicipalityDetails())
                                .province(physicalAddress.getProvince())
                                .zip(physicalAddress.getZip())
                                .build());
            }

            recipientIntBuilder.payments(mapNotificationPaymentInfo(recipient.getPayments()));
            list.add(recipientIntBuilder.build());
        }

        return list;
    }

    private static List<NotificationPaymentInfoInt> mapNotificationPaymentInfo(List<InformalNotificationPaymentItem> payments) {
        List<NotificationPaymentInfoInt> list = new ArrayList<>();

        if (payments == null) {
            return list;
        }

        for (InformalNotificationPaymentItem payment : payments) {
            PagoPaPaymentBase pagoPa = payment.getPagoPa();
            list.add(
                    NotificationPaymentInfoInt.builder()
                            .pagoPA(PagoPaInt.builder()
                                    .creditorTaxId(pagoPa.getCreditorTaxId())
                                    .noticeCode(pagoPa.getNoticeCode())
                                    .attachment(pagoPa.getAttachment() != null ? NotificationDocumentInt.builder()
                                            .ref(NotificationDocumentInt.Ref.builder()
                                                    .key(pagoPa.getAttachment().getRef().getKey())
                                                    .versionToken(pagoPa.getAttachment().getRef().getVersionToken())
                                                    .build())
                                            .digests(NotificationDocumentInt.Digests.builder()
                                                    .sha256(pagoPa.getAttachment().getDigests().getSha256())
                                                    .build())
                                            .build() : null)
                                    .build())
                            .build());
        }

        return list;
    }
    
    //Utilizzata a livello di test
    public static SentNotificationV25 internalToExternal(NotificationInt notification) {
        SentNotificationV25 sentNotification = new SentNotificationV25();

        sentNotification.setIun(notification.getIun());
        sentNotification.setPaProtocolNumber(notification.getPaProtocolNumber());
        sentNotification.setSentAt(notification.getSentAt());
        sentNotification.setSubject(notification.getSubject());
        sentNotification.setAmount(notification.getAmount());
        sentNotification.setPaFee(notification.getPaFee());
        sentNotification.setVat(notification.getVat());
        sentNotification.setAdditionalLanguages(notification.getAdditionalLanguages());
        sentNotification.setUsedServices(mapToUserSevicesInt(notification.getUsedServices()));

        ZonedDateTime time = DateFormatUtils.parseInstantToZonedDateTime(notification.getPaymentExpirationDate());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String formattedString = time.format(formatter);
        sentNotification.setPaymentExpirationDate(formattedString);
        
        if(notification.getPagoPaIntMode() != null){
            sentNotification.setPagoPaIntMode(SentNotificationV25.PagoPaIntModeEnum.valueOf(notification.getPagoPaIntMode().getValue()));
        }
        if( notification.getPhysicalCommunicationType() != null ) {
            sentNotification.setPhysicalCommunicationType(
                    SentNotificationV25.PhysicalCommunicationTypeEnum.valueOf( notification.getPhysicalCommunicationType().name() )
            );
        }

        NotificationSenderInt sender = notification.getSender();
        if( sender != null ) {
            sentNotification.setSenderDenomination( sender.getPaDenomination() );
            sentNotification.setSenderPaId( sender.getPaId() );
            sentNotification.setSenderTaxId( sender.getPaTaxId() );
        }

        List<NotificationRecipientV24> recipients = notification.getRecipients().stream()
                .map(RecipientMapper::internalToExternal).toList();

        sentNotification.setRecipients(recipients);

        List<NotificationDocument> documents = notification.getDocuments().stream().map(
                NotificationMapper::getNotificationDocument).toList();

        sentNotification.setDocuments(documents);

        if(notification.getPhysicalCommunicationType() != null){
            sentNotification.setPhysicalCommunicationType(SentNotificationV25.PhysicalCommunicationTypeEnum.valueOf(notification.getPhysicalCommunicationType().name()));
        }
        
        if(notification.getSender() != null){
            sentNotification.setSenderPaId(notification.getSender().getPaId());
            sentNotification.setSenderDenomination(notification.getSender().getPaDenomination());
            sentNotification.setSenderTaxId(notification.getSender().getPaTaxId());
        }

        if(notification.getNotificationFeePolicy() != null){
            sentNotification.setNotificationFeePolicy(NotificationFeePolicy.fromValue(notification.getNotificationFeePolicy().getValue()));
        }

        sentNotification.setVersion(notification.getVersion());
        
        return sentNotification;
    }

    @NotNull
    private static NotificationDocument getNotificationDocument(NotificationDocumentInt documentInt) {
        NotificationAttachmentDigests digests = new NotificationAttachmentDigests();
        digests.setSha256(documentInt.getDigests().getSha256());

        NotificationAttachmentBodyRef ref = new NotificationAttachmentBodyRef();
        ref.setKey(documentInt.getRef().getKey());
        ref.setVersionToken(documentInt.getRef().getVersionToken());

        NotificationDocument document = new NotificationDocument();
        document.setDigests(digests);
        document.setRef(ref);
        return document;
    }

    private static UsedServices mapToUserSevicesInt(UsedServicesInt usedServicesInt) {
        if (usedServicesInt == null) {
            return null;
        }
        UsedServices usedServices = new UsedServices();
        usedServices.physicalAddressLookup(usedServicesInt.getPhysicalAddressLookUp());
        return usedServices;
    }

}
