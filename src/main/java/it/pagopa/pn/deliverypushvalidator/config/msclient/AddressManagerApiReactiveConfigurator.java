package it.pagopa.pn.deliverypushvalidator.config.msclient;

import it.pagopa.pn.commons.pnclients.CommonBaseClient;
import it.pagopa.pn.deliverypushvalidator.config.PnDeliveryPushValidatorConfigs;
import it.pagopa.pn.deliverypushvalidator.generated.openapi.msclient.addressmanager.ApiClient;
import it.pagopa.pn.deliverypushvalidator.generated.openapi.msclient.addressmanager.api.NormalizeAddressServiceApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
@Configuration
public class AddressManagerApiReactiveConfigurator extends CommonBaseClient {
    @Bean
    public NormalizeAddressServiceApi normalizeAddressReactiveServiceApi(PnDeliveryPushValidatorConfigs cfg){
        ApiClient newApiClient = new ApiClient( initWebClient(ApiClient.buildWebClientBuilder()) );
        newApiClient.setBasePath( cfg.getAddressManagerBaseUrl() );
        return new NormalizeAddressServiceApi(newApiClient);
    }
}
