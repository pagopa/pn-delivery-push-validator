package it.pagopa.pn.deliverypushvalidator.middleware.queue.consumer;

import it.pagopa.pn.api.dto.events.*;
import it.pagopa.pn.deliverypushvalidator.LocalStackTestConfig;
import it.pagopa.pn.deliverypushvalidator.MockActionPoolTest;
import it.pagopa.pn.deliverypushvalidator.action.startworkflow.notificationvalidation.NotificationValidationActionHandler;
import it.pagopa.pn.deliverypushvalidator.action.utils.TimelineUtils;
import it.pagopa.pn.deliverypushvalidator.middleware.responsehandler.F24ResponseHandler;
import it.pagopa.pn.deliverypushvalidator.service.F24Service;
import it.pagopa.pn.deliverypushvalidator.service.SchedulerService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.function.context.FunctionCatalog;
import org.springframework.cloud.function.context.test.FunctionalSpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static org.mockito.ArgumentMatchers.any;


@FunctionalSpringBootTest
@Import(LocalStackTestConfig.class)
class F24ConsumerTest extends MockActionPoolTest {

    @Autowired
    private FunctionCatalog functionCatalog;

    @InjectMocks
    private F24ResponseHandler handler;

    @MockitoBean
    private NotificationValidationActionHandler validationActionHandler;

    @MockitoBean
    private F24Service f24Service;

    @MockitoBean
    private SchedulerService schedulerService;

    @MockitoBean
    private TimelineUtils timelineUtils;

    @Test
    void consumeMessageOK() {
        Message<PnF24MetadataValidationEndEvent.Detail> pnF24EventInboundConsumer = functionCatalog.lookup(Consumer.class, "pnF24EventInboundConsumer");

        PnF24MetadataValidationEndEvent.Detail event = new PnF24MetadataValidationEndEvent.Detail();
        event.setMetadataValidationEnd(PnF24MetadataValidationEndEventPayload.builder()
                        .setId("iun")
                        .status("ok")
                .build());

        Mockito.when(timelineUtils.checkIsNotificationCancellationRequested(any())).thenReturn(false);
        Mockito.doNothing().when(validationActionHandler).handleValidateF24Response(any());

        Message<PnF24MetadataValidationEndEvent.Detail> message = MessageBuilder.withPayload(event).build();

        Mockito.verify(validationActionHandler,Mockito.times(1)).handleValidateF24Response(any());
        Mockito.verify(f24Service,Mockito.never()).handleF24PrepareResponse(any(),any());
        Mockito.verify(schedulerService,Mockito.never()).scheduleEvent(any(),any(),any());
    }



    @Test
    void handlePrepareResponseReceivedOK() {
       Message<PnF24PdfSetReadyEvent.Detail> pnF24EventInboundConsumer = functionCatalog.lookup(Consumer.class, "pnF24EventInboundConsumer");

        List<PnF24PdfSetReadyEventItem> pnF24PdfSetReadyEventItems = List.of(
                PnF24PdfSetReadyEventItem.builder().pathTokens("0_0").uri("uri1").build(),
                PnF24PdfSetReadyEventItem.builder().pathTokens("0_0").uri("uri2").build(),
                PnF24PdfSetReadyEventItem.builder().pathTokens("1_0").uri("uri1").build(),
                PnF24PdfSetReadyEventItem.builder().pathTokens("1_0").uri("uri2").build(),
                PnF24PdfSetReadyEventItem.builder().pathTokens("1_0").uri("uri3").build()
        );

        String requestId = "GENERATE_F24_REQUEST.IUN_XWGR-MZJX-VNLW-202403-L-1";
        PnF24PdfSetReadyEventPayload pnF24PdfSetReadyEventPayload = PnF24PdfSetReadyEventPayload.builder()
                .requestId(requestId)
                .generatedPdfsUrls(pnF24PdfSetReadyEventItems)
                .status("OK")
                .build();


        PnF24PdfSetReadyEvent.Detail event = new PnF24PdfSetReadyEvent.Detail().toBuilder()
                .pdfSetReady(pnF24PdfSetReadyEventPayload)
                .clientId("test1")
                .build();

        Map<Integer, List<String>> expectedArgument = Map.of(
                0,List.of("uri1","uri2"),
                1,List.of("uri1","uri2","uri3")
        );

        Mockito.when(timelineUtils.getIunFromTimelineId(requestId)).thenReturn("XWGR-MZJX-VNLW-202403-L-1");
        Mockito.doNothing().when(f24Service).handleF24PrepareResponse(any(),any());
        Mockito.doNothing().when(schedulerService).scheduleEvent(any(),any(),any());

        Message<PnF24PdfSetReadyEvent.Detail> message = MessageBuilder.withPayload(event).build();

        ArgumentCaptor<Map<Integer, List<String>>> captor = ArgumentCaptor.forClass(Map.class);
        Mockito.verify(f24Service,Mockito.times(1)).handleF24PrepareResponse(any(),captor.capture());
        Assertions.assertEquals(captor.getAllValues().get(0),expectedArgument);
        Mockito.verify(schedulerService,Mockito.times(1)).scheduleEvent(any(),any(),any());

        Mockito.verify(validationActionHandler,Mockito.never()).handleValidateF24Response(any());
    }
}
