package it.pagopa.pn.deliverypushvalidator.dto.documentcreation;


import it.pagopa.pn.deliverypushvalidator.dto.legalfacts.LegalFactCategoryInt;
import lombok.Getter;

@Getter
public enum DocumentCreationTypeInt {
    SENDER_ACK(LegalFactCategoryInt.SENDER_ACK.getValue()),
    NOTIFICATION_CANCELLED(LegalFactCategoryInt.NOTIFICATION_CANCELLED.getValue());

    private final String value;

    DocumentCreationTypeInt(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}