package it.pagopa.pn.deliverypushvalidator.service;

import it.pagopa.pn.deliverypushvalidator.dto.address.DigitalAddressInt;
import it.pagopa.pn.deliverypushvalidator.dto.address.PhysicalAddressInt;
import it.pagopa.pn.deliverypushvalidator.dto.ext.datavault.NotificationRecipientAddressesDtoInt;
import it.pagopa.pn.deliverypushvalidator.generated.openapi.msclient.datavault_reactive.model.AddressDto;
import it.pagopa.pn.deliverypushvalidator.generated.openapi.msclient.datavault_reactive.model.AnalogDomicile;
import it.pagopa.pn.deliverypushvalidator.generated.openapi.msclient.datavault_reactive.model.NotificationRecipientAddressesDto;
import it.pagopa.pn.deliverypushvalidator.it.utils.PhysicalAddressBuilder;
import it.pagopa.pn.deliverypushvalidator.middleware.externalclient.pnclient.datavault.PnDataVaultClientReactive;
import it.pagopa.pn.deliverypushvalidator.service.impl.ConfidentialInformationServiceImpl;
import it.pagopa.pn.deliverypushvalidator.service.mapper.NotificationRecipientAddressesDtoMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.core.publisher.Mono;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ConfidentialInformationServiceImplTest {
    private ConfidentialInformationService confidentialInformationService;
    private PnDataVaultClientReactive pnDataVaultClientReactive;
    
    @BeforeEach
    void setup() {
        pnDataVaultClientReactive = Mockito.mock( PnDataVaultClientReactive.class );
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

        DigitalAddressInt digitalAddressInt= Mockito.mock(DigitalAddressInt.class);


        NotificationRecipientAddressesDtoInt notificationRecipientAddressesDtoInt = NotificationRecipientAddressesDtoInt.builder()
                .physicalAddress(paPhysicalAddress1)
                .digitalAddress(digitalAddressInt)
                .recIndex(1)
                .denomination("denomination")
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

        NotificationRecipientAddressesDto notificationRecipientAddressesDto = NotificationRecipientAddressesDto.builder()
                .physicalAddress(analogDomicile)
                .digitalAddress(addressDto)
                .denomination("denomination")
                .recIndex(1)
                .build();

        List<NotificationRecipientAddressesDtoInt> inputList = List.of(notificationRecipientAddressesDtoInt);

        when(NotificationRecipientAddressesDtoMapper.internalToExternal(any()))
                .thenReturn(new NotificationRecipientAddressesDto());

        when(pnDataVaultClientReactive.updateNotificationAddressesByIun(eq(iun), eq(normalizer), anyList()))
                .thenReturn(Mono.empty());

        confidentialInformationService.updateNotificationAddresses(iun, normalizer, inputList);

        verify(pnDataVaultClientReactive, times(1))
                .updateNotificationAddressesByIun(eq(iun), eq(normalizer), anyList());
    }

}