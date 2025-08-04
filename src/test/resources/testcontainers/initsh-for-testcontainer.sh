#!/bin/bash
echo "### CREATE DELIVERY-PUSH COMPONENT ###"

bash <(curl -s https://raw.githubusercontent.com/pagopa/pn-delivery-push-validator/56806ac5fe458f6def309d64fa019d993362e078/src/test/resources/testcontainers/init.sh)

## La creazione delle queue local-delivery-push-inputs.fifo è già presente nel file init.sh di delivery. Viene duplicata su deliveryPush per solo per test-container

echo "### CREATE QUEUE FIFO ###"
queues="local-delivery-push-inputs.fifo"

for qn in  $( echo $queues | tr " " "\n" ) ; do

    echo creating queue $qn ...

    aws --profile default --region us-east-1 --endpoint-url http://localstack:4566 \
        sqs create-queue \
        --attributes '{"DelaySeconds":"2"}' \
        --queue-name $qn

done
echo "Initialization terminated"


