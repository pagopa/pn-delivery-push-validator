package it.pagopa.pn.deliverypushvalidator.dto.address;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class LegalDigitalAddressIntTest {

    @Test
    void builderCreaOggettoConTipoAtteso() {
        LegalDigitalAddressInt address = LegalDigitalAddressInt.builder()
                .type(LegalDigitalAddressInt.LEGAL_DIGITAL_ADDRESS_TYPE.PEC)
                .build();

        assertThat(address.getType()).isEqualTo(LegalDigitalAddressInt.LEGAL_DIGITAL_ADDRESS_TYPE.PEC);
        assertThat(address.getType().getValue()).isEqualTo("PEC");
        assertThat(address.getType()).hasToString("PEC");
    }

    @Test
    void setterModificaTipo() {
        LegalDigitalAddressInt address = new LegalDigitalAddressInt();
        address.setType(LegalDigitalAddressInt.LEGAL_DIGITAL_ADDRESS_TYPE.SERCQ);
        assertThat(address.getType()).isEqualTo(LegalDigitalAddressInt.LEGAL_DIGITAL_ADDRESS_TYPE.SERCQ);
    }

    @Test
    void enumContieneValoriAttesi() {
        assertThat(LegalDigitalAddressInt.LEGAL_DIGITAL_ADDRESS_TYPE.valueOf("PEC")).isNotNull();
        assertThat(LegalDigitalAddressInt.LEGAL_DIGITAL_ADDRESS_TYPE.valueOf("SERCQ")).isNotNull();
    }
}
