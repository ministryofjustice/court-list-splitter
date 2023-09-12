# Court List Splitter

[![CircleCI](https://circleci.com/gh/ministryofjustice/court-list-splitter.svg?style=svg)](https://circleci.com/gh/ministryofjustice/court-list-splitter)

This service will provide the capability to read a message from configured Amazon SQS queue. The message received is a LIBRA court feed and contains multiple court cases. The service will split this message into its constituent cases and publish each to a configured Amazon SNS topic.

## Quickstart

### Requirements

- Docker
- Java

Build and test:

```
./gradlew build
```

Install Git hooks (Optional):

```./gradlew installGitHooks```

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

## Accessing AWS Resources

The service uses IAM Roles for Service Accounts (IRSA) rather than an AWS access key id and secret to access
SQS queues and SNS topics. There is therefore no need to programmatically specify credentials as authentication
takes place behind the scenes using a `WebIdentityTokenFileCredentialsProvider` which requires the
AWS sts module to be on the classpath and in the [build.gradle.kts](build.gradle.kts):

```
implementation("com.amazonaws:aws-java-sdk-sts:$awsSdkVersion")
```
 
As this service uses [hmpps-helm-charts](https://ministryofjustice.github.io/hmpps-helm-charts/) as a template, the default service account name needs to be overridden
in the [values.yaml](./helm_deploy/court-list-splitter/values.yaml) to include the IRSA service account name associated with the k8s namespace:

```
generic-service:
  nameOverride: court-list-splitter
  replicaCount: 4
  serviceAccountName: "court-case-service"
  ....
  ....
```

