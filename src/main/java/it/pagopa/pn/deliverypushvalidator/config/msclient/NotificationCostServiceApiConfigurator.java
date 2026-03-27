package it.pagopa.pn.deliverypushvalidator.config.msclient;

import it.pagopa.pn.commons.pnclients.CommonBaseClient;
import it.pagopa.pn.deliverypushvalidator.config.PnDeliveryPushValidatorConfigs;
import it.pagopa.pn.deliverypushvalidator.generated.openapi.msclient.notificationcostservice.ApiClient;
import it.pagopa.pn.deliverypushvalidator.generated.openapi.msclient.notificationcostservice.api.NotificationCostRecipientApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import static it.pagopa.pn.deliverypushvalidator.generated.openapi.msclient.notificationcostservice.ApiClient.buildWebClientBuilder;

@Configuration
public class NotificationCostServiceApiConfigurator extends CommonBaseClient {

    @Bean
    @Primary
    public NotificationCostRecipientApi notificationCostRecipientApi(PnDeliveryPushValidatorConfigs cfg) {
        ApiClient apiClient = new ApiClient(initWebClient(buildWebClientBuilder()));
        apiClient.setBasePath(cfg.getNotificationCostServiceBaseUrl());
        return new NotificationCostRecipientApi(apiClient);
    }

}
