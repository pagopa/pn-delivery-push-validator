package it.pagopa.pn.deliverypushvalidator.service;

import it.pagopa.pn.deliverypushvalidator.dto.campaign.Campaign;
import it.pagopa.pn.deliverypushvalidator.dto.ext.delivery.notification.NotificationInt;

public interface CampaignValidator {

    Campaign validateAndGetCampaign(NotificationInt notification);
}
