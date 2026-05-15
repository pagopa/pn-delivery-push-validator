package it.pagopa.pn.deliverypushvalidator.service.impl;

import it.pagopa.pn.deliverypushvalidator.action.it.utils.PhysicalAddressBuilder;
import it.pagopa.pn.deliverypushvalidator.dto.address.DigitalAddressInt;
import it.pagopa.pn.deliverypushvalidator.dto.address.LegalDigitalAddressInt;
import it.pagopa.pn.deliverypushvalidator.dto.address.PhysicalAddressInt;
import it.pagopa.pn.deliverypushvalidator.dto.ext.datavault.NotificationRecipientAddressesDtoInt;
import it.pagopa.pn.deliverypushvalidator.dto.ext.delivery.notification.NotificationRecipientInt;
import it.pagopa.pn.deliverypushvalidator.generated.openapi.msclient.datavault_reactive.model.*;
import it.pagopa.pn.deliverypushvalidator.middleware.externalclient.pnclient.datavault.PnDataVaultClientReactive;
import it.pagopa.pn.deliverypushvalidator.service.ConfidentialInformationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import java.util.List;

import static it.pagopa.pn.deliverypushvalidator.service.impl.ConfidentialInformationServiceImpl.buildNotificationRecipientAddressesDtoInt;
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

        DigitalAddressInt digitalAddressInt= mock(DigitalAddressInt.class);


        NotificationRecipientAddressesDtoInt notificationRecipientAddressesDtoInt = NotificationRecipientAddressesDtoInt.builder()
                .physicalAddress(paPhysicalAddress1)
                .digitalAddress(digitalAddressInt)
                .recIndex(1)
                .denomination("denomination")
                .email("test@example.com")
                .phoneNumber("+393331234567")
                .build();

        AddressDto addressDto = new AddressDto();
        addressDto.setValue("via via");

        AnalogDomicile analogDomicile = new AnalogDomicile();
        analogDomicile.setAddress("via via");
        analogDomicile.setAt("at");
        analogDomicile.setAddressDetails("details");
        analogDomicile.setCap("80000");
        analogDomicile.setMunicipality("mun");
        analogDomicile.setMunicipalityDetails("mun mun");
        analogDomicile.setProvince("NA");
        analogDomicile.setState("It");

        EmailDto emailDto = new EmailDto();
        emailDto.setValue("test@example.com");

        PhoneNumberDto phoneNumberDto = new PhoneNumberDto();
        phoneNumberDto.setValue("+393331234567");

        NotificationRecipientAddressesDto notificationRecipientAddressesDto = NotificationRecipientAddressesDto.builder()
                .physicalAddress(analogDomicile)
                .digitalAddress(addressDto)
                .denomination("denomination")
                .recIndex(1)
                .emails(List.of(emailDto))
                .phoneNumbers(List.of(phoneNumberDto))
                .build();

        List<NotificationRecipientAddressesDtoInt> inputList = List.of(notificationRecipientAddressesDtoInt);


        when(pnDataVaultClientReactive.updateNotificationAddressesByIun(eq(iun), eq(normalizer), anyList()))
                .thenReturn(Mono.empty());

        confidentialInformationService.updateNotificationAddresses(iun, normalizer, inputList);

        verify(pnDataVaultClientReactive, times(1))
                .updateNotificationAddressesByIun(eq(iun), eq(normalizer), anyList());
        assertEquals(analogDomicile, notificationRecipientAddressesDto.getPhysicalAddress());
        assertEquals(addressDto, notificationRecipientAddressesDto.getDigitalAddress());
        assertEquals("denomination", notificationRecipientAddressesDto.getDenomination());
        assertEquals(1, notificationRecipientAddressesDto.getRecIndex());
        assertEquals("test@example.com", notificationRecipientAddressesDto.getEmails() != null ? notificationRecipientAddressesDto.getEmails().getFirst().getValue() : null);
        assertEquals("+393331234567", notificationRecipientAddressesDto.getPhoneNumbers() != null ? notificationRecipientAddressesDto.getPhoneNumbers().getFirst().getValue() : null);
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