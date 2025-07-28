package it.pagopa.pn.deliverypushvalidator.dto.timeline.details;


import it.pagopa.pn.deliverypushvalidator.dto.address.CourtesyDigitalAddressInt;

public interface CourtesyAddressRelatedTimelineElement extends ConfidentialInformationTimelineElement{
    CourtesyDigitalAddressInt getDigitalAddress();
    void setDigitalAddress(CourtesyDigitalAddressInt digitalAddressInt);
}
