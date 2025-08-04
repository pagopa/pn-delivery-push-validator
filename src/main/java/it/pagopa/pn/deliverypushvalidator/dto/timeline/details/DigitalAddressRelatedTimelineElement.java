package it.pagopa.pn.deliverypushvalidator.dto.timeline.details;


import it.pagopa.pn.deliverypushvalidator.dto.address.LegalDigitalAddressInt;

public interface DigitalAddressRelatedTimelineElement extends ConfidentialInformationTimelineElement{
    LegalDigitalAddressInt getDigitalAddress();
    void setDigitalAddress(LegalDigitalAddressInt digitalAddressInt);
}
