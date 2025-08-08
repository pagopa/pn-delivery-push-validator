package it.pagopa.pn.deliverypushvalidator.config.springbootcfg;

import io.awspring.cloud.sqs.config.SqsMessageListenerContainerFactory;
import io.awspring.cloud.sqs.operations.SqsTemplate;
import io.micrometer.observation.ObservationRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;

@Configuration
public class SqsTracingConfig {
    @Bean
    SqsTemplate sqsTemplate(SqsAsyncClient sqsAsyncClient, ObservationRegistry observationRegistry) {
        return SqsTemplate.builder()
                .sqsAsyncClient(sqsAsyncClient)
                .configure(options -> options.observationRegistry(observationRegistry))
                .build();
    }

    @Bean
    SqsMessageListenerContainerFactory<Object> sqsListenerContainerFactory(SqsAsyncClient sqsAsyncClient, ObservationRegistry observationRegistry) {
        return SqsMessageListenerContainerFactory.builder()
                .sqsAsyncClient(sqsAsyncClient)
                .configure(options -> options.observationRegistry(observationRegistry))
                .build();
    }
}
