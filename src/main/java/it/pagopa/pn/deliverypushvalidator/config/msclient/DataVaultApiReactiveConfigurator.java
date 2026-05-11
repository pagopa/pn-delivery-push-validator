package it.pagopa.pn.deliverypushvalidator.config.msclient;

import it.pagopa.pn.commons.pnclients.CommonBaseClient;
import it.pagopa.pn.deliverypushvalidator.config.PnDeliveryPushValidatorConfigs;
import it.pagopa.pn.deliverypushvalidator.generated.openapi.msclient.datavault_reactive.ApiClient;
import it.pagopa.pn.deliverypushvalidator.generated.openapi.msclient.datavault_reactive.api.NotificationsApi;
import it.pagopa.pn.deliverypushvalidator.generated.openapi.msclient.datavault_reactive.api.RecipientsApi;
import it.pagopa.pn.deliverypushvalidator.generated.openapi.msclient.datavault_reactive.api.MessagesApi;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataVaultApiReactiveConfigurator extends CommonBaseClient {

    @Bean
    public NotificationsApi notificationsApiReactive(PnDeliveryPushValidatorConfigs cfg){
        return new NotificationsApi(getNewApiClient(cfg));
    }

    @Bean
    public RecipientsApi recipientsApiReactive(PnDeliveryPushValidatorConfigs cfg){
        return new RecipientsApi(getNewApiClient(cfg));
    }
    
    @Bean
    public MessagesApi messagesApiReactive(PnDeliveryPushValidatorConfigs cfg){
        return new MessagesApi(getNewApiClient(cfg));
    }

    @NotNull
    private ApiClient getNewApiClient(PnDeliveryPushValidatorConfigs cfg) {
        ApiClient newApiClient = new ApiClient( initWebClient(ApiClient.buildWebClientBuilder()) );
        newApiClient.setBasePath( cfg.getDataVaultBaseUrl() );
        return newApiClient;
    }

}
