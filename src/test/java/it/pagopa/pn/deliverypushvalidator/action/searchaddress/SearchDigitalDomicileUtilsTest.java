package it.pagopa.pn.deliverypushvalidator.action.searchaddress;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SearchDigitalDomicileUtilsTest {

    private SearchDigitalDomicileParameterConsumer consumer;
    private SearchDigitalDomicileUtils utils;

    @BeforeEach
    void setUp() {
        consumer = org.mockito.Mockito.mock(SearchDigitalDomicileParameterConsumer.class);
        utils = new SearchDigitalDomicileUtils(consumer);
    }

    @Test
    void isPecFullSearchEnabled_returnsFalseWhenNoConfigs() {
        org.mockito.Mockito.when(consumer.getSearchDigitalDomicileConfigs()).thenReturn(List.of());

        assertFalse(utils.isPecFullSearchEnabled(Instant.parse("2024-01-02T00:00:00Z")));
    }

    @Test
    void isPecFullSearchEnabled_returnsTrueForMatchingFullSearchConfig() {
        SearchDigitalDomicileConfig config = new SearchDigitalDomicileConfig(
                Instant.parse("2024-01-01T00:00:00Z"),
                List.of(DigitalAddressSourceInt.GENERAL, DigitalAddressSourceInt.PLATFORM, DigitalAddressSourceInt.SPECIAL),
                List.of(),
                List.of()
        );
        org.mockito.Mockito.when(consumer.getSearchDigitalDomicileConfigs()).thenReturn(List.of(config));

        assertTrue(utils.isPecFullSearchEnabled(Instant.parse("2024-01-02T00:00:00Z")));
    }

    @Test
    void isPecFullSearchEnabled_returnsTrueAtExactValidFromBoundary() {
        SearchDigitalDomicileConfig config = new SearchDigitalDomicileConfig(
                Instant.parse("2024-01-01T00:00:00Z"),
                List.of(DigitalAddressSourceInt.GENERAL, DigitalAddressSourceInt.PLATFORM, DigitalAddressSourceInt.SPECIAL),
                List.of(),
                List.of()
        );
        org.mockito.Mockito.when(consumer.getSearchDigitalDomicileConfigs()).thenReturn(List.of(config));

        assertTrue(utils.isPecFullSearchEnabled(Instant.parse("2024-01-01T00:00:00Z")));
    }

    @Test
    void isPecFullSearchEnabled_returnsFalseWhenPecSourcesAreIncomplete() {
        SearchDigitalDomicileConfig config = new SearchDigitalDomicileConfig(
                Instant.parse("2024-01-01T00:00:00Z"),
                List.of(DigitalAddressSourceInt.GENERAL),
                List.of(),
                List.of()
        );
        org.mockito.Mockito.when(consumer.getSearchDigitalDomicileConfigs()).thenReturn(List.of(config));

        assertFalse(utils.isPecFullSearchEnabled(Instant.parse("2024-01-02T00:00:00Z")));
    }

    @Test
    void isPecFullSearchEnabled_returnsFalseWhenNoConfigMatchesSentAt() {
        SearchDigitalDomicileConfig config = new SearchDigitalDomicileConfig(
                Instant.parse("2024-01-03T00:00:00Z"),
                List.of(DigitalAddressSourceInt.GENERAL, DigitalAddressSourceInt.PLATFORM, DigitalAddressSourceInt.SPECIAL),
                List.of(),
                List.of()
        );
        org.mockito.Mockito.when(consumer.getSearchDigitalDomicileConfigs()).thenReturn(List.of(config));

        assertFalse(utils.isPecFullSearchEnabled(Instant.parse("2024-01-02T00:00:00Z")));
    }
}
