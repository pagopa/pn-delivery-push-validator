package it.pagopa.pn.deliverypushvalidator.dto.timeline.details;


import it.pagopa.pn.deliverypushvalidator.dto.address.DigitalAddressSourceInt;

public interface DigitalAddressSourceRelatedTimelineElement  extends RecipientRelatedTimelineElementDetails {
    DigitalAddressSourceInt getDigitalAddressSource();
    void setDigitalAddressSource(DigitalAddressSourceInt digitalAddressInt);
}
