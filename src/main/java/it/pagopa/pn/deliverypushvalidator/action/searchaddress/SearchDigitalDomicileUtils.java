package it.pagopa.pn.deliverypushvalidator.action.searchaddress;

import lombok.AllArgsConstructor;
import lombok.CustomLog;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.time.Instant;
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
            SearchDigitalDomicileConfig config = configs.stream().filter(internalConfig -> !sentAt.isBefore(internalConfig.getValidFrom()))
                    .max((internalConfig1, internalConfig2) -> internalConfig1.getValidFrom().compareTo(internalConfig2.getValidFrom()))
                    .orElse(null);

            if (config == null) {
                return false;
            }

            boolean fullSearchEnabled = config.getPec().stream().toList().containsAll(List.of(DigitalAddressSourceInt.GENERAL, DigitalAddressSourceInt.PLATFORM, DigitalAddressSourceInt.SPECIAL));

            if (fullSearchEnabled) {
                return true;
            }
        }

        return false;
    }
}
