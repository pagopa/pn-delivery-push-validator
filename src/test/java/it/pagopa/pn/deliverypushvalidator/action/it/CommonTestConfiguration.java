package it.pagopa.pn.deliverypushvalidator.action.it;

import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
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
import java.util.ArrayList;
import java.util.List;

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
//        DigitalWorkflowFirstSendRepeatHandler.class,
//        SendAndUnscheduleNotification.class,
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
        ValidationActionsConsumer.class
})
@ExtendWith(SpringExtension.class)
@TestPropertySource(value = "classpath:/application-testIT.properties")
@DirtiesContext
@EnableScheduling
public class CommonTestConfiguration {
    private static final String[] PARAMETER_STORES_MAP_ZIP_EXPERIMENTATION_LIST = {"radd-expeAAArimentation-zip-1", "radd-experimentation-zip-2", "radd-experimentation-zip-3", "radd-experimentation-zip-4", "radd-experimentation-zip-5"};

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
    AddressManagerClientMock addressManagerClientMock;
    @Autowired
    PnDeliveryPushValidatorConfigs cfg;
    
    @BeforeEach
    public void setup() {
        setDefaultTimeout(Duration.ofSeconds(120));

        // Viene creato un oggetto Answer per ottenere l'istante corrente al momento della chiamata ...
        Answer<Instant> answer = invocation -> Instant.now();
        // e configurato Mockito per restituire l'istante corrente al momento della chiamata
        Mockito.when(instantNowSupplier.get()).thenAnswer(answer);
        
        setcCommonsConfigurationPropertiesForTest(cfg);

        ConsoleAppenderCustom.initializeLog();

        TestUtils.initializeAllMockClient(
                safeStorageClientMock,
                pnDeliveryClientMock,
                nationalRegistriesClientMock,
                pnDataVaultClientReactiveMock,
                documentCreationRequestDaoMock,
                addressManagerClientMock,
                actionPoolMock
        );
    }

    private void setcCommonsConfigurationPropertiesForTest(PnDeliveryPushValidatorConfigs cfg) {
        // Impostazione delle proprietà TimeParams
        TimeParams times = new TimeParams();
        times.setWaitingForReadCourtesyMessage(Duration.ofSeconds(1));
        times.setSecondNotificationWorkflowWaitingTime(Duration.ofSeconds(1));
        times.setSchedulingDaysSuccessDigitalRefinement(Duration.ofSeconds(1));
        times.setSchedulingDaysFailureDigitalRefinement(Duration.ofSeconds(1));
        times.setSchedulingDaysSuccessAnalogRefinement(Duration.ofSeconds(1));
        times.setSchedulingDaysFailureAnalogRefinement(Duration.ofSeconds(1));
        times.setNotificationNonVisibilityTime("21:00");
        times.setTimeToAddInNonVisibilityTimeCase(Duration.ofSeconds(1));
        times.setAttachmentRetentionTimeAfterValidation(Duration.ofSeconds(5));
        times.setCheckAttachmentTimeBeforeExpiration(Duration.ofSeconds(2));
        times.setAttachmentTimeToAddAfterExpiration(Duration.ofSeconds(50));
        
        Mockito.when(cfg.getTimeParams()).thenReturn(times);


        // Impostazione delle proprietà Webapp
        PnDeliveryPushValidatorConfigs.Webapp webapp = new PnDeliveryPushValidatorConfigs.Webapp();
        webapp.setDirectAccessUrlTemplatePhysical("http://localhost:8090/dist/direct_access_pf");
        webapp.setDirectAccessUrlTemplateLegal("http://localhost:8090/dist/direct_access_pg");
        webapp.setFaqUrlTemplateSuffix("faq.html");
        webapp.setQuickAccessUrlAarDetailSuffix("notifica?aar");
        webapp.setLandingUrl("https://www.dev.pn.pagopa.it");
        webapp.setRaddPhoneNumber("06.4520.2323");
        webapp.setAarSenderLogoUrlTemplate("TO_BASE64_RESOLVER:https://example.com/<PA_ID>/logo.png");
        Mockito.when(cfg.getWebapp()).thenReturn(webapp);

        //todo: va tolto?

        // Impostazione delle proprietà di retention degli allegati
//        Mockito.when(cfg.getRetentionAttachmentDaysAfterRefinement()).thenReturn(120);

        // Impostazione delle proprietà di validazione PDF
        Mockito.when(cfg.isCheckPdfValidEnabled()).thenReturn(true);
        Mockito.when(cfg.getCheckPdfSize()).thenReturn(DataSize.ofMegabytes(200));

        // Impostazione delle proprietà di PnSendMode
        List<String> pnSendModeList = new ArrayList<>();
        pnSendModeList.add("1970-01-01T00:00:00Z;AAR-DOCUMENTS-PAYMENTS;AAR-DOCUMENTS-PAYMENTS;AAR-DOCUMENTS-PAYMENTS;AAR_NOTIFICATION");
        pnSendModeList.add("2023-11-30T23:00:00Z;AAR;AAR;AAR-DOCUMENTS-PAYMENTS;AAR_NOTIFICATION_RADD");


        Mockito.when(cfg.getPnSendMode()).thenReturn(pnSendModeList);

        //quickWorkAroundForPN-9116
        Mockito.when(cfg.isSendMoreThan20GramsDefaultValue()).thenReturn(true);
        
        //Set send fee
        Mockito.when(cfg.getPagoPaNotificationBaseCost()).thenReturn(100);

        Mockito.when(cfg.getErrorCorrectionLevelQrCode()).thenReturn(ErrorCorrectionLevel.H);

        List<String> pnRaddExperimentationStore = new ArrayList<>();
        pnRaddExperimentationStore.add(PARAMETER_STORES_MAP_ZIP_EXPERIMENTATION_LIST[0]);
        pnRaddExperimentationStore.add(PARAMETER_STORES_MAP_ZIP_EXPERIMENTATION_LIST[1]);
        pnRaddExperimentationStore.add(PARAMETER_STORES_MAP_ZIP_EXPERIMENTATION_LIST[2]);
        pnRaddExperimentationStore.add(PARAMETER_STORES_MAP_ZIP_EXPERIMENTATION_LIST[3]);
        pnRaddExperimentationStore.add(PARAMETER_STORES_MAP_ZIP_EXPERIMENTATION_LIST[4]);
        Mockito.when(cfg.getRaddExperimentationStoresName()).thenReturn(pnRaddExperimentationStore);

        Mockito.when(cfg.getFeatureUnreachableRefinementPostAARStartDate()).thenReturn(Instant.parse("2024-11-27T00:00:00Z"));

        Mockito.when(cfg.getPfNewWorkflowStop()).thenReturn("2099-03-31T23:00:00Z");
        Mockito.when(cfg.getPfNewWorkflowStart()).thenReturn("2099-02-13T23:00:00Z");

        Mockito.when(cfg.getTemplateURLforPEC()).thenReturn("/templates-engine-private/v1/templates/notification-aar-for-pec");
        Mockito.when(cfg.getTemplatesEngineBaseUrl()).thenReturn("http://localhost:8090");
    }

}
