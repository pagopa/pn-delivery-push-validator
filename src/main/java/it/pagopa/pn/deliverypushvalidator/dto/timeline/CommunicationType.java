package it.pagopa.pn.deliverypushvalidator.dto.timeline;

import lombok.Getter;

@Getter
public enum CommunicationType {
    LEGAL,
    INFORMAL;

    public static CommunicationType fromValue(String value) {
        if (value == null || "LEGAL".equalsIgnoreCase(value)) {
            return LEGAL;
        } else if ("INFORMAL".equalsIgnoreCase(value)) {
            return INFORMAL;
        }
        throw new IllegalArgumentException("Valore non valido: " + value);
    }
}
