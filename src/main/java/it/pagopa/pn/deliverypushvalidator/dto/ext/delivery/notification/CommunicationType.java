package it.pagopa.pn.deliverypushvalidator.dto.ext.delivery.notification;

import lombok.Getter;

@Getter
public enum CommunicationType {
    LEGAL,
    INFORMAL;

    public static CommunicationType fromValue(String value) {
        if (value == null || LEGAL.name().equals(value)) {
            return LEGAL;
        } else if (INFORMAL.name().equals(value)) {
            return INFORMAL;
        }
        throw new IllegalArgumentException("Valore non valido: " + value);
    }
}
