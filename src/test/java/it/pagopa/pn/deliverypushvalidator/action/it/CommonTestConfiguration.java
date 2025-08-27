package it.pagopa.pn.deliverypushvalidator.action.it;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.pagopa.pn.commons.configs.MVPParameterConsumer;
import it.pagopa.pn.deliverypushvalidator.action.it.mockbean.*;
import it.pagopa.pn.deliverypushvalidator.action.it.utils.TestUtils;
import it.pagopa.pn.deliverypushvalidator.action.refused.NotificationRefusedActionHandler;
import it.pagopa.pn.deliverypushvalidator.action.startworkflow.*;
import it.pagopa.pn.deliverypushvalidator.action.startworkflow.notificationvalidation.*;
import it.pagopa.pn.deliverypushvalidator.action.utils.InstantNowSupplier;
import it.pagopa.pn.deliverypushvalidator.action.utils.NotificationUtils;
import it.pagopa.pn.deliverypushvalidator.action.utils.TimelineUtils;
import it.pagopa.pn.deliverypushvalidator.config.PnDeliveryPushValidatorConfigs;
import it.pagopa.pn.deliverypushvalidator.config.SendMoreThan20GramsParameterConsumer;
import it.pagopa.pn.deliverypushvalidator.legalfact.DocumentComposition;
import it.pagopa.pn.deliverypushvalidator.logtest.ConsoleAppenderCustom;
import it.pagopa.pn.deliverypushvalidator.middleware.queue.consumer.*;
import it.pagopa.pn.deliverypushvalidator.middleware.queue.consumer.handler.action.DocumentCreationResponseEventHandler;
import it.pagopa.pn.deliverypushvalidator.middleware.queue.consumer.handler.action.NotificationRefusedHandler;
import it.pagopa.pn.deliverypushvalidator.middleware.queue.consumer.handler.action.NotificationValidationHandler;
import it.pagopa.pn.deliverypushvalidator.middleware.queue.consumer.handler.action.ReceivedLegalFactGenerationHandler;
import it.pagopa.pn.deliverypushvalidator.middleware.queue.consumer.router.EventHandlerRegistry;
import it.pagopa.pn.deliverypushvalidator.middleware.queue.consumer.router.EventRouter;
import it.pagopa.pn.deliverypushvalidator.middleware.queue.producer.abstractions.actionspool.impl.TimeParams;
import it.pagopa.pn.deliverypushvalidator.middleware.responsehandler.AddressManagerResponseHandler;
import it.pagopa.pn.deliverypushvalidator.middleware.responsehandler.DocumentCreationResponseHandler;
import it.pagopa.pn.deliverypushvalidator.middleware.responsehandler.F24ResponseHandler;
import it.pagopa.pn.deliverypushvalidator.middleware.responsehandler.SafeStorageResponseHandler;
import it.pagopa.pn.deliverypushvalidator.service.impl.*;
import it.pagopa.pn.deliverypushvalidator.service.mapper.SmartMapper;
import it.pagopa.pn.deliverypushvalidator.utils.PnTechnicalRefusalCostMode;
import it.pagopa.pn.deliverypushvalidator.utils.RefusalCostCalculator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.util.unit.DataSize;

import java.time.Duration;
import java.time.Instant;

import static org.awaitility.Awaitility.setDefaultTimeout;

@ContextConfiguration(classes = {
        StartWorkflowHandler.class,
        AuditLogServiceImpl.class,
        NationalRegistriesServiceImpl.class,
        SafeStorageServiceImpl.class,
        TimelineUtils.class,
        NotificationServiceImpl.class,
        TimelineServiceHttpImpl.class,
        ConfidentialInformationServiceImpl.class,
        AttachmentUtils.class,
        NotificationUtils.class,
        TimelineClientMock.class,
        ObjectMapper.class,
        MVPParameterConsumer.class,
        DocumentCreationRequestServiceImpl.class,
        DocumentCreationRequestDaoMock.class,
        SafeStorageResponseHandler.class,
        DocumentCreationResponseHandler.class,
        ReceivedLegalFactCreationResponseHandler.class,
        NotificationValidationActionHandler.class,
        TaxIdPivaValidator.class,
        ReceivedLegalFactCreationRequest.class,
        NotificationValidationScheduler.class,
        AddressValidator.class,
        AddressManagerServiceImpl.class,
        AddressManagerClientMock.class,
        NationalRegistriesClientMock.class,
        NormalizeAddressHandler.class,
        AddressManagerResponseHandler.class,
        CommonTestConfiguration.SpringTestConfiguration.class,
        F24Validator.class,
        F24ClientMock.class,
        PnExternalRegistriesClientReactiveMock.class,
        PaymentValidator.class,
        NotificationRefusedActionHandler.class,
        F24ResponseHandler.class,
        ActionPoolMock.class,
        //quickWorkAroundForPN-9116
        SendMoreThan20GramsParameterConsumer.class,
        SmartMapper.class,
        DocumentComposition.class,
        RefusalCostCalculator.class,
        PnTechnicalRefusalCostMode.class,
        LookupAddressHandler.class,
        EventRouter.class,
        EventHandlerRegistry.class,
        ActionHandlerRegistry.class,
        NotificationRefusedHandler.class,
        NotificationValidationHandler.class,
        ReceivedLegalFactGenerationHandler.class,
        AddressManagerConsumer.class,
        F24Consumer.class,
        NewNotificationConsumer.class,
        SafeStorageConsumer.class,
        ValidationActionsConsumer.class,
        DocumentCreationResponseHandler.class,
        DocumentCreationResponseEventHandler.class
})
@ExtendWith(SpringExtension.class)
@TestPropertySource(value = "classpath:/application-testIT.properties")
@DirtiesContext
@EnableScheduling
public class CommonTestConfiguration {
    @TestConfiguration
    static class SpringTestConfiguration extends AbstractWorkflowTestConfiguration {
        public SpringTestConfiguration() {
            super();
        }
    }
    @Autowired
    ActionPoolMock actionPoolMock;
    @Autowired
    SafeStorageClientMock safeStorageClientMock;
    @Autowired
    PnDeliveryClientMock pnDeliveryClientMock;
    @Autowired
    NationalRegistriesClientMock nationalRegistriesClientMock;
    @Autowired
    InstantNowSupplier instantNowSupplier;
    @Autowired
    PnDataVaultClientReactiveMock pnDataVaultClientReactiveMock;
    @Autowired
    DocumentCreationRequestDaoMock documentCreationRequestDaoMock;
    @Autowired
    TimelineClientMock timelineClientMock;
    @Autowired
    AddressManagerClientMock addressManagerClientMock;
    @Autowired
    PnDeliveryPushValidatorConfigs cfg;
    
    @BeforeEach
    void setup() {
        setDefaultTimeout(Duration.ofSeconds(120));

        // Viene creato un oggetto Answer per ottenere l'istante corrente al momento della chiamata ...
        Answer<Instant> answer = invocation -> Instant.now();
        // e configurato Mockito per restituire l'istante corrente al momento della chiamata
        Mockito.when(instantNowSupplier.get()).thenAnswer(answer);
        
        setCommonsConfigurationPropertiesForTest(cfg);

        ConsoleAppenderCustom.initializeLog();

        TestUtils.initializeAllMockClient(
                safeStorageClientMock,
                pnDeliveryClientMock,
                nationalRegistriesClientMock,
                pnDataVaultClientReactiveMock,
                documentCreationRequestDaoMock,
                addressManagerClientMock,
                actionPoolMock,
                timelineClientMock
        );
    }

    private void setCommonsConfigurationPropertiesForTest(PnDeliveryPushValidatorConfigs cfg) {
        // Impostazione delle proprietà TimeParams
        TimeParams times = new TimeParams();
        times.setAttachmentRetentionTimeAfterValidation(Duration.ofSeconds(5));
        times.setCheckAttachmentTimeBeforeExpiration(Duration.ofSeconds(2));

        Mockito.when(cfg.getTimeParams()).thenReturn(times);

        // Impostazione delle proprietà di validazione PDF
        Mockito.when(cfg.isCheckPdfValidEnabled()).thenReturn(true);
        Mockito.when(cfg.getCheckPdfSize()).thenReturn(DataSize.ofMegabytes(200));

        //quickWorkAroundForPN-9116
        Mockito.when(cfg.isSendMoreThan20GramsDefaultValue()).thenReturn(true);
        
        //Set send fee
        Mockito.when(cfg.getPagoPaNotificationBaseCost()).thenReturn(100);


        Mockito.when(cfg.getTemplatesEngineBaseUrl()).thenReturn("http://localhost:8090");
    }

}
