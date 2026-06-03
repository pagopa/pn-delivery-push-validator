package it.pagopa.pn.deliverypushvalidator.service;

import it.pagopa.pn.deliverypushvalidator.dto.campaign.Campaign;

public interface CampaignService {

    Campaign getCampaignByCampaignIdAndSenderId(String campaignId, String senderId );
}
