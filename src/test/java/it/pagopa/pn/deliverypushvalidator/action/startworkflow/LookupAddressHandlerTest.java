package it.pagopa.pn.deliverypushvalidator.action.startworkflow;


import it.pagopa.pn.deliverypushvalidator.action.utils.TimelineUtils;
import it.pagopa.pn.deliverypushvalidator.dto.address.LegalDigitalAddressInt;
import it.pagopa.pn.deliverypushvalidator.dto.address.PhysicalAddressInt;
import it.pagopa.pn.deliverypushvalidator.dto.ext.datavault.NotificationRecipientAddressesDtoInt;
import it.pagopa.pn.deliverypushvalidator.dto.ext.datavault.RecipientTypeInt;
import it.pagopa.pn.deliverypushvalidator.dto.ext.delivery.notification.NotificationInt;
import it.pagopa.pn.deliverypushvalidator.dto.ext.delivery.notification.NotificationRecipientInt;
import it.pagopa.pn.deliverypushvalidator.dto.ext.publicregistry.NationalRegistriesResponse;
import it.pagopa.pn.deliverypushvalidator.dto.timeline.TimelineElementInternal;
import it.pagopa.pn.deliverypushvalidator.exception.PnLookupAddressValidationFailedException;
import it.pagopa.pn.deliverypushvalidator.service.ConfidentialInformationService;
import it.pagopa.pn.deliverypushvalidator.service.NationalRegistriesService;
import it.pagopa.pn.deliverypushvalidator.service.TimelineService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import reactor.core.publisher.Mono;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LookupAddressHandlerTest {

    @Mock
    private TimelineService timelineService;

    @Mock
    private TimelineUtils timelineUtils;

    @Mock
    private ConfidentialInformationService confidentialInformationService;

    @Mock
    private NationalRegistriesService nationalRegistriesService;

    private LookupAddressHandler lookupAddressHandler;

    @BeforeEach
    void setUp() {
        timelineService = mock(TimelineService.class);
        timelineUtils = mock(TimelineUtils.class);
        confidentialInformationService = mock(ConfidentialInformationService.class);
        nationalRegistriesService = mock(NationalRegistriesService.class);
        lookupAddressHandler = new LookupAddressHandler(timelineService, timelineUtils, confidentialInformationService, nationalRegistriesService);
    }

    @Test
    void performValidation_success() {
        NotificationInt notification = getNotificationWithEmailAndPhone();
        NationalRegistriesResponse response = mock(NationalRegistriesResponse.class);
        when(response.getPhysicalAddress()).thenReturn(getPhysicalAddress());
        when(response.getError()).thenReturn(null);
        when(response.getRecIndex()).thenReturn(0);
        when(nationalRegistriesService.getMultiplePhysicalAddress(notification)).thenReturn(List.of(response));

        TimelineElementInternal timelineElement = mock(TimelineElementInternal.class);
        when(timelineUtils.buildNationalRegistryValidationResponse(notification, response)).thenReturn(timelineElement);

        when(confidentialInformationService.updateNotificationAddresses(Mockito.any(), Mockito.any(), Mockito.any()))
                .thenReturn(Mono.empty());

        lookupAddressHandler.performValidation(notification);

        verify(nationalRegistriesService, times(1)).getMultiplePhysicalAddress(notification);
        verify(timelineService, times(1)).addTimelineElement(timelineElement, notification);
        verify(confidentialInformationService, times(1)).updateNotificationAddresses(Mockito.any(), Mockito.any(), Mockito.any());
    }

    @Test
    void performValidation_success_verifiesEmailAndPhoneAreSaved() {
        // GIVEN
        NotificationInt notification = getNotificationWithEmailAndPhone();
        NationalRegistriesResponse response = mock(NationalRegistriesResponse.class);
        when(response.getPhysicalAddress()).thenReturn(getPhysicalAddress());
        when(response.getError()).thenReturn(null);
        when(response.getRecIndex()).thenReturn(0);
        when(nationalRegistriesService.getMultiplePhysicalAddress(notification)).thenReturn(List.of(response));

        TimelineElementInternal timelineElement = mock(TimelineElementInternal.class);
        when(timelineUtils.buildNationalRegistryValidationResponse(notification, response)).thenReturn(timelineElement);

        when(confidentialInformationService.updateNotificationAddresses(Mockito.any(), Mockito.any(), Mockito.any()))
                .thenReturn(Mono.empty());

        // WHEN
        lookupAddressHandler.performValidation(notification);

        // THEN
        ArgumentCaptor<List<NotificationRecipientAddressesDtoInt>> captor = ArgumentCaptor.forClass(List.class);
        verify(confidentialInformationService, times(1))
                .updateNotificationAddresses(eq(notification.getIun()), eq(false), captor.capture());

        List<NotificationRecipientAddressesDtoInt> capturedList = captor.getValue();
        assertNotNull(capturedList);
        assertEquals(1, capturedList.size());

        NotificationRecipientAddressesDtoInt addressDto = capturedList.get(0);
        assertEquals("test@example.com", addressDto.getEmail());
        assertEquals("+393331234567", addressDto.getPhoneNumber());
        assertEquals("Mario Rossi", addressDto.getDenomination());
        assertNotNull(addressDto.getDigitalAddress());
        assertNotNull(addressDto.getPhysicalAddress());
    }

    @Test
    void performValidation_addressNotFound() {
        // Case ADDRESS_NOT_FOUND
        NotificationInt notification = mock(NotificationInt.class);
        NationalRegistriesResponse response = mock(NationalRegistriesResponse.class);
        when(response.getPhysicalAddress()).thenReturn(null);
        when(response.getError()).thenReturn(null);
        when(nationalRegistriesService.getMultiplePhysicalAddress(notification)).thenReturn(List.of(response));

        assertThrows(PnLookupAddressValidationFailedException.class, () -> lookupAddressHandler.performValidation(notification));
        verify(confidentialInformationService, times(0)).updateNotificationAddresses(Mockito.any(), Mockito.any(), Mockito.any());
        verify(timelineService, times(0)).addTimelineElement(Mockito.any(), Mockito.any());
    }

    @Test
    void performValidation_addressSearchFailed() {
        // Case ADDRESS_SEARCH_FAILED
        NotificationInt notification = mock(NotificationInt.class);
        NationalRegistriesResponse response = mock(NationalRegistriesResponse.class);
        when(response.getPhysicalAddress()).thenReturn(getPhysicalAddress());
        when(response.getError()).thenReturn("Error");
        when(nationalRegistriesService.getMultiplePhysicalAddress(notification)).thenReturn(List.of(response));

        assertThrows(PnLookupAddressValidationFailedException.class, () -> lookupAddressHandler.performValidation(notification));
        verify(confidentialInformationService, times(0)).updateNotificationAddresses(Mockito.any(), Mockito.any(), Mockito.any());
        verify(timelineService, times(0)).addTimelineElement(Mockito.any(), Mockito.any());
    }

    @Test
    void performValidation_success_withNullEmailAndPhone() {
        // GIVEN - recipient senza email e phone
        NotificationInt notification = getNotificationWithoutEmailAndPhone();
        NationalRegistriesResponse response = mock(NationalRegistriesResponse.class);
        when(response.getPhysicalAddress()).thenReturn(getPhysicalAddress());
        when(response.getError()).thenReturn(null);
        when(response.getRecIndex()).thenReturn(0);
        when(nationalRegistriesService.getMultiplePhysicalAddress(notification)).thenReturn(List.of(response));

        TimelineElementInternal timelineElement = mock(TimelineElementInternal.class);
        when(timelineUtils.buildNationalRegistryValidationResponse(notification, response)).thenReturn(timelineElement);

        when(confidentialInformationService.updateNotificationAddresses(Mockito.any(), Mockito.any(), Mockito.any()))
                .thenReturn(Mono.empty());

        // WHEN
        lookupAddressHandler.performValidation(notification);

        // THEN
        ArgumentCaptor<List<NotificationRecipientAddressesDtoInt>> captor = ArgumentCaptor.forClass(List.class);
        verify(confidentialInformationService, times(1))
                .updateNotificationAddresses(eq(notification.getIun()), eq(false), captor.capture());

        List<NotificationRecipientAddressesDtoInt> capturedList = captor.getValue();
        NotificationRecipientAddressesDtoInt addressDto = capturedList.get(0);
        assertNull(addressDto.getEmail());
        assertNull(addressDto.getPhoneNumber());
    }

    private static NotificationInt getNotificationWithEmailAndPhone() {
        LegalDigitalAddressInt digitalDomicile = LegalDigitalAddressInt.builder()
                .type(LegalDigitalAddressInt.LEGAL_DIGITAL_ADDRESS_TYPE.PEC)
                .address("pec@example.com")
                .build();

        List<NotificationRecipientInt> recipients = List.of(
                NotificationRecipientInt.builder()
                        .recipientType(RecipientTypeInt.PF)
                        .taxId("taxId")
                        .denomination("Mario Rossi")
                        .physicalAddress(getPhysicalAddress())
                        .digitalDomicile(digitalDomicile)
                        .email("test@example.com")
                        .phoneNumber("+393331234567")
                        .build()
        );
        return NotificationInt.builder()
                .iun("testIun")
                .recipients(recipients)
                .build();
    }

    private static NotificationInt getNotificationWithoutEmailAndPhone() {
        List<NotificationRecipientInt> recipients = List.of(
                NotificationRecipientInt.builder()
                        .recipientType(RecipientTypeInt.PF)
                        .taxId("taxId")
                        .denomination("Luigi Verdi")
                        .physicalAddress(getPhysicalAddress())
                        .email(null)
                        .phoneNumber(null)
                        .build()
        );
        return NotificationInt.builder()
                .iun("testIun")
                .recipients(recipients)
                .build();
    }

    private static PhysicalAddressInt getPhysicalAddress() {
        return PhysicalAddressInt.builder()
                .addressDetails("addressDetails")
                .zip("zip")
                .municipality("municipality")
                .build();
    }
}