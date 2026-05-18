package it.pagopa.pn.deliverypushvalidator.service.mapper;


import io.micrometer.common.util.StringUtils;
import it.pagopa.pn.deliverypushvalidator.dto.address.DigitalAddressInt;
import it.pagopa.pn.deliverypushvalidator.dto.address.PhysicalAddressInt;
import it.pagopa.pn.deliverypushvalidator.dto.ext.datavault.NotificationRecipientAddressesDtoInt;
import it.pagopa.pn.deliverypushvalidator.dto.ext.delivery.notification.NotificationRecipientInt;
import it.pagopa.pn.deliverypushvalidator.generated.openapi.msclient.datavault_reactive.model.*;

import java.util.ArrayList;
import java.util.List;

public class NotificationRecipientAddressesDtoMapper {
    private NotificationRecipientAddressesDtoMapper(){}
    
    public static NotificationRecipientAddressesDto internalToExternal(NotificationRecipientAddressesDtoInt dtoInt) {
        NotificationRecipientAddressesDto dtoExt = new NotificationRecipientAddressesDto();
        dtoExt.setDenomination(dtoInt.getDenomination());
        dtoExt.setDigitalAddress(getAddressDtoFromDigitalAddress(dtoInt.getDigitalAddress()));
        dtoExt.setPhysicalAddress(getAnalogDomicileFromPhysical(dtoInt.getPhysicalAddress()));
        dtoExt.setRecIndex(dtoInt.getRecIndex());
        dtoExt.setEmails(mapEmailToDto(dtoInt.getEmail()));
        dtoExt.setPhoneNumbers(mapPhoneNumberToDto(dtoInt.getPhoneNumber()));
        return dtoExt;
    }

    public static NotificationRecipientAddressesDtoInt buildNotificationRecipientAddressesDtoInt(NotificationRecipientInt recipient, PhysicalAddressInt physicalAddress, Integer recIndex){
        return NotificationRecipientAddressesDtoInt.builder()
                .denomination(recipient.getDenomination())
                .digitalAddress(recipient.getDigitalDomicile())
                .email(recipient.getEmail())
                .phoneNumber(recipient.getPhoneNumber())
                .physicalAddress(physicalAddress)
                .recIndex(recIndex)
                .build();
    }

    private static List<EmailDto> mapEmailToDto(String email) {
        if (StringUtils.isBlank(email)) {
            return null;
        }
        List<EmailDto> list = new ArrayList<>();
        EmailDto emailDto = new EmailDto();
        emailDto.setValue(email);
        list.add(emailDto);
        return list;
    }

    private static List<PhoneNumberDto> mapPhoneNumberToDto(String phoneNumber) {
        if (StringUtils.isBlank(phoneNumber)) {
            return null;
        }
        List<PhoneNumberDto> list = new ArrayList<>();
        PhoneNumberDto phoneNumberDto = new PhoneNumberDto();
        phoneNumberDto.setValue(phoneNumber);
        list.add(phoneNumberDto);
        return list;
    }

    private static AddressDto getAddressDtoFromDigitalAddress(DigitalAddressInt digitalAddressInt){
        AddressDto addressDtoExt = null;
        if(digitalAddressInt != null ){
            addressDtoExt = new AddressDto();
            addressDtoExt.setValue(digitalAddressInt.getAddress());
        }
        return addressDtoExt;
    }

    private static AnalogDomicile getAnalogDomicileFromPhysical(PhysicalAddressInt physicalAddress){
        AnalogDomicile address = null;
        if(physicalAddress != null){
            address = new AnalogDomicile();
            address.setAddress(physicalAddress.getAddress());
            address.setAt(physicalAddress.getAt());
            address.setAddressDetails(physicalAddress.getAddressDetails());
            address.setCap(physicalAddress.getZip());
            address.setMunicipality(physicalAddress.getMunicipality());
            address.setMunicipalityDetails(physicalAddress.getMunicipalityDetails());
            address.setProvince(physicalAddress.getProvince());
            address.setState(physicalAddress.getForeignState());
        }
        return address;
    }

}
