#!/usr/bin/env bash
set -e
export TERM=ansi
export AWS_ACCESS_KEY_ID=foobar
export AWS_SECRET_ACCESS_KEY=foobar
export AWS_DEFAULT_REGION=eu-west-2
export PAGER=

# The queue that the splitter receives messages from
aws --endpoint-url=http://localhost:4566 sqs create-queue --queue-name crime-portal-gateway-queue-dlq
aws --endpoint-url http://localhost:4566 sqs create-queue --queue-name crime-portal-gateway-queue

aws --endpoint-url=http://localhost:4566 sqs set-queue-attributes --queue-url "http://localhost:4566/000000000000/crime-portal-gateway-queue" --attributes '{"RedrivePolicy":"{\"maxReceiveCount\":\"2\", \"deadLetterTargetArn\":\"arn:aws:sqs:eu-west-2:000000000000:crime-portal-gateway-queue-dlq\"}", "VisibilityTimeout": "0" }'

# The topic that the splitter writes to
aws --endpoint-url http://localhost:4566 sqs create-queue --queue-name court-case-matcher-queue
aws --endpoint-url=http://localhost:4566 sns create-topic --name court-case-events-topic
aws --endpoint-url=http://localhost:4566 sns subscribe --topic-arn "arn:aws:sns:eu-west-2:000000000000:court-case-events-topic" --protocol "sqs" --notification-endpoint "http://localhost:4566/000000000000/court-case-matcher-queue"



echo "SNS and SQS Configured"
