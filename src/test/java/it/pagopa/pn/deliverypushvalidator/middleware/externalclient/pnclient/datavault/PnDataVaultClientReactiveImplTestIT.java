package it.pagopa.pn.deliverypushvalidator.middleware.externalclient.pnclient.datavault;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import it.pagopa.pn.commons.exceptions.PnInternalException;
import it.pagopa.pn.deliverypushvalidator.MockAWSObjectsTest;
import it.pagopa.pn.deliverypushvalidator.generated.openapi.msclient.datavault_reactive.model.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockserver.client.MockServerClient;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockserver.integration.ClientAndServer.startClientAndServer;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "pn.delivery-push-validator.data-vault-base-url=http://localhost:9998"
})
class PnDataVaultClientReactiveImplTestIT extends MockAWSObjectsTest {
    @Autowired
    private PnDataVaultClientReactiveImpl client;
    
    private static ClientAndServer mockServer;
    
    @Test
    void getRecipientDenominationByInternalId() throws JsonProcessingException {
        mockServer = startClientAndServer(9998);

        //Given
        String path = "/datavault-private/v1/recipients/internal";

        ObjectMapper mapper = new ObjectMapper();

        String internalId = "internalIdTest";

        BaseRecipientDto responseDto = new BaseRecipientDto();
        responseDto.setDenomination("denomination");
        responseDto.setInternalId(internalId);
        responseDto.setTaxId("taxId");
        responseDto.setRecipientType(RecipientType.PF);
        
        String responseJson = mapper.writeValueAsString(responseDto);

        new MockServerClient("localhost", 9998)
                .when(request()
                        .withMethod("GET")
                        .withPath(path)
                )
                .respond(response()
                        .withBody(responseJson)
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withStatusCode(200)
                );

        Flux<BaseRecipientDto> responseMono = client.getRecipientsDenominationByInternalId(List.of(internalId));
        Assertions.assertNotNull(responseMono);
        BaseRecipientDto response = responseMono.blockFirst();
        Assertions.assertEquals(responseDto, response);

        mockServer.stop();
    }

    @Test
    void getRecipientDenominationByInternalIdKo() {
        mockServer = startClientAndServer(9998);

        //Given
        String path = "/datavault-private/v1/recipients/internal";
        String internalId = "internalId";

        new MockServerClient("localhost", 9998)
                .when(request()
                        .withMethod("GET")
                        .withPath(path)
                )
                .respond(response()
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withStatusCode(400)
                );

        Flux<BaseRecipientDto> responseMono = client.getRecipientsDenominationByInternalId(List.of(internalId));
        Assertions.assertNotNull(responseMono);
        
        Assertions.assertThrows( PnInternalException.class, responseMono::blockFirst);
        
        mockServer.stop();
    }

    @Test
    void getNotificationTimeLines() throws JsonProcessingException {
        mockServer = startClientAndServer(9998);

        //Given
        String path = "/datavault-private/v1/timelines";

        ObjectMapper mapper = new ObjectMapper();

        ConfidentialTimelineElementId confidentialTimelineElementId = ConfidentialTimelineElementId.builder()
                .iun("iun")
                .timelineElementId("timelineElementId")
                .build();

        ConfidentialTimelineElementDto responseDto = new ConfidentialTimelineElementDto();
        responseDto.setDenomination("denomination");
        responseDto.setTimelineElementId("timelineElementId");
        responseDto.setTaxId("taxId");

        AnalogDomicile analogDomicile = AnalogDomicile.builder()
                .at("at")
                .address("via address")
                .cap("00100")
                .municipality("municipality")
                .build();
        responseDto.setPhysicalAddress(analogDomicile);

        String responseJson = mapper.writeValueAsString(responseDto);

        new MockServerClient("localhost", 9998)
                .when(request()
                        .withMethod("POST")
                        .withPath(path)
                )
                .respond(response()
                        .withBody(responseJson)
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withStatusCode(200)
                );

        Flux<ConfidentialTimelineElementDto> fluxDto = client.getNotificationTimelines(List.of(confidentialTimelineElementId));
        Assertions.assertNotNull(fluxDto);

        ConfidentialTimelineElementDto dto = fluxDto.blockFirst();

        Assertions.assertNotNull(dto);
        Assertions.assertEquals("denomination", dto.getDenomination());
        Assertions.assertEquals("timelineElementId", dto.getTimelineElementId());
        Assertions.assertEquals("taxId", dto.getTaxId());
        Assertions.assertNotNull(dto.getPhysicalAddress());
        Assertions.assertEquals(analogDomicile.getAddress(), dto.getPhysicalAddress().getAddress());
        Assertions.assertEquals(analogDomicile.getAt(), dto.getPhysicalAddress().getAt());
        Assertions.assertEquals(analogDomicile.getCap(), dto.getPhysicalAddress().getCap());
        Assertions.assertEquals(analogDomicile.getMunicipality(), dto.getPhysicalAddress().getMunicipality());

        mockServer.stop();
    }

    @Test
    void getNotificationTimeLinesKo() {
        mockServer = startClientAndServer(9998);

        //Given
        String path = "/datavault-private/v1/timelines";
        ConfidentialTimelineElementId confidentialTimelineElementId = ConfidentialTimelineElementId.builder()
                .iun("iun")
                .timelineElementId("timelineElementId")
                .build();

        new MockServerClient("localhost", 9998)
                .when(request()
                        .withMethod("POST")
                        .withPath(path)
                )
                .respond(response()
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withStatusCode(400)
                );

        Flux<ConfidentialTimelineElementDto> fluxDto = client.getNotificationTimelines(List.of(confidentialTimelineElementId));
        Assertions.assertThrows(PnInternalException.class, fluxDto::blockFirst);

        mockServer.stop();
    }

    @Test
    void getMessageById() throws JsonProcessingException {
        mockServer = startClientAndServer(9998);

        //Given
        UUID messageId = UUID.randomUUID();
        UUID senderId = UUID.randomUUID();
        String path = "/datavault-private/v1/messages/" + messageId;

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());

        MessageResponseDto responseDto = new MessageResponseDto();
        responseDto.setMessageId(messageId);
        responseDto.setSenderId(senderId.toString());
        responseDto.setCreatedAt(Instant.now());

        String responseJson = mapper.writeValueAsString(responseDto);

        new MockServerClient("localhost", 9998)
                .when(request()
                        .withMethod("GET")
                        .withPath(path)
                )
                .respond(response()
                        .withBody(responseJson)
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withStatusCode(200)
                );

        Mono<MessageResponseDto> responseMono = client.getMessageById(messageId, senderId);
        Assertions.assertNotNull(responseMono);
        MessageResponseDto response = responseMono.block();
        Assertions.assertNotNull(response);
        Assertions.assertEquals(messageId, response.getMessageId());
        Assertions.assertEquals(senderId.toString(), response.getSenderId());

        mockServer.stop();
    }

    @Test
    void getMessageByIdKo() {
        mockServer = startClientAndServer(9998);

        //Given
        UUID messageId = UUID.randomUUID();
        UUID senderId = UUID.randomUUID();
        String path = "/datavault-private/v1/messages/" + messageId;

        new MockServerClient("localhost", 9998)
                .when(request()
                        .withMethod("GET")
                        .withPath(path)
                )
                .respond(response()
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withStatusCode(400)
                );

        Mono<MessageResponseDto> responseMono = client.getMessageById(messageId, senderId);
        Assertions.assertNotNull(responseMono);
        Assertions.assertThrows(PnInternalException.class, responseMono::block);

        mockServer.stop();
    }
}