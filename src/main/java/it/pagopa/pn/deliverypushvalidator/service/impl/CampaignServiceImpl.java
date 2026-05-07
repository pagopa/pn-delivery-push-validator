package it.pagopa.pn.deliverypushvalidator.service.impl;

import it.pagopa.pn.deliverypushvalidator.config.MVPCampaignsParameterConsumer;
import it.pagopa.pn.deliverypushvalidator.dto.campaign.Campaign;
import it.pagopa.pn.deliverypushvalidator.service.CampaignService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class CampaignServiceImpl implements CampaignService {

    private final MVPCampaignsParameterConsumer mvpCampaignsParameterConsumer;

    public Campaign getCampaignByCampaignIdAndSenderId(String campaignId, String senderId ) {
        log.debug("Start getCampaignByCampaignIdAndSenderId for campaignId={} and senderId={}", campaignId, senderId);
        return mvpCampaignsParameterConsumer.getCampaignByCampaignIdAndSenderId(campaignId, senderId);
    }
}
