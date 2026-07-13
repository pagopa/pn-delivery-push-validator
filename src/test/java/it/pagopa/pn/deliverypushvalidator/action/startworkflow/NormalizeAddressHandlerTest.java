package it.pagopa.pn.deliverypushvalidator.action.startworkflow;

import it.pagopa.pn.deliverypushvalidator.action.it.utils.TestUtils;
import it.pagopa.pn.deliverypushvalidator.action.utils.NotificationUtils;
import it.pagopa.pn.deliverypushvalidator.action.utils.TimelineUtils;
import it.pagopa.pn.deliverypushvalidator.dto.address.LegalDigitalAddressInt;
import it.pagopa.pn.deliverypushvalidator.dto.address.PhysicalAddressInt;
import it.pagopa.pn.deliverypushvalidator.dto.ext.addressmanager.NormalizeItemsResultInt;
import it.pagopa.pn.deliverypushvalidator.dto.ext.addressmanager.NormalizeResultInt;
import it.pagopa.pn.deliverypushvalidator.dto.ext.datavault.NotificationRecipientAddressesDtoInt;
import it.pagopa.pn.deliverypushvalidator.dto.ext.datavault.RecipientTypeInt;
import it.pagopa.pn.deliverypushvalidator.dto.ext.delivery.notification.NotificationInt;
import it.pagopa.pn.deliverypushvalidator.dto.ext.delivery.notification.NotificationPaymentInfoInt;
import it.pagopa.pn.deliverypushvalidator.dto.ext.delivery.notification.NotificationRecipientInt;
import it.pagopa.pn.deliverypushvalidator.service.ConfidentialInformationService;
import it.pagopa.pn.deliverypushvalidator.service.TimelineService;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class NormalizeAddressHandlerTest {

    @Mock
    private TimelineService timelineService;
    @Mock
    private NotificationUtils notificationUtils;
    @Mock
    private TimelineUtils timelineUtils;
    @Mock
    private ConfidentialInformationService confidentialInformationService;
    private NormalizeAddressHandler normalizeAddressHandler;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        normalizeAddressHandler = new NormalizeAddressHandler(timelineService, notificationUtils, timelineUtils,confidentialInformationService);
    }

    @Test
    void testHandleNormalizedAddressResponse() {

        //GIVEN
        NormalizeItemsResultInt normalizeItemsResult = NormalizeItemsResultInt.builder()
                .correlationId("testCorrId")
                .resultItems(getNormalizeResultIntUnsortedList())
                .build();

        NotificationInt notification = TestUtils.getNotificationMultiRecipient();
        NotificationRecipientInt notificationRecipientInt =
                new NotificationRecipientInt("","","",new LegalDigitalAddressInt(),
                        new PhysicalAddressInt("","","","","","","","",""),
                        List.of(new NotificationPaymentInfoInt()), RecipientTypeInt.PF, "nomecognome@emeail.it","+390000000000", "",null);
        when(notificationUtils.getRecipientFromIndex(Mockito.any(),Mockito.anyInt())).thenReturn(notificationRecipientInt);

        when(confidentialInformationService.updateNotificationAddresses(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(Mono.empty());

        normalizeAddressHandler.handleNormalizedAddressResponse(notification, normalizeItemsResult);

        //WHEN
        ArgumentCaptor<List<NotificationRecipientAddressesDtoInt>> captor = ArgumentCaptor.forClass(List.class);
        verify(confidentialInformationService).updateNotificationAddresses(Mockito.any(),Mockito.any(), captor.capture());
        List<NotificationRecipientAddressesDtoInt> capturedList = captor.getValue();

        //THEN
        PhysicalAddressInt capturedPhysicalAddressAtPosition0 = capturedList.getFirst().getPhysicalAddress();
        PhysicalAddressInt originPhysicalAddressAtPosition1 = getNormalizeResultIntUnsortedList().get(1).getNormalizedAddress();
        Assertions.assertEquals(capturedPhysicalAddressAtPosition0.getAddressDetails(), originPhysicalAddressAtPosition1.getAddressDetails());
        Assertions.assertEquals(capturedPhysicalAddressAtPosition0.getZip(), originPhysicalAddressAtPosition1.getZip());
        Assertions.assertEquals(capturedPhysicalAddressAtPosition0.getMunicipality(), originPhysicalAddressAtPosition1.getMunicipality());
        Assertions.assertEquals(capturedPhysicalAddressAtPosition0.getMunicipalityDetails(), originPhysicalAddressAtPosition1.getMunicipalityDetails());
        Assertions.assertEquals(capturedPhysicalAddressAtPosition0.getProvince(), originPhysicalAddressAtPosition1.getProvince());

        PhysicalAddressInt capturedPhysicalAddressAtPosition1 = capturedList.get(1).getPhysicalAddress();
        PhysicalAddressInt originPhysicalAddressAtPosition0 = getNormalizeResultIntUnsortedList().getFirst().getNormalizedAddress();
        Assertions.assertEquals(capturedPhysicalAddressAtPosition1.getAddressDetails(), originPhysicalAddressAtPosition0.getAddressDetails());
        Assertions.assertEquals(capturedPhysicalAddressAtPosition1.getZip(), originPhysicalAddressAtPosition0.getZip());
        Assertions.assertEquals(capturedPhysicalAddressAtPosition1.getMunicipality(), originPhysicalAddressAtPosition0.getMunicipality());
        Assertions.assertEquals(capturedPhysicalAddressAtPosition1.getMunicipalityDetails(), originPhysicalAddressAtPosition0.getMunicipalityDetails());
        Assertions.assertEquals(capturedPhysicalAddressAtPosition1.getProvince(), originPhysicalAddressAtPosition0.getProvince());
    }



    @NotNull
    private static List<NormalizeResultInt> getNormalizeResultIntUnsortedList() {
        List<NormalizeResultInt> listNormResult = new ArrayList<>();
        NormalizeResultInt result1 = NormalizeResultInt.builder()
                .normalizedAddress(PhysicalAddressInt.builder()
                        .addressDetails("001")
                        .foreignState("002")
                        .at("003")
                        .province("004")
                        .municipality("005")
                        .zip("006")
                        .municipalityDetails("007")
                        .build())
                .id("1")
                .build();
        listNormResult.add(result1);

        NormalizeResultInt result2 = NormalizeResultInt.builder()
                .normalizedAddress(PhysicalAddressInt.builder()
                        .addressDetails("002")
                        .foreignState("003")
                        .at("004")
                        .province("005")
                        .municipality("006")
                        .zip("007")
                        .municipalityDetails("008")
                        .build())
                .id("0")
                .build();
        listNormResult.add(result2);
        return listNormResult;
    }
}
