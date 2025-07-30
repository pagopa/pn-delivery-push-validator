package it.pagopa.pn.deliverypushvalidator.dto.ext.paperchannel;

import lombok.Getter;

@Getter
public enum ResultFilterEnum {

    SUCCESS("SUCCESS"),

    DISCARD("DISCARD"),

    NEXT("NEXT");

    private final String value;

    ResultFilterEnum(String value) {
        this.value = value;
    }

}
