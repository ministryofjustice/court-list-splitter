# Court List Splitter

[![CircleCI](https://circleci.com/gh/ministryofjustice/court-list-splitter.svg?style=svg)](https://circleci.com/gh/ministryofjustice/court-list-splitter)

This service will provide the capability to read a message from configured Amazon SQS queue. The message received is a LIBRA court feed and contains multiple court cases. The service will split this message into its consituent cases and publish each to a configured Amazon SNS topic.

## Quickstart

### Requirements

- Docker
- Java

Build and test:

```
./gradlew build
```

### Integration tests

These require an SQS queue which can be initialised with localstack (https://github.com/localstack/localstack). There is a docker compose config file and a script which creates the required queues.

```
docker-compose up localstack
```

Run integration tests
```
./gradlew integrationTest
```

## Code Style

[ktlint](https://github.com/pinterest/ktlint) is the authority on style and is enforced on build.

Run `./gradlew ktlintFormat` to fix formatting errors in your code before commit.
