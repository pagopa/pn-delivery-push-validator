package it.pagopa.pn.deliverypushvalidator.service.impl;

import it.pagopa.pn.commons.exceptions.PnInternalException;
import it.pagopa.pn.deliverypushvalidator.action.utils.TimelineUtils;
import it.pagopa.pn.deliverypushvalidator.dto.address.PhysicalAddressInt;
import it.pagopa.pn.deliverypushvalidator.dto.ext.datavault.RecipientTypeInt;
import it.pagopa.pn.deliverypushvalidator.dto.ext.delivery.notification.NotificationInt;
import it.pagopa.pn.deliverypushvalidator.dto.ext.delivery.notification.NotificationRecipientInt;
import it.pagopa.pn.deliverypushvalidator.dto.ext.publicregistry.NationalRegistriesResponse;
import it.pagopa.pn.deliverypushvalidator.dto.timeline.TimelineElementInternal;
import it.pagopa.pn.deliverypushvalidator.dto.timeline.details.DeliveryModeInt;
import it.pagopa.pn.deliverypushvalidator.middleware.externalclient.pnclient.nationalregistries.NationalRegistriesClient;
import it.pagopa.pn.deliverypushvalidator.service.TimelineService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static it.pagopa.pn.deliverypushvalidator.exception.PnDeliveryPushValidatorExceptionCodes.ERROR_CODE_DELIVERYPUSH_INVALID_PHYSICALADDRESS;

class NationalRegistriesClientServiceImplTest {

    @Mock
    private NationalRegistriesClient nationalRegistriesClient;

    @Mock
    private TimelineUtils timelineUtils;

    @Mock
    private TimelineService timelineService;

    private NationalRegistriesServiceImpl service;

    @BeforeEach
    void setUp() {
        nationalRegistriesClient = Mockito.mock(NationalRegistriesClient.class);
        timelineUtils = Mockito.mock(TimelineUtils.class);
        timelineService = Mockito.mock(TimelineService.class);

        service = new NationalRegistriesServiceImpl(nationalRegistriesClient, timelineService, timelineUtils);
    }

    @Test
    void getMultiplePhysicalAddressTestOK() {
        NotificationInt notification = NotificationInt.builder()
                .iun("Example_IUN_1234_Test")
                .recipients(Arrays.asList(
                        NotificationRecipientInt.builder()
                                .taxId("taxId1")
                                .recipientType(RecipientTypeInt.PF)
                                .build(),
                        NotificationRecipientInt.builder()
                                .taxId("taxId2")
                                .recipientType(RecipientTypeInt.PG)
                                .build()
                ))
                .build();

        String eventId = "NATIONAL_REGISTRY_VALIDATION_CALL_Example_IUN_1234_Test";

        List<NationalRegistriesResponse> expectedResponses = getNationalRegistriesResponses();

        Mockito.when(timelineUtils.buildNationalRegistryValidationCall(Mockito.eq(eventId), Mockito.eq(notification), Mockito.anyList(), Mockito.eq(DeliveryModeInt.ANALOG)))
                .thenReturn(new TimelineElementInternal());
        Mockito.when(nationalRegistriesClient.sendRequestForGetPhysicalAddresses(Mockito.any()))
                .thenReturn(expectedResponses);

        List<NationalRegistriesResponse> actualResponses = service.getMultiplePhysicalAddress(notification);

        Assertions.assertEquals(expectedResponses, actualResponses);
        Mockito.verify(timelineService, Mockito.times(1)).addTimelineElement(Mockito.any(), Mockito.eq(notification));
        Mockito.verify(nationalRegistriesClient, Mockito.times(1)).sendRequestForGetPhysicalAddresses(Mockito.any());
    }



    private List<NationalRegistriesResponse> getNationalRegistriesResponses() {
        List<NationalRegistriesResponse> responses = new ArrayList<>();

        NationalRegistriesResponse response1 = new NationalRegistriesResponse();
        response1.toBuilder()
                .recIndex(0)
                .error(null)
                .errorStatus(null)
                .registry("ANPR")
                .physicalAddress(new PhysicalAddressInt(
                        "Galileo Bruno",
                        "Palazzo dell'Inquisizione",
                        "corso Italia 666",
                        "Piano Terra (piatta)",
                        "00100",
                        "Roma",
                        null,
                        "RM",
                        "IT"
                ))
                .build();

        NationalRegistriesResponse response2 = new NationalRegistriesResponse();
        response2.toBuilder()
                .recIndex(1)
                .error(null)
                .errorStatus(null)
                .registry("REGISTRO_IMPRESE")
                .physicalAddress(new PhysicalAddressInt(
                        "Galileo Bruno",
                        "Palazzo dell'Inquisizione",
                        "corso Italia 666",
                        "Piano Terra (piatta)",
                        "00100",
                        "Roma",
                        null,
                        "RM",
                        "IT"
                ))
                .build();

        responses.add(response1);
        responses.add(response2);

        return responses;
    }
}