#!/bin/bash

echo "### CREATE QUEUES ###"

queues="local-delivery-push-inputs.fifo local-safe-storage-inputs local-validation-actions-inputs local-address-manager-inputs local-f24-inputs local-pn-notification-cost-to-delivery-push-validator-inputs local-informal-validation-inputs"

for qn in  $( echo $queues | tr " " "\n" ) ; do

    echo creating queue $qn ...

    aws --profile default --region us-east-1 --endpoint-url http://localstack:4566 \
        sqs create-queue \
        --attributes '{"DelaySeconds":"2"}' \
        --queue-name $qn

done

echo " - Create pn-delivery-push-validator TABLE"
aws --profile default --region us-east-1 --endpoint-url=http://localstack:4566 \
    dynamodb create-table \
    --table-name DocumentCreationRequest \
    --attribute-definitions \
        AttributeName=key,AttributeType=S \
    --key-schema \
        AttributeName=key,KeyType=HASH \
    --provisioned-throughput \
        ReadCapacityUnits=10,WriteCapacityUnits=5

aws --profile default --region us-east-1 --endpoint-url=http://localstack:4566 \
    ssm put-parameter \
    --name "MVPCampaigns" \
    --type String \
    --value "[
                 {
                   \"campaignId\": \"campaign-1\",
                   \"senderId\": \"5b994d4a-0fa8-47ac-9c7b-354f1d44a1ce\",
                   \"title\": \"First campaign\",
                   \"descriptionScope\": \"Test campaign with 2 defaults messages\",
                   \"startDate\": \"2026-02-01T00:00:00Z\",
                   \"endDate\": \"2026-12-31T23:59:59Z\",
                   \"closed\": false,
                   \"senderContact\": \"contact@example.com\",
                   \"serviceId\": \"service789\",
                   \"messages\": [
                     {
                       \"additionalLanguage\": \"FR\",
                       \"primaryLanguage\": \"IT\",
                       \"messageId\": \"be59736d-7d2d-4b16-bf8f-b4f735788519\"
                     },
                     {
                       \"additionalLanguage\": \"DE\",
                       \"primaryLanguage\": \"IT\",
                       \"messageId\": \"49cf74bc-6048-46be-83a7-e498e0a581c1\"
                     }
                   ],
                   \"sensitiveContent\": true,
                   \"stopOnViewed\": false,
                   \"workflow\": []
                 },
                 {
                   \"campaignId\": \"campaign-2\",
                   \"senderId\": \"5b994d4a-0fa8-47ac-9c7b-354f1d44a1ce\",
                   \"title\": \"Second campaign\",
                   \"descriptionScope\": \"Test campaign with 1 defaults messages\",
                   \"startDate\": \"2026-02-01T00:00:00Z\",
                   \"endDate\": \"2026-12-31T23:59:59Z\",
                   \"closed\": false,
                   \"senderContact\": \"contact@example.com\",
                   \"serviceId\": \"service789\",
                   \"messages\": [
                     {
                       \"primaryLanguage\": \"IT\",
                       \"messageId\": \"42a4b389-b9ee-497a-9908-1c486bce2cce\"
                     }
                   ],
                   \"sensitiveContent\": true,
                   \"stopOnViewed\": false,
                   \"workflow\": []
                 }
             ]"

   aws --profile default --region us-east-1 --endpoint-url=http://localstack:4566 \
       dynamodb create-table \
       --table-name pn-Campaigns \
       --attribute-definitions \
           AttributeName=senderId,AttributeType=S \
           AttributeName=campaignId,AttributeType=S \
       --key-schema \
           AttributeName=senderId,KeyType=HASH \
           AttributeName=campaignId,KeyType=RANGE \
       --provisioned-throughput \
           ReadCapacityUnits=10,WriteCapacityUnits=5

   echo "### INSERT TEST CAMPAIGNS - OK CASES ###"

   Campaigns_ok=(
       # Case 1: Campagna completa con workflow IO
       '{"senderId": {"S": "550e8400-e29b-41d4-a716-446655440001"}, "campaignId": {"S": "camp-001-io"}, "title": {"S": "Avviso Notifica IO"}, "descriptionScope": {"S": "Avviso importante tramite IO"}, "status": {"S": "IN_PROGRESS"}, "serviceId": {"S": "svc-001"}, "serviceName": {"S": "Notifiche"}, "senderContact": {"S": "notifiche@example.com"}, "sensitiveContent": {"BOOL": true}, "stopOnViewed": {"BOOL": true}, "startDate": {"S": "2024-01-01T00:00:00Z"}, "endDate": {"S": "2025-12-31T23:59:59Z"}, "taxonomyCode": {"S": "TAX-001"}, "workflow": {"L": [{"M": {"channel": {"S": "IO"}, "recipientType": {"SS": ["PF", "PG"]}, "timeout": {"S": "PT24H"}, "desiredFeedback": {"SS": ["READ", "RECEIVED"]}, "includeAttachment": {"BOOL": true}}}]}}'

       # Case 2: Campagna EMAIL
       '{"senderId": {"S": "550e8400-e29b-41d4-a716-446655440001"}, "campaignId": {"S": "camp-002-email"}, "title": {"S": "Campagna Newsletter"}, "descriptionScope": {"S": "Newsletter mensile"}, "status": {"S": "DRAFT"}, "serviceId": {"S": "svc-001"}, "serviceName": {"S": "Marketing"}, "senderContact": {"S": "marketing@example.com"}, "sensitiveContent": {"BOOL": false}, "stopOnViewed": {"BOOL": false}, "startDate": {"S": "2025-03-01T00:00:00Z"}, "endDate": {"S": "2025-03-08T23:59:59Z"}, "taxonomyCode": {"S": "TAX-002"}, "workflow": {"L": [{"M": {"channel": {"S": "EMAIL"}, "recipientType": {"SS": ["PF"]}, "timeout": {"S": "PT48H"}, "desiredFeedback": {"SS": ["SENT", "RECEIVED"]}, "includeAttachment": {"BOOL": false}}}]}}'

       # Case 3: Campagna Multi-Canale (IO + SMS + EMAIL)
       '{"senderId": {"S": "550e8400-e29b-41d4-a716-446655440002"}, "campaignId": {"S": "camp-003-multi"}, "title": {"S": "Campagna Multi-Canale"}, "descriptionScope": {"S": "Comunicazione su più canali"}, "status": {"S": "IN_PROGRESS"}, "serviceId": {"S": "svc-002"}, "serviceName": {"S": "Comunicazioni"}, "senderContact": {"S": "comms@example.com"}, "sensitiveContent": {"BOOL": false}, "stopOnViewed": {"BOOL": false}, "startDate": {"S": "2024-02-01T00:00:00Z"}, "endDate": {"S": "2024-02-02T00:00:00Z"}, "taxonomyCode": {"S": "TAX-003"}, "workflow": {"L": [{"M": {"channel": {"S": "IO"}, "recipientType": {"SS": ["PF"]}, "timeout": {"S": "PT12H"}, "desiredFeedback": {"SS": ["READ"]}, "includeAttachment": {"BOOL": true}}}, {"M": {"channel": {"S": "SMS"}, "recipientType": {"SS": ["PF", "PG"]}, "timeout": {"S": "PT6H"}, "desiredFeedback": {"SS": ["SENT"]}, "includeAttachment": {"BOOL": false}}}, {"M": {"channel": {"S": "EMAIL"}, "recipientType": {"SS": ["PG"]}, "timeout": {"S": "PT72H"}, "desiredFeedback": {"SS": ["RECEIVED", "PAID"]}, "includeAttachment": {"BOOL": true}}}]}}'

       # Case 4: Campagna PEC
       '{"senderId": {"S": "550e8400-e29b-41d4-a716-446655440003"}, "campaignId": {"S": "camp-004-pec"}, "title": {"S": "Comunicazione Legale"}, "descriptionScope": {"S": "Comunicazione tramite PEC"}, "status": {"S": "CONCLUDED"}, "serviceId": {"S": "svc-003"}, "serviceName": {"S": "Legale"}, "senderContact": {"S": "legal@example.com"}, "sensitiveContent": {"BOOL": true}, "stopOnViewed": {"BOOL": false}, "startDate": {"S": "2023-01-01T00:00:00Z"}, "endDate": {"S": "2023-03-01T23:59:59Z"}, "taxonomyCode": {"S": "TAX-004"}, "workflow": {"L": [{"M": {"channel": {"S": "PEC"}, "recipientType": {"SS": ["PG"]}, "timeout": {"S": "PT168H"}, "desiredFeedback": {"SS": ["SKIP"]}, "includeAttachment": {"BOOL": true}}}]}}'

       # Case 5: Campagna ANALOG
       '{"senderId": {"S": "550e8400-e29b-41d4-a716-446655440002"}, "campaignId": {"S": "camp-005-analog"}, "title": {"S": "Campagna Postale"}, "descriptionScope": {"S": "Comunicazione cartacea"}, "status": {"S": "IN_PROGRESS"}, "serviceId": {"S": "svc-002"}, "serviceName": {"S": "Comunicazioni"}, "senderContact": {"S": "postal@example.com"}, "sensitiveContent": {"BOOL": false}, "stopOnViewed": {"BOOL": false}, "startDate": {"S": "2024-01-01T00:00:00Z"}, "endDate": {"S": "2025-12-31T23:59:59Z"}, "taxonomyCode": {"S": "TAX-005"}, "workflow": {"L": [{"M": {"channel": {"S": "ANALOG"}, "recipientType": {"SS": ["PF", "PG"]}, "timeout": {"S": "PT720H"}, "desiredFeedback": {"SS": ["RECEIVED", "PAID"]}, "includeAttachment": {"BOOL": true}}}]}}'
   )

   for campaign in "${Campaigns_ok[@]}"; do
       aws --profile default --region us-east-1 --endpoint-url=http://localstack:4566 \
           dynamodb put-item \
           --table-name pn-Campaigns \
           --item "$campaign"
   done

echo "Initialization terminated"
