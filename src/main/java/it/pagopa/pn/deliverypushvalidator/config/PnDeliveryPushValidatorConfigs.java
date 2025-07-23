package it.pagopa.pn.deliverypushvalidator.config;


import it.pagopa.pn.commons.conf.SharedAutoConfiguration;
import jakarta.annotation.PostConstruct;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@ConfigurationProperties( prefix = "pn.delivery-push-validator")
@Data
@Import({SharedAutoConfiguration.class})
public class PnDeliveryPushValidatorConfigs {

    private DocumentCreationRequestDao documentCreationRequestDao;

    @Data
    public static class DocumentCreationRequestDao {
        private String tableName;
    }

    private boolean safeStorageFileNotFoundRetry;

    @PostConstruct
    public void init() {
        System.out.println(this);
    }

}
