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
                   \"descriptionScope\": \"Test campaign with 2 deafults messages\",
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
                   \"descriptionScope\": \"Test campaign with 1 deafults messages\",
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

echo "Initialization terminated"
