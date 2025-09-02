package it.pagopa.pn.deliverypushvalidator.dto.documentcreation;


import it.pagopa.pn.deliverypushvalidator.dto.legalfacts.LegalFactCategoryInt;

public enum DocumentCreationTypeInt {
    AAR("AAR"),

    ANALOG_FAILURE_DELIVERY(LegalFactCategoryInt.ANALOG_FAILURE_DELIVERY.getValue()),

    SENDER_ACK(LegalFactCategoryInt.SENDER_ACK.getValue()),

    DIGITAL_DELIVERY(LegalFactCategoryInt.DIGITAL_DELIVERY.getValue()),

    RECIPIENT_ACCESS(LegalFactCategoryInt.RECIPIENT_ACCESS.getValue()),

    NOTIFICATION_CANCELLED(LegalFactCategoryInt.NOTIFICATION_CANCELLED.getValue()),

    ANALOG_DELIVERY_TIMEOUT(LegalFactCategoryInt.ANALOG_DELIVERY_TIMEOUT.getValue());

    private final String value;

    DocumentCreationTypeInt(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}