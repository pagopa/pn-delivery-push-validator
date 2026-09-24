package it.pagopa.pn.deliverypushvalidator.action.searchaddress;

import lombok.AllArgsConstructor;
import lombok.CustomLog;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;

@Component
@AllArgsConstructor
@CustomLog
public class SearchDigitalDomicileUtils {

    private final SearchDigitalDomicileParameterConsumer searchDigitalDomicileParameterConsumer;

    public boolean isPecFullSearchEnabled(Instant sentAt) {
        List<SearchDigitalDomicileConfig> configs = searchDigitalDomicileParameterConsumer.getSearchDigitalDomicileConfigs();

        if (CollectionUtils.isEmpty(configs)) {
            return false;
        } else {
            SearchDigitalDomicileConfig activeSearchDigitalDomicileConfig = configs.stream()
                    .filter(internalConfig -> !sentAt.isBefore(internalConfig.getValidFrom())) // !isBefore per accettare anche il caso in cui sentAt == validFrom
                    .max(Comparator.comparing(SearchDigitalDomicileConfig::getValidFrom))
                    .orElse(null);

            if (activeSearchDigitalDomicileConfig == null) {
                return false;
            }

            boolean fullSearchEnabled = activeSearchDigitalDomicileConfig.getPec().stream().toList().containsAll(List.of(DigitalAddressSourceInt.GENERAL, DigitalAddressSourceInt.PLATFORM, DigitalAddressSourceInt.SPECIAL));

            return fullSearchEnabled;
        }
    }
}
