package it.pagopa.pn.deliverypushvalidator.action.searchaddress;

import it.pagopa.pn.commons.abstractions.ParameterConsumer;
import it.pagopa.pn.commons.exceptions.PnInternalException;
import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.CustomLog;
import lombok.Getter;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.ssm.model.ParameterNotFoundException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Component
@AllArgsConstructor
@CustomLog
@Getter
public class SearchDigitalDomicileParameterConsumer {

    private static final String PARAMETER_STORE_SEARCH_DIGITAL_DOMICILE = "/config/workflow/search-digital-domicile";

    private final ParameterConsumer parameterConsumer;
    private List<SearchDigitalDomicileConfig> searchDigitalDomicileConfigs;

    @PostConstruct
    protected void initialize() {
        Optional<SearchDigitalDomicileConfig[]> maybeSearchDigitalDomicileConfigs = loadSearchDigitalDomicileConfigs();

        if (maybeSearchDigitalDomicileConfigs.isEmpty()) {
            log.info("No SearchDigitalDomicile configuration found on parameter store");
            return;
        }

        List<SearchDigitalDomicileConfig> loaded = new ArrayList<>();
        for (SearchDigitalDomicileConfig config : maybeSearchDigitalDomicileConfigs.get()) {
            if (isValid(config)) {
                log.info("Adding SearchDigitalDomicile configuration to in-memory load list validFrom={}, pec={}, sms={}, email={}",
                        config.getValidFrom(), config.getPec(), config.getSms(), config.getEmail());
                loaded.add(config);
            } else {
                log.warn("Invalid SearchDigitalDomicile configuration found: {}", config);
            }
        }
        searchDigitalDomicileConfigs = Collections.unmodifiableList(loaded);

        log.info("Loaded {} searchDigitalDomicileConfigs in memory", searchDigitalDomicileConfigs.size());
    }

    private Optional<SearchDigitalDomicileConfig[]> loadSearchDigitalDomicileConfigs() {
        try {
            return parameterConsumer.getParameterValue(
                    PARAMETER_STORE_SEARCH_DIGITAL_DOMICILE,
                    SearchDigitalDomicileConfig[].class
            );
        } catch (PnInternalException ex) {
            if (hasParameterNotFoundCause(ex)) {
                log.info("SearchDigitalDomicile configuration parameter {} not found on parameter store", PARAMETER_STORE_SEARCH_DIGITAL_DOMICILE);
                return Optional.empty();
            }
            throw ex;
        }
    }

    private boolean isValid(SearchDigitalDomicileConfig config) {
        return config.getValidFrom() != null &&
                config.getEmail() != null &&
                config.getSms() != null &&
                config.getPec() != null;
    }

    private boolean hasParameterNotFoundCause(Throwable throwable) {
        Throwable current = throwable;
        while (current != null) {
            if (current instanceof ParameterNotFoundException) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }

}
