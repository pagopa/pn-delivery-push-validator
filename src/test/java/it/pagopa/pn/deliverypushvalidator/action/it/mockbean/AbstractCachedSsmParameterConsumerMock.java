package it.pagopa.pn.deliverypushvalidator.action.it.mockbean;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.pagopa.pn.commons.abstractions.ParameterConsumer;
import it.pagopa.pn.commons.exceptions.PnInternalException;

import java.util.Optional;

public class AbstractCachedSsmParameterConsumerMock implements ParameterConsumer {
    private static final String PARAMETER_STORE_MVP_CAMPAIGNS = "MVPCampaigns";
    public static final String CAMPAIGN_ID_DIGITAL_WORKFLOW = "campaign-digital-workflow";
    public static final String CAMPAIGN_ID_ANALOG_WORKFLOW = "campaign-analog-workflow";
    public static final String CAMPAIGN_ID_CLOSED = "campaign-closed";
    public static final String DEFAULT_CAMPAIGN_SENDER_ID = "5b994d4a-0fa8-47ac-9c7b-354f1d44a1ce";
    private static final String JSON_CAMPAIGNS_PARAMETER_CONSUMER = """
            [
                {
                    "campaignId": "campaign-digital-workflow",
                    "senderId": "5b994d4a-0fa8-47ac-9c7b-354f1d44a1ce",
                    "title": "campaign for test with digital workflow",
                    "descriptionScope": "campaign for test",
                    "startDate": "2026-02-01T00:00:00Z",
                    "endDate": "2027-12-31T23:59:59Z",
                    "status": "IN_PROGRESS",
                    "senderContact": "contact@example.com",
                    "serviceId": "service789",
                    "sensitiveContent": true,
                    "stopOnViewed": false,
                    "workflow": [
                        {
                          "channel": "IO",
                          "recipientType": ["PF"],
                          "desiredFeedback": ["READ"],
                          "includeAttachment": false
                        },
                        {
                          "channel": "PEC",
                          "recipientType": ["PG"],
                          "desiredFeedback": ["READ"],
                          "includeAttachment": false
                        }
                    ]
                },
                {
                    "campaignId": "campaign-analog-workflow",
                    "senderId": "5b994d4a-0fa8-47ac-9c7b-354f1d44a1ce",
                    "title": "campaign for test with analog workflow",
                    "descriptionScope": "campaign for test",
                    "startDate": "2026-02-01T00:00:00Z",
                    "endDate": "2027-12-31T23:59:59Z",
                    "status": "IN_PROGRESS",
                    "senderContact": "contact@example.com",
                    "serviceId": "service789",
                    "sensitiveContent": true,
                    "stopOnViewed": false,
                    "workflow": [
                        {
                          "channel": "ANALOG",
                          "recipientType": ["PF", "PG"],
                          "desiredFeedback": ["RECEIVED"],
                          "includeAttachment": false
                        }
                    ]
                },
                {
                    "campaignId": "campaign-closed",
                    "senderId": "5b994d4a-0fa8-47ac-9c7b-354f1d44a1ce",
                    "title": "campaign for test closed",
                    "descriptionScope": "campaign for test",
                    "startDate": "2026-02-01T00:00:00Z",
                    "endDate": "2027-12-31T23:59:59Z",
                    "status": "CONCLUDED",
                    "senderContact": "contact@example.com",
                    "serviceId": "service789",
                    "sensitiveContent": true,
                    "stopOnViewed": false,
                    "workflow": []
                }
            ]""";

    @Override
    public <T> Optional<T> getParameterValue(String storeName, Class<T> aClass) {
        Optional<T> result = Optional.empty();

        if(storeName.startsWith(PARAMETER_STORE_MVP_CAMPAIGNS)){
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());

            try {
                result = Optional.of(objectMapper.readValue(JSON_CAMPAIGNS_PARAMETER_CONSUMER, aClass));
            } catch (JsonProcessingException var7) {
                throw new PnInternalException("[TEST] Unable to deserialize object", "PN_GENERIC_ERROR", var7);
            }
        }
        return result;
    }
}
