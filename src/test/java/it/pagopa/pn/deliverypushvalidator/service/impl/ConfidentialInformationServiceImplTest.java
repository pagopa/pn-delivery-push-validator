package it.pagopa.pn.deliverypushvalidator.service.impl;

import it.pagopa.pn.deliverypushvalidator.action.it.utils.PhysicalAddressBuilder;
import it.pagopa.pn.deliverypushvalidator.dto.address.DigitalAddressInt;
import it.pagopa.pn.deliverypushvalidator.dto.address.LegalDigitalAddressInt;
import it.pagopa.pn.deliverypushvalidator.dto.address.PhysicalAddressInt;
import it.pagopa.pn.deliverypushvalidator.dto.ext.datavault.NotificationRecipientAddressesDtoInt;
import it.pagopa.pn.deliverypushvalidator.dto.ext.delivery.notification.NotificationRecipientInt;
import it.pagopa.pn.deliverypushvalidator.generated.openapi.msclient.datavault_reactive.model.NotificationRecipientAddressesDto;
import it.pagopa.pn.deliverypushvalidator.middleware.externalclient.pnclient.datavault.PnDataVaultClientReactive;
import it.pagopa.pn.deliverypushvalidator.service.ConfidentialInformationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import reactor.core.publisher.Mono;

import java.util.List;

import static it.pagopa.pn.deliverypushvalidator.service.mapper.NotificationRecipientAddressesDtoMapper.buildNotificationRecipientAddressesDtoInt;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class ConfidentialInformationServiceImplTest {
    private ConfidentialInformationService confidentialInformationService;
    private PnDataVaultClientReactive pnDataVaultClientReactive;
    
    @BeforeEach
    void setup() {
        pnDataVaultClientReactive = mock( PnDataVaultClientReactive.class );
        confidentialInformationService = new ConfidentialInformationServiceImpl(
                pnDataVaultClientReactive);
    }

    @Test
    void testUpdateNotificationAddresses(){
        String iun = "testIun";
        Boolean normalizer = true;

        PhysicalAddressInt paPhysicalAddress1 = PhysicalAddressBuilder.builder()
                .withAddress(" Via Nuova 1")
                .build();

        DigitalAddressInt digitalAddressInt = mock(DigitalAddressInt.class);

        NotificationRecipientAddressesDtoInt notificationRecipientAddressesDtoInt = NotificationRecipientAddressesDtoInt.builder()
                .physicalAddress(paPhysicalAddress1)
                .digitalAddress(digitalAddressInt)
                .recIndex(1)
                .denomination("denomination")
                .email("test@example.com")
                .phoneNumber("+393331234567")
                .build();

        List<NotificationRecipientAddressesDtoInt> inputList = List.of(notificationRecipientAddressesDtoInt);

        when(pnDataVaultClientReactive.updateNotificationAddressesByIun(eq(iun), eq(normalizer), anyList()))
                .thenReturn(Mono.empty());

        // WHEN
        confidentialInformationService.updateNotificationAddresses(iun, normalizer, inputList);


        ArgumentCaptor<List> captor =
                ArgumentCaptor.forClass(List.class);

        verify(pnDataVaultClientReactive, times(1))
                .updateNotificationAddressesByIun(eq(iun), eq(normalizer), captor.capture());

        List<NotificationRecipientAddressesDto> capturedList = captor.getValue();
        assertNotNull(capturedList);
        assertEquals(1, capturedList.size());

        NotificationRecipientAddressesDto capturedDto = capturedList.getFirst();
        assertEquals("denomination", capturedDto.getDenomination());
        assertEquals(1, capturedDto.getRecIndex());

        // Verify email mapping
        assertNotNull(capturedDto.getEmails());
        assertEquals(1, capturedDto.getEmails().size());
        assertEquals("test@example.com", capturedDto.getEmails().getFirst().getValue());

        // Verify phone number mapping
        assertNotNull(capturedDto.getPhoneNumbers());
        assertEquals(1, capturedDto.getPhoneNumbers().size());
        assertEquals("+393331234567", capturedDto.getPhoneNumbers().getFirst().getValue());

        // Verify other fields
        assertNotNull(capturedDto.getPhysicalAddress());
        assertNotNull(capturedDto.getDigitalAddress());
    }

    @Test
    void testBuildNotificationRecipientAddressesDtoInt_withAllFields() {
        // GIVEN
        PhysicalAddressInt physicalAddress = PhysicalAddressBuilder.builder()
                .withAddress("Via Roma 1")
                .build();

        LegalDigitalAddressInt digitalDomicile = LegalDigitalAddressInt.builder()
                .type(LegalDigitalAddressInt.LEGAL_DIGITAL_ADDRESS_TYPE.PEC)
                .address("test@pec.it")
                .build();

        NotificationRecipientInt recipient = NotificationRecipientInt.builder()
                .denomination("Mario Rossi")
                .digitalDomicile(digitalDomicile)
                .email("mario.rossi@example.com")
                .phoneNumber("+393331234567")
                .build();

        Integer recIndex = 0;

        // WHEN
        NotificationRecipientAddressesDtoInt result = buildNotificationRecipientAddressesDtoInt(
                recipient,
                physicalAddress,
                recIndex
        );

        // THEN
        assertNotNull(result);
        assertEquals("Mario Rossi", result.getDenomination());
        assertEquals(digitalDomicile, result.getDigitalAddress());
        assertEquals("mario.rossi@example.com", result.getEmail());
        assertEquals("+393331234567", result.getPhoneNumber());
        assertEquals(physicalAddress, result.getPhysicalAddress());
        assertEquals(0, result.getRecIndex());
    }

    @Test
    void testBuildNotificationRecipientAddressesDtoInt_withNullEmailAndPhone() {
        // GIVEN
        PhysicalAddressInt physicalAddress = PhysicalAddressBuilder.builder()
                .withAddress("Via Roma 1")
                .build();

        NotificationRecipientInt recipient = NotificationRecipientInt.builder()
                .denomination("Mario Rossi")
                .digitalDomicile(null)
                .email(null)
                .phoneNumber(null)
                .build();

        Integer recIndex = 1;

        // WHEN
        NotificationRecipientAddressesDtoInt result = buildNotificationRecipientAddressesDtoInt(
                recipient,
                physicalAddress,
                recIndex
        );

        // THEN
        assertNotNull(result);
        assertEquals("Mario Rossi", result.getDenomination());
        assertNull(result.getDigitalAddress());
        assertNull(result.getEmail());
        assertNull(result.getPhoneNumber());
        assertEquals(physicalAddress, result.getPhysicalAddress());
        assertEquals(1, result.getRecIndex());
    }

    @Test
    void testBuildNotificationRecipientAddressesDtoInt_withOnlyEmail() {
        // GIVEN
        PhysicalAddressInt physicalAddress = PhysicalAddressBuilder.builder()
                .withAddress("Via Roma 1")
                .build();

        NotificationRecipientInt recipient = NotificationRecipientInt.builder()
                .denomination("Luigi Verdi")
                .email("luigi.verdi@example.com")
                .phoneNumber(null)
                .build();

        Integer recIndex = 2;

        // WHEN
        NotificationRecipientAddressesDtoInt result = buildNotificationRecipientAddressesDtoInt(
                recipient,
                physicalAddress,
                recIndex
        );

        // THEN
        assertNotNull(result);
        assertEquals("Luigi Verdi", result.getDenomination());
        assertEquals("luigi.verdi@example.com", result.getEmail());
        assertNull(result.getPhoneNumber());
        assertEquals(physicalAddress, result.getPhysicalAddress());
        assertEquals(2, result.getRecIndex());
    }

    @Test
    void testBuildNotificationRecipientAddressesDtoInt_withOnlyPhoneNumber() {
        // GIVEN
        PhysicalAddressInt physicalAddress = PhysicalAddressBuilder.builder()
                .withAddress("Via Roma 1")
                .build();

        NotificationRecipientInt recipient = NotificationRecipientInt.builder()
                .denomination("Anna Bianchi")
                .email(null)
                .phoneNumber("+393339876543")
                .build();

        Integer recIndex = 3;

        // WHEN
        NotificationRecipientAddressesDtoInt result = buildNotificationRecipientAddressesDtoInt(
                recipient,
                physicalAddress,
                recIndex
        );

        // THEN
        assertNotNull(result);
        assertEquals("Anna Bianchi", result.getDenomination());
        assertNull(result.getEmail());
        assertEquals("+393339876543", result.getPhoneNumber());
        assertEquals(physicalAddress, result.getPhysicalAddress());
        assertEquals(3, result.getRecIndex());
    }
}