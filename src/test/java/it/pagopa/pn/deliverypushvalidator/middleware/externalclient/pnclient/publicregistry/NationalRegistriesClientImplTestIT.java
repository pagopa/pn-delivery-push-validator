package it.pagopa.pn.deliverypushvalidator.middleware.externalclient.pnclient.publicregistry;

import it.pagopa.pn.deliverypushvalidator.MockAWSObjectsTest;
import it.pagopa.pn.deliverypushvalidator.generated.openapi.msclient.nationalregistries.model.PhysicalAddressesRequestBody;
import it.pagopa.pn.deliverypushvalidator.generated.openapi.msclient.nationalregistries.model.RecipientAddressRequestBody;
import it.pagopa.pn.deliverypushvalidator.middleware.externalclient.pnclient.nationalregistries.NationalRegistriesClient;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockserver.client.MockServerClient;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.time.Instant;
import java.util.List;

import static org.mockserver.integration.ClientAndServer.startClientAndServer;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "pn.delivery-push-validator.national-registries-base-url=http://localhost:9999"
})
class NationalRegistriesClientImplTestIT extends MockAWSObjectsTest {

    private static ClientAndServer mockServer;

    @Autowired
    private NationalRegistriesClient nationalRegistriesClient;

    @BeforeAll
    public static void startMockServer() {
        mockServer = startClientAndServer(9999);
    }

    @AfterAll
    public static void stopMockServer() {
        mockServer.stop();
    }

    @Test
    void sendRequestForGetDigitalAddressTest() {
        new MockServerClient("localhost", 9999)
                .when(request()
                        .withMethod("POST")
                        .withPath("/national-registries-private/physical-addresses"))
                .respond(response()
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withStatusCode(200));

        RecipientAddressRequestBody recipientAddressRequestBody= new RecipientAddressRequestBody();
        recipientAddressRequestBody.setTaxId("CPLDVL07H25H850V");
        recipientAddressRequestBody.setRecIndex(1);
        recipientAddressRequestBody.setRecipientType(RecipientAddressRequestBody.RecipientTypeEnum.PF);

        PhysicalAddressesRequestBody physicalAddressesRequestBody= new PhysicalAddressesRequestBody();
        physicalAddressesRequestBody.setAddresses(List.of(recipientAddressRequestBody));
        physicalAddressesRequestBody.setCorrelationId("correlationId");
        physicalAddressesRequestBody.setReferenceRequestDate(Instant.now());

        Assertions.assertDoesNotThrow(
                () -> nationalRegistriesClient.sendRequestForGetPhysicalAddresses(physicalAddressesRequestBody));
    }

    @Test
    void checkTaxIdTest() {
        new MockServerClient("localhost", 9999)
                .when(request()
                        .withMethod("POST")
                        .withPath("/national-registries-private/agenzia-entrate/tax-id"))
                .respond(response()
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withStatusCode(200));

        final String taxIdTest = "CPLDVL07H25H850V";

        Assertions.assertDoesNotThrow(
                () -> nationalRegistriesClient.checkTaxId(taxIdTest));
    }

}
