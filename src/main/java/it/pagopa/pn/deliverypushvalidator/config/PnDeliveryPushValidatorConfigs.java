package it.pagopa.pn.deliverypushvalidator.config;

import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import it.pagopa.pn.commons.conf.SharedAutoConfiguration;
import it.pagopa.pn.deliverypushvalidator.middleware.queue.producer.abstractions.actionspool.impl.TimeParams;
import jakarta.annotation.PostConstruct;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.util.unit.DataSize;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Configuration
@ConfigurationProperties( prefix = "pn.delivery-push-validator")
@Data
@Import({SharedAutoConfiguration.class})
public class PnDeliveryPushValidatorConfigs {

    private DocumentCreationRequestDao documentCreationRequestDao;
    private Topics topics;
    private boolean safeStorageFileNotFoundRetry;
    private TimeParams timeParams;
    private int pagoPaNotificationBaseCost;
    private int pagoPaNotificationFee;
    private int pagoPaNotificationVat;
    private String pfNewWorkflowStart;
    private String pfNewWorkflowStop;
    private String AAROnlyPECForRADDAndPF;
    private Duration[] validationRetryIntervals;
    private Instant featureUnreachableRefinementPostAARStartDate;
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
    private boolean checkCfEnabled;
    private DataSize checkPdfSize;
    private boolean checkPdfValidEnabled;
    private List<String> pnSendMode;
    private List<String> raddExperimentationStoresName;
    private String f24CxId;
    private String safeStorageCxId;
    private String safeStorageCxIdUpdatemetadata;
    private String templateURLforPEC;
    private boolean sendMoreThan20GramsDefaultValue;
    private Webapp webapp;
    private boolean additionalLangsEnabled;
    private ErrorCorrectionLevel errorCorrectionLevelQrCode;

    @Data
    public static class Topics {
        private String deliveryValidationEvents;
        private String validationActions;
        private String addressManagerEvents;
        private String safeStorageEvents;
        private String f24Events;
    }

    @Data
    public static class Webapp {
        private String directAccessUrlTemplatePhysical;
        private String directAccessUrlTemplateLegal;
        private String faqUrlTemplateSuffix;
        private String faqSendHash;
        private String quickAccessUrlAarDetailSuffix;
        private String landingUrl;
        private String raddPhoneNumber;
        private String aarSenderLogoUrlTemplate;
    }

    @Data
    public static class DocumentCreationRequestDao {
        private String tableName;
    }

    @PostConstruct
    public void init() {
        System.out.println(this);
    }

}
