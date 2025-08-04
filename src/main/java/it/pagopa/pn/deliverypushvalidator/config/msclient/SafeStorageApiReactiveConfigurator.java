package it.pagopa.pn.deliverypushvalidator.config.msclient;

import it.pagopa.pn.commons.pnclients.CommonBaseClient;
import it.pagopa.pn.deliverypushvalidator.config.PnDeliveryPushValidatorConfigs;
import it.pagopa.pn.deliverypushvalidator.generated.openapi.msclient.pnsafestorage.v1.ApiClient;
import it.pagopa.pn.deliverypushvalidator.generated.openapi.msclient.pnsafestorage.v1.api.FileDownloadApi;
import it.pagopa.pn.deliverypushvalidator.generated.openapi.msclient.pnsafestorage.v1.api.FileMetadataUpdateApi;
import it.pagopa.pn.deliverypushvalidator.generated.openapi.msclient.pnsafestorage.v1.api.FileUploadApi;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SafeStorageApiReactiveConfigurator extends CommonBaseClient {

    @Bean
    public FileUploadApi fileUploadApiReactive(PnDeliveryPushValidatorConfigs cfg){
        return new FileUploadApi( getNewApiClient(cfg) );
    }

    @Bean
    public FileDownloadApi fileDownloadApiReactive(PnDeliveryPushValidatorConfigs cfg){
        return new FileDownloadApi( getNewApiClient(cfg) );
    }

    @Bean
    public FileMetadataUpdateApi fileMetadataUpdateApiReactive(PnDeliveryPushValidatorConfigs cfg){
        return new FileMetadataUpdateApi( getNewApiClient(cfg) );
    }
    
    @NotNull
    private ApiClient getNewApiClient(PnDeliveryPushValidatorConfigs cfg) {
        ApiClient newApiClient = new ApiClient( initWebClient(ApiClient.buildWebClientBuilder()) );
        newApiClient.setBasePath( cfg.getSafeStorageBaseUrl() );
        return newApiClient;
    }
}
