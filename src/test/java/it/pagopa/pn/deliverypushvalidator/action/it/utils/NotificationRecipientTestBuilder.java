package it.pagopa.pn.deliverypushvalidator.action.it.utils;

import it.pagopa.pn.deliverypushvalidator.dto.address.LegalDigitalAddressInt;
import it.pagopa.pn.deliverypushvalidator.dto.address.PhysicalAddressInt;
import it.pagopa.pn.deliverypushvalidator.dto.ext.datavault.RecipientTypeInt;
import it.pagopa.pn.deliverypushvalidator.dto.ext.delivery.notification.NotificationPaymentInfoInt;
import it.pagopa.pn.deliverypushvalidator.dto.ext.delivery.notification.NotificationRecipientInt;

import java.util.List;
import java.util.UUID;


public class NotificationRecipientTestBuilder {
    private String taxId;
    private RecipientTypeInt recipientType;
    private PhysicalAddressInt physicalAddress;
    private String internalId;
    private LegalDigitalAddressInt digitalDomicile;
    private List<NotificationPaymentInfoInt> payments;
    private String denomination;
    private String messageId;
    private String email;

    public static NotificationRecipientTestBuilder builder() {
        return new NotificationRecipientTestBuilder();
    }

    public NotificationRecipientTestBuilder withTaxId(String taxId) {
        this.taxId = taxId;
        return this;
    }

    public NotificationRecipientTestBuilder withRecipientType(RecipientTypeInt recipientType) {
        this.recipientType = recipientType;
        return this;
    }

    public NotificationRecipientTestBuilder withPhysicalAddress(PhysicalAddressInt physicalAddress) {
        this.physicalAddress = physicalAddress;
        return this;
    }

    public NotificationRecipientTestBuilder withInternalId(String internalId) {
        this.internalId = internalId;
        return this;
    }

    public NotificationRecipientTestBuilder withDigitalDomicile(LegalDigitalAddressInt digitalDomicile) {
        this.digitalDomicile = digitalDomicile;
        return this;
    }



    public NotificationRecipientTestBuilder withPayments(List<NotificationPaymentInfoInt> payments) {
        this.payments = payments;
        return this;
    }

    public NotificationRecipientTestBuilder withDenomination(String denomination) {
        this.denomination = denomination;
        return this;
    }

    public NotificationRecipientTestBuilder withMessageId(String messageId) {
        this.messageId = messageId;
        return this;
    }

    public NotificationRecipientTestBuilder withEmail(String email) {
        this.email = email;
        return this;
    }
    
    public NotificationRecipientInt build() {
        if(taxId == null){
            taxId = "GeneratedTaxId_" +UUID.randomUUID();
        }
        
        if(internalId == null){
            internalId = "ANON_"+taxId;
        }

        if(physicalAddress == null){
            physicalAddress = PhysicalAddressInt.builder()
                    .address("Test.address")
                    .at("Test.at")
                    .zip("Test.zip")
                    .foreignState("Test.foreignState")
                    .municipality("Test.municipality")
                    .addressDetails("Test.addressDetails")
                    .municipalityDetails("Test.municipalityDetails")
                    .province("Test.province")
                    .foreignState("Test.foreignState")
                    .build();
        }
        
        if (denomination == null) {
            denomination = "Name_and_surname_of_" + taxId;
        }

        if(physicalAddress != null){
            physicalAddress.setFullname(denomination);
        }

        if(recipientType == null) {
            recipientType = RecipientTypeInt.PF;
        }
        
        return NotificationRecipientInt.builder()
                .recipientType(recipientType)
                .taxId(taxId)
                .internalId(internalId)
                .denomination(denomination)
                .physicalAddress(physicalAddress)
                .digitalDomicile(digitalDomicile)
                .payments(payments)
                .messageId(messageId)
                .email(email)
                .build();
    }

}
