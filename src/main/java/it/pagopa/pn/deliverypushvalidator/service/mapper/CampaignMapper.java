package it.pagopa.pn.deliverypushvalidator.service.mapper;

import it.pagopa.pn.commons.db.campaign.entity.CampaignEntity;
import it.pagopa.pn.commons.db.campaign.entity.WorkflowEntity;
import it.pagopa.pn.deliverypushvalidator.dto.campaign.Campaign;
import it.pagopa.pn.deliverypushvalidator.dto.campaign.Channel;
import it.pagopa.pn.deliverypushvalidator.dto.campaign.DesiredFeedback;

import java.util.Collections;
import java.util.Objects;
import java.util.stream.Collectors;

public final class CampaignMapper {

    private CampaignMapper() {
    }

    public static Campaign toInternalCampaign(CampaignEntity campaignEntity) {
        if (campaignEntity == null) {
            return null;
        }

        return Campaign.builder()
                .campaignId(campaignEntity.getCampaignId())
                .senderId(campaignEntity.getSenderId())
                .title(campaignEntity.getTitle())
                .descriptionScope(campaignEntity.getDescriptionScope())
                .startDate(campaignEntity.getStartDate())
                .endDate(campaignEntity.getEndDate())
                .status(campaignEntity.getStatus() == null ? null : it.pagopa.pn.deliverypushvalidator.dto.campaign.CampaignStatus.valueOf(campaignEntity.getStatus().name()))
                .senderContact(campaignEntity.getSenderContact())
                .serviceId(campaignEntity.getServiceId())
                .serviceName(campaignEntity.getServiceName())
                .taxonomyCode(campaignEntity.getTaxonomyCode())
                .sensitiveContent(Boolean.TRUE.equals(campaignEntity.getSensitiveContent()))
                .stopOnViewed(Boolean.TRUE.equals(campaignEntity.getStopOnViewed()))
                .stopOnPaid(Boolean.TRUE.equals(campaignEntity.getStopOnPaid()))
                .workflow(campaignEntity.getWorkflow() == null
                        ? Collections.emptyList()
                        : campaignEntity.getWorkflow().stream()
                        .filter(Objects::nonNull)
                        .map(CampaignMapper::toInternalWorkflowEntity)
                        .toList())
                .build();
    }

    private static it.pagopa.pn.deliverypushvalidator.dto.campaign.WorkflowEntity toInternalWorkflowEntity(WorkflowEntity workflowEntity) {
        return it.pagopa.pn.deliverypushvalidator.dto.campaign.WorkflowEntity.builder()
                .channel(workflowEntity.getChannel() == null ? null : Channel.valueOf(workflowEntity.getChannel().name()))
                .recipientType(workflowEntity.getRecipientType() == null
                        ? Collections.emptySet()
                        : workflowEntity.getRecipientType().stream()
                        .filter(Objects::nonNull)
                        .map(type -> it.pagopa.pn.deliverypushvalidator.dto.ext.datavault.RecipientTypeInt.valueOf(type.name()))
                        .collect(Collectors.toSet()))
                .timeout(workflowEntity.getTimeout())
                .desiredFeedback(workflowEntity.getDesiredFeedback() == null
                        ? Collections.emptySet()
                        : workflowEntity.getDesiredFeedback().stream()
                        .filter(Objects::nonNull)
                        .map(df -> DesiredFeedback.valueOf(df.name()))
                        .collect(Collectors.toSet()))
                .includeAttachment(workflowEntity.getIncludeAttachment())
                .build();
    }
}
