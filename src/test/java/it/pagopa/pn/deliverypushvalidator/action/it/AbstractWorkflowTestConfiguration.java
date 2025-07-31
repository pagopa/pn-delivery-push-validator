package it.pagopa.pn.deliverypushvalidator.action.it;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.pagopa.pn.commons.abstractions.ParameterConsumer;
import it.pagopa.pn.deliverypushvalidator.action.it.mockbean.*;
import it.pagopa.pn.deliverypushvalidator.action.utils.InstantNowSupplier;
import it.pagopa.pn.deliverypushvalidator.config.PnDeliveryPushValidatorConfigs;
import it.pagopa.pn.deliverypushvalidator.legalfact.CustomInstantWriter;
import it.pagopa.pn.deliverypushvalidator.legalfact.LegalFactGenerator;
import it.pagopa.pn.deliverypushvalidator.legalfact.LegalFactGeneratorTemplates;
import it.pagopa.pn.deliverypushvalidator.legalfact.PhysicalAddressWriter;
import it.pagopa.pn.deliverypushvalidator.middleware.externalclient.pnclient.delivery.PnDeliveryClient;
import it.pagopa.pn.deliverypushvalidator.middleware.externalclient.pnclient.deliverypush.PnDeliveryPushClientReactive;
import it.pagopa.pn.deliverypushvalidator.middleware.externalclient.pnclient.externalregistry.PnExternalRegistriesClientReactive;
import it.pagopa.pn.deliverypushvalidator.middleware.externalclient.pnclient.safestorage.PnSafeStorageClient;
import it.pagopa.pn.deliverypushvalidator.middleware.externalclient.pnclient.templatesengine.TemplatesClient;
import it.pagopa.pn.deliverypushvalidator.middleware.queue.consumer.router.deserializer.RouterDeserializer;
import it.pagopa.pn.deliverypushvalidator.middleware.queue.consumer.router.deserializer.impl.JsonRouterDeserializer;
import it.pagopa.pn.deliverypushvalidator.middleware.responsehandler.SafeStorageResponseHandler;
import it.pagopa.pn.deliverypushvalidator.service.*;
import it.pagopa.pn.deliverypushvalidator.service.impl.NotificationProcessCostServiceImpl;
import it.pagopa.pn.deliverypushvalidator.service.impl.SaveLegalFactsServiceImpl;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;

import java.util.ArrayList;
import java.util.List;

public class AbstractWorkflowTestConfiguration {
    static final int SEND_FEE = 100;

    @Bean
    public PnDeliveryPushValidatorConfigs pnDeliveryPushConfigs() {
        PnDeliveryPushValidatorConfigs pnDeliveryPushConfigs = Mockito.mock(PnDeliveryPushValidatorConfigs.class);

        Mockito.when(pnDeliveryPushConfigs.getPagoPaNotificationBaseCost()).thenReturn(SEND_FEE);

        return pnDeliveryPushConfigs;
    }
    @Bean
    public PnDeliveryPushClientReactive pnDeliveryPushClientReactive() {
        return new PnDeliveryPushClientMock();
    }

    @Bean
    public NotificationProcessCostService notificationProcessCostService(@Lazy PnExternalRegistriesClientReactive pnExternalRegistriesClientReactive,
                                                                         @Lazy PnDeliveryPushClientReactive pnDeliveryPushClientReactive,
                                                                         @Lazy PnDeliveryPushValidatorConfigs cfg) {
        return new NotificationProcessCostServiceImpl(pnExternalRegistriesClientReactive, cfg, pnDeliveryPushClientReactive);
    }

    @Bean
    public PnDeliveryClient testPnDeliveryClient(PnDataVaultClientReactiveMock pnDataVaultClientReactiveMock) {
        PnDeliveryClientMock pnDeliveryClientMock = new PnDeliveryClientMock(pnDataVaultClientReactiveMock);
        pnDataVaultClientReactiveMock.setPnDeliveryClientMock(pnDeliveryClientMock);
        return pnDeliveryClientMock;
    }

    @Bean
    public PnDataVaultClientReactiveMock testPnDataVaultClient() {
        return new PnDataVaultClientReactiveMock();
    }

    @Bean
    public PnSafeStorageClient safeStorageTest(DocumentCreationRequestService creationRequestService,
                                               SafeStorageResponseHandler safeStorageResponseHandler) {
        return new SafeStorageClientMock(creationRequestService, safeStorageResponseHandler);
    }

    @Bean
    public InstantNowSupplier instantNowSupplierTest() {
        return Mockito.mock(InstantNowSupplier.class);
    }

    @Bean
    public LegalFactGenerator legalFactGeneratorTemplatesClient(PnDeliveryPushValidatorConfigs pnDeliveryPushConfigs) {
        CustomInstantWriter instantWriter = new CustomInstantWriter();
        PhysicalAddressWriter physicalAddressWriter = new PhysicalAddressWriter();
        return new LegalFactGeneratorTemplates(instantWriter, physicalAddressWriter, pnDeliveryPushConfigs, templatesClient());
    }

    @Bean
    public TemplatesClient templatesClient() {
        return new TemplatesClientMock();
    }

    @Bean
    public SaveLegalFactsServiceImpl LegalFactsTest(SafeStorageService safeStorageService,
                                                    LegalFactGenerator pdfUtils) {
        return new SaveLegalFactsServiceImpl(pdfUtils, safeStorageService);
    }

    @Bean
    public ActionHandlerMock ActionHandlerMock(ActionHandlerRegistry actionHandlerRegistry) {
        return new ActionHandlerMock(actionHandlerRegistry);
    }

    @Bean
    public SchedulerServiceMock schedulerServiceMockMock(@Lazy ActionPoolMock actionPoolMock) {
        return new SchedulerServiceMock(actionPoolMock);
    }

    @Bean
    public ParameterConsumer pnParameterConsumerClientTest() {
        return new AbstractCachedSsmParameterConsumerMock();
    }

    @Bean
    public F24Service f24Service() {
        return Mockito.mock(F24Service.class);
    }

    @Bean("jsonRouterDeserializer")
    public RouterDeserializer routerDeserializer() {
        return new JsonRouterDeserializer(new ObjectMapper());
    }

}
