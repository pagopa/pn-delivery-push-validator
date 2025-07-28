package it.pagopa.pn.deliverypushvalidator.dto.timeline.details;


import it.pagopa.pn.deliverypushvalidator.dto.address.PhysicalAddressInt;

public interface NewAddressRelatedTimelineElement extends ConfidentialInformationTimelineElement{
    PhysicalAddressInt getNewAddress();
    void setNewAddress(PhysicalAddressInt digitalAddressInt);
}
