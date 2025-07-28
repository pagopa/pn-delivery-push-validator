package it.pagopa.pn.deliverypushvalidator.service;


import it.pagopa.pn.deliverypushvalidator.dto.delivery.notification.NotificationInt;
import it.pagopa.pn.deliverypushvalidator.dto.ext.publicregistry.NationalRegistriesResponse;
import it.pagopa.pn.deliverypushvalidator.dto.nationalregistries.CheckTaxIdOKInt;
import it.pagopa.pn.deliverypushvalidator.dto.timeline.details.ContactPhaseInt;

import java.util.List;

public interface NationalRegistriesService {
    void sendRequestForGetDigitalGeneralAddress(NotificationInt notification,
                                                Integer recIndex,
                                                ContactPhaseInt contactPhase,
                                                int sentAttemptMade,
                                                String relatedFeedbackTimelineId);

    CheckTaxIdOKInt checkTaxId(String taxId);

    List<NationalRegistriesResponse> getMultiplePhysicalAddress(NotificationInt notification);
}
