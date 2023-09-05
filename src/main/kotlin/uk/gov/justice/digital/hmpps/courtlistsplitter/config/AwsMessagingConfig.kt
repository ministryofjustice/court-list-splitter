package uk.gov.justice.digital.hmpps.courtlistsplitter.config

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
class AwsMessagingConfig {

  @Primary
  @Bean
  fun amazonSQSAsync(
    @Value("\${aws.region-name}") regionName: String,
    @Value("\${aws.sqs_endpoint_url}") awsEndpointUrl: String
  ): AmazonSQSAsync {
    return AmazonSQSAsyncClientBuilder
      .standard()
      .withEndpointConfiguration(EndpointConfiguration(awsEndpointUrl, regionName))
      .build()
  }

  @Bean
  fun amazonSNSClient(
    @Value("\${aws.region-name}") regionName: String
  ): AmazonSNS {
    return AmazonSNSClientBuilder
      .standard()
      .withRegion(regionName)
      .build()
  }
}
