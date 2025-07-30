package it.pagopa.pn.deliverypushvalidator.service;


import it.pagopa.pn.deliverypushvalidator.dto.ext.delivery.notification.NotificationInt;
import it.pagopa.pn.deliverypushvalidator.dto.ext.publicregistry.NationalRegistriesResponse;
import it.pagopa.pn.deliverypushvalidator.dto.nationalregistries.CheckTaxIdOKInt;

import java.util.List;

public interface NationalRegistriesService {

    CheckTaxIdOKInt checkTaxId(String taxId);

    List<NationalRegistriesResponse> getMultiplePhysicalAddress(NotificationInt notification);
}
