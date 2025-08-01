package it.pagopa.pn.deliverypushvalidator.action.utils;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;


class InstantNowSupplierTest {

    @Test
    void testGetReturnsCurrentInstant() {
        InstantNowSupplier supplier = new InstantNowSupplier();
        Instant before = Instant.now();
        Instant result = supplier.get();
        Instant after = Instant.now();

        assertNotNull(result);
        assertFalse(result.isBefore(before));
        assertFalse(result.isAfter(after));
    }
}
