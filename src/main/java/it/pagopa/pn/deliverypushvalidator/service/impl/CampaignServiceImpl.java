package it.pagopa.pn.deliverypushvalidator.service.impl;

import it.pagopa.pn.commons.db.campaign.CampaignProvider;
import it.pagopa.pn.commons.db.campaign.entity.CampaignEntity;
import it.pagopa.pn.commons.exceptions.PnCampaignNotFoundException;
import it.pagopa.pn.deliverypushvalidator.dto.campaign.Campaign;
import it.pagopa.pn.deliverypushvalidator.service.CampaignService;
import it.pagopa.pn.deliverypushvalidator.service.mapper.CampaignMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class CampaignServiceImpl implements CampaignService {

    private final CampaignProvider campaignServiceCachedProvider;
    @Override
    public Campaign getCampaignByCampaignIdAndSenderId(String campaignId, String senderId) {
        log.debug("Start getCampaignByCampaignIdAndSenderId for campaignId={} and senderId={}", campaignId, senderId);
        try {
            CampaignEntity campaignEntity = campaignServiceCachedProvider.getByCampaignIdAndSenderId(campaignId, senderId);
            return CampaignMapper.toInternalCampaign(campaignEntity);
        } catch (PnCampaignNotFoundException ex) {
            throw new it.pagopa.pn.deliverypushvalidator.exception.PnCampaignNotFoundException(
                    ex.getMessage(),
                    ex.getProblem().getDetail()
            );
        }
    }
}
