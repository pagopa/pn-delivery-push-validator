package it.pagopa.pn.deliverypushvalidator.config;

import it.pagopa.pn.commons.abstractions.ParameterConsumer;
import it.pagopa.pn.commons.db.campaign.CampaignProvider;
import it.pagopa.pn.commons.db.campaign.entity.CampaignEntity;
import it.pagopa.pn.commons.db.campaign.provider.CampaignServiceCachedProvider;
import it.pagopa.pn.deliverypushvalidator.action.searchaddress.SearchDigitalDomicileConfigProvider;
import it.pagopa.pn.deliverypushvalidator.action.searchaddress.SearchDigitalDomicileParameterConsumer;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;

import java.util.List;

/**
 * Configura i provider applicativi che dipendono da servizi AWS.
 * In base alla presenza della variabile d'ambiente {@code AWS_REGIONCODE},
 * registra i provider reali, che accedono ai servizi AWS, oppure provider
 * sostitutivi per l'ambiente locale, che segnalano con
 * {@link IllegalStateException} l'uso di funzionalità non disponibili.
 */
@Configuration(proxyBeanMethods = false)
@NoArgsConstructor
public class AwsDependentProvidersConfiguration {

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnExpression("'${AWS_REGIONCODE:}'.trim().isEmpty()")
    @Slf4j
    static class MockBeans {

        @Bean
        SearchDigitalDomicileConfigProvider mockSearchDigitalDomicileConfigProvider() {
            log.info("Local AWS environment: SearchDigitalDomicile SSM consumer disabled");
            return () -> {
                throw new IllegalStateException(
                        "SearchDigitalDomicile is not available in the local AWS environment");
            };
        }

        @Bean
        CampaignProvider mockCampaignProvider() {
            log.info("Local AWS environment: campaign provider disabled");
            return new CampaignProvider() {
                @Override
                public List<CampaignEntity> getBySenderId(String senderId) {
                    throw new IllegalStateException(
                            "Campaign is not available in the local AWS environment");
                }

                @Override
                public CampaignEntity getByCampaignIdAndSenderId(
                        String campaignId, String senderId) {
                    throw new IllegalStateException(
                            "Campaign is not available in the local AWS environment");
                }
            };
        }
    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnExpression("!'${AWS_REGIONCODE:}'.trim().isEmpty()")
    @Slf4j
    static class RealBeans {

        @Bean
        SearchDigitalDomicileConfigProvider searchDigitalDomicileConfigProvider(
                ParameterConsumer parameterConsumer) {
            return new SearchDigitalDomicileParameterConsumer(parameterConsumer);
        }

        @Bean
        CampaignProvider campaignProvider(
                DynamoDbEnhancedClient dynamoDbEnhancedClient,
                @Value("${campaign.table-name}") String tableName) {
            return new CampaignServiceCachedProvider(dynamoDbEnhancedClient, tableName);
        }
    }
}