package it.pagopa.pn.deliverypushvalidator.dto.timeline;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CommunicationTypeTest {

    @Test
    void fromValueShouldReturnLEGALWhenValueIsNull() {
        assertEquals(CommunicationType.LEGAL, CommunicationType.fromValue(null));
    }

    @Test
    void fromValueShouldReturnLEGALWhenValueIsLEGAL() {
        assertEquals(CommunicationType.LEGAL, CommunicationType.fromValue("LEGAL"));
        assertEquals(CommunicationType.LEGAL, CommunicationType.fromValue("legal"));
        assertEquals(CommunicationType.LEGAL, CommunicationType.fromValue("LeGaL"));
    }

    @Test
    void fromValueShouldReturnINFORMALWhenValueIsINFORMAL() {
        assertEquals(CommunicationType.INFORMAL, CommunicationType.fromValue("INFORMAL"));
        assertEquals(CommunicationType.INFORMAL, CommunicationType.fromValue("informal"));
        assertEquals(CommunicationType.INFORMAL, CommunicationType.fromValue("InFoRmAl"));
    }

    @Test
    void fromValueShouldThrowExceptionForInvalidValue() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> CommunicationType.fromValue("UNKNOWN"));
        assertTrue(ex.getMessage().contains("Valore non valido"));
    }
}