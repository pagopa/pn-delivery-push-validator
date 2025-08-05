package it.pagopa.pn.deliverypushvalidator.dto.address;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PhysicalAddressIntTest {

    @Test
    void builderCreaOggettoConCampiAttesi() {
        PhysicalAddressInt address = PhysicalAddressInt.builder()
                .fullname("Mario Rossi")
                .at("c/o Bianchi")
                .address("Via Roma 1")
                .addressDetails("Scala A")
                .zip("00100")
                .municipality("Roma")
                .municipalityDetails("Centro")
                .province("RM")
                .foreignState("Italia")
                .build();

        assertThat(address.getFullname()).isEqualTo("Mario Rossi");
        assertThat(address.getAt()).isEqualTo("c/o Bianchi");
        assertThat(address.getAddress()).isEqualTo("Via Roma 1");
        assertThat(address.getAddressDetails()).isEqualTo("Scala A");
        assertThat(address.getZip()).isEqualTo("00100");
        assertThat(address.getMunicipality()).isEqualTo("Roma");
        assertThat(address.getMunicipalityDetails()).isEqualTo("Centro");
        assertThat(address.getProvince()).isEqualTo("RM");
        assertThat(address.getForeignState()).isEqualTo("Italia");
    }

    @Test
    void setterModificaCampi() {
        PhysicalAddressInt address = new PhysicalAddressInt();
        address.setFullname("Luigi Verdi");
        address.setZip("12345");
        assertThat(address.getFullname()).isEqualTo("Luigi Verdi");
        assertThat(address.getZip()).isEqualTo("12345");
    }

    @Test
    void enumAnalogTypeContieneValoriAttesi() {
        assertThat(PhysicalAddressInt.ANALOG_TYPE.valueOf("REGISTERED_LETTER_890")).isNotNull();
        assertThat(PhysicalAddressInt.ANALOG_TYPE.valueOf("SIMPLE_REGISTERED_LETTER")).isNotNull();
        assertThat(PhysicalAddressInt.ANALOG_TYPE.valueOf("AR_REGISTERED_LETTER")).isNotNull();
    }
}
