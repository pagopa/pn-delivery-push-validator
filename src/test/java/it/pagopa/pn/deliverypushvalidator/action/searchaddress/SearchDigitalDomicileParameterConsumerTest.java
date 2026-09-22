package it.pagopa.pn.deliverypushvalidator.action.searchaddress;

import it.pagopa.pn.commons.abstractions.ParameterConsumer;
import it.pagopa.pn.commons.exceptions.PnInternalException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SearchDigitalDomicileParameterConsumerTest {

    @Mock
    private ParameterConsumer parameterConsumer;

    private SearchDigitalDomicileParameterConsumer consumer;

    @BeforeEach
    void setUp() {
        consumer = new SearchDigitalDomicileParameterConsumer(parameterConsumer);
    }

    @Test
    void initialize_loadsValidConfigs() {
        SearchDigitalDomicileConfig config = new SearchDigitalDomicileConfig(
                Instant.parse("2024-01-01T00:00:00Z"),
                List.of(DigitalAddressSourceInt.GENERAL),
                List.of(DigitalAddressSourceInt.PLATFORM),
                List.of(DigitalAddressSourceInt.SPECIAL)
        );
        when(parameterConsumer.getParameterValue(
                "/config/workflow/search-digital-domicile",
                SearchDigitalDomicileConfig[].class
        )).thenReturn(Optional.of(new SearchDigitalDomicileConfig[]{config}));

        consumer.initialize();

        assertEquals(1, consumer.getSearchDigitalDomicileConfigs().size());
        assertEquals(config, consumer.getSearchDigitalDomicileConfigs().get(0));
    }

    @Test
    void initialize_ignoresInvalidConfigs() {
        SearchDigitalDomicileConfig invalid = new SearchDigitalDomicileConfig(
                null,
                null,
                null,
                null
        );
        when(parameterConsumer.getParameterValue(
                "/config/workflow/search-digital-domicile",
                SearchDigitalDomicileConfig[].class
        )).thenReturn(Optional.of(new SearchDigitalDomicileConfig[]{invalid}));

        consumer.initialize();

        assertEquals(0, consumer.getSearchDigitalDomicileConfigs().size());
    }

    @Test
    void initialize_propagatesUnexpectedInternalException() {
        when(parameterConsumer.getParameterValue(
                "/config/workflow/search-digital-domicile",
                SearchDigitalDomicileConfig[].class
        )).thenThrow(new PnInternalException("boom", "GENERIC_ERROR"));

        assertThrows(PnInternalException.class, () -> consumer.initialize());
    }
}
