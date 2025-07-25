package it.pagopa.pn.deliverypushvalidator.config;

import it.pagopa.pn.commons.conf.SharedAutoConfiguration;
import jakarta.annotation.PostConstruct;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Data
@Configuration
@ConfigurationProperties( prefix = "pn.delivery-push-validator")
@Import({SharedAutoConfiguration.class})
public class PnDeliveryPushValidatorConfigs {

    private Topics topics;

    @Data
    public static class Topics {
        private String deliveryValidationEvents;
        private String validationActions;
        private String addressManagerEvents;
        private String safeStorageEvents;
        private String f24Events;
    }

    @PostConstruct
    public void init() {
        System.out.println(this);
    }
}
