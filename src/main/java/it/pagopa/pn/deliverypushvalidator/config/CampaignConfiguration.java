package it.pagopa.pn.deliverypushvalidator.config;

import it.pagopa.pn.commons.db.campaign.CampaignServiceCachedProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;

@Configuration
public class CampaignConfiguration {

    @Bean
    public CampaignServiceCachedProvider campaignServiceCachedProvider(
            DynamoDbEnhancedClient dynamoDbEnhancedClient,
            @Value("${campaign.table-name}") String tableName) {
        return new CampaignServiceCachedProvider(dynamoDbEnhancedClient, tableName);
    }
}
