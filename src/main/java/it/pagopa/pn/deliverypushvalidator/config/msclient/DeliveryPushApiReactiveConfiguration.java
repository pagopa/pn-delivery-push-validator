package it.pagopa.pn.deliverypushvalidator.config.msclient;

import it.pagopa.pn.deliverypushvalidator.config.PnDeliveryPushValidatorConfigs;
import it.pagopa.pn.deliverypushvalidator.generated.openapi.msclient.deliverypush.api.NotificationProcessCostApi;
import it.pagopa.pn.deliverypushvalidator.generated.openapi.msclient.deliverypush.ApiClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class DeliveryPushApiReactiveConfiguration {

    @Bean
    @Primary
    public NotificationProcessCostApi deliveryPushPrivateApiConfig(WebClient webClient,
                                                                   PnDeliveryPushValidatorConfigs cfg) {
        ApiClient apiClient = new ApiClient(webClient);
        apiClient.setBasePath(cfg.getDeliveryPushBaseUrl());
        return new NotificationProcessCostApi(apiClient);
    }

}
