package it.pagopa.pn.deliverypushvalidator.config;

import it.pagopa.pn.commons.conf.SharedAutoConfiguration;
import it.pagopa.pn.deliverypushvalidator.middleware.queue.producer.abstractions.actionspool.impl.TimeParams;
import jakarta.annotation.PostConstruct;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.util.unit.DataSize;

import java.time.Duration;

@Configuration
@ConfigurationProperties( prefix = "pn.delivery-push-validator")
@Data
@Import({SharedAutoConfiguration.class})
@Slf4j
public class PnDeliveryPushValidatorConfigs {

    private DocumentCreationRequestDao documentCreationRequestDao;
    private Topics topics;
    private boolean safeStorageFileNotFoundRetry;
    private TimeParams timeParams;
    private int pagoPaNotificationBaseCost;
    private Duration[] validationRetryIntervals;
    private String nationalRegistriesBaseUrl;
    private String addressManagerBaseUrl;
    private String addressManagerApiKey;
    private String actionManagerBaseUrl;
    private String f24BaseUrl;
    private String timelineClientBaseUrl;
    private String dataVaultBaseUrl;
    private String externalRegistryBaseUrl;
    private String templatesEngineBaseUrl;
    private String deliveryBaseUrl;
    private String safeStorageBaseUrl;
    private String deliveryPushBaseUrl;
    private boolean checkCfEnabled;
    private DataSize checkPdfSize;
    private boolean checkPdfValidEnabled;
    private String f24CxId;
    private String safeStorageCxId;
    private String safeStorageCxIdUpdatemetadata;
    private boolean sendMoreThan20GramsDefaultValue;
    private boolean additionalLangsEnabled;
    private String technicalRefusalCostMode;

    @Data
    public static class Topics {
        private String deliveryValidationEvents;
        private String validationActions;
        private String addressManagerEvents;
        private String safeStorageEvents;
        private String f24Events;
        private String informalValidationInputEvents;
    }


    @Data
    public static class DocumentCreationRequestDao {
        private String tableName;
    }

    @PostConstruct
    public void init() {
        log.info("PnDeliveryPushValidatorConfigs={}", this);
    }

}
