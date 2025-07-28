package it.pagopa.pn.deliverypushvalidator.dto.legalfacts;


import it.pagopa.pn.deliverypushvalidator.dto.address.PhysicalAddressInt;

public interface AarTemplateChooseStrategy {
    AarTemplateType choose(PhysicalAddressInt address);
}
