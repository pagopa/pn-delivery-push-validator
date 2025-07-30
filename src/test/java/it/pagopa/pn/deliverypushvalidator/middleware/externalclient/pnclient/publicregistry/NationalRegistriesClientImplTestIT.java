package it.pagopa.pn.deliverypushvalidator.middleware.externalclient.pnclient.publicregistry;


import it.pagopa.pn.deliverypushvalidator.MockAWSObjectsTest;
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

import static it.pagopa.pn.deliverypushvalidator.middleware.externalclient.pnclient.nationalregistries.NationalRegistriesClientImpl.PN_NATIONAL_REGISTRIES_CX_ID_VALUE;
import static org.mockserver.integration.ClientAndServer.startClientAndServer;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "pn.delivery-push.national-registries-base-url=http://localhost:9999"
})
class NationalRegistriesClientImplTestIT extends MockAWSObjectsTest {

    private static final String PN_NATIONAL_REGISTRIES_CX_ID = "pn-national-registries-cx-id";

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

    // TODO: Scrivere test per i metodi checkTaxId e sendRequestForGetPhysicalAddresses

}
