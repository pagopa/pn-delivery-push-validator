package it.pagopa.pn.deliverypushvalidator.config.msclient;

import it.pagopa.pn.commons.pnclients.CommonBaseClient;
import it.pagopa.pn.deliverypushvalidator.config.PnDeliveryPushValidatorConfigs;
import it.pagopa.pn.deliverypushvalidator.generated.openapi.msclient.delivery_reactive.ApiClient;
import it.pagopa.pn.deliverypushvalidator.generated.openapi.msclient.delivery_reactive.api.InternalOnlyApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DeliveryApiReactiveConfigurator extends CommonBaseClient {

    @Bean
    public InternalOnlyApi internalOnlyApiReactive(PnDeliveryPushValidatorConfigs cfg){
        ApiClient apiClient = new ApiClient(initWebClient(ApiClient.buildWebClientBuilder()));
        apiClient.setBasePath(cfg.getDeliveryBaseUrl());
        return new InternalOnlyApi(apiClient);
    }
}
