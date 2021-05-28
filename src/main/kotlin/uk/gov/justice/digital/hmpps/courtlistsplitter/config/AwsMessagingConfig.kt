package uk.gov.justice.digital.hmpps.courtlistsplitter.config

import com.amazonaws.auth.AWSStaticCredentialsProvider
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.client.builder.AwsClientBuilder.EndpointConfiguration
import com.amazonaws.services.sns.AmazonSNS
import com.amazonaws.services.sns.AmazonSNSClientBuilder
import com.amazonaws.services.sqs.AmazonSQSAsync
import com.amazonaws.services.sqs.AmazonSQSAsyncClientBuilder
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile

@Profile("!test")
@Configuration
open class AwsMessagingConfig {

  @Primary
  @Bean
  open fun amazonSQSAsync(
    @Value("\${aws.region-name}") regionName: String,
    @Value("\${aws.sqs_endpoint_url}") awsEndpointUrl: String,
    @Value("\${aws_access_key_id}") awsAccessKeyId: String,
    @Value("\${aws_secret_access_key}") awsSecretAccessKey: String
  ): AmazonSQSAsync {
    return AmazonSQSAsyncClientBuilder
      .standard()
      .withCredentials(
        AWSStaticCredentialsProvider(BasicAWSCredentials(awsAccessKeyId, awsSecretAccessKey))
      )
      .withEndpointConfiguration(EndpointConfiguration(awsEndpointUrl, regionName))
      .build()
  }

  @Bean
  open fun amazonSNSClient(
    @Value("\${aws.region-name}") regionName: String,
    @Value("\${aws_sns_access_key_id}") awsAccessKeyId: String,
    @Value("\${aws_sns_secret_access_key}") awsSecretAccessKey: String
  ): AmazonSNS {
    return AmazonSNSClientBuilder
      .standard()
      .withCredentials(AWSStaticCredentialsProvider(BasicAWSCredentials(awsAccessKeyId, awsSecretAccessKey)))
      .withRegion(regionName)
      .build()
  }
}
