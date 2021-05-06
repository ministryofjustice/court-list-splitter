package uk.gov.justice.digital.hmpps.courtlistsplitter.messaging

import com.amazonaws.auth.AWSStaticCredentialsProvider
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.client.builder.AwsClientBuilder
import com.amazonaws.services.sqs.AmazonSQSAsync
import com.amazonaws.services.sqs.AmazonSQSAsyncClientBuilder
import com.nhaarman.mockitokotlin2.timeout
import com.nhaarman.mockitokotlin2.verify
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.cloud.aws.messaging.core.QueueMessagingTemplate
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.Primary
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.digital.hmpps.courtlistsplitter.service.TelemetryEventType
import uk.gov.justice.digital.hmpps.courtlistsplitter.service.TelemetryService

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Import(SqsMessageReceiverIntTest.AwsTestConfig::class)
class SqsMessageReceiverIntTest {

  @Autowired
  private lateinit var queueMessagingTemplate: QueueMessagingTemplate

  @Autowired
  private lateinit var telemetryService: TelemetryService

  @Test
  fun `when message is received then track with telemetry`() {
    queueMessagingTemplate.convertAndSend(QUEUE_NAME, "hello")

    verify(telemetryService, timeout(2000)).trackEvent(TelemetryEventType.COURT_LIST_RECEIVED)
  }

  @TestConfiguration
  class AwsTestConfig(
    @Value("\${aws.sqs_endpoint_url}")
    private val sqsEndpointUrl: String,
    @Value("\${aws.access_key_id}")
    private val accessKeyId: String,
    @Value("\${aws.secret_access_key}")
    private val secretAccessKey: String,
    @Value("\${aws.region_name}")
    private val regionName: String,
    @Value("\${aws.sqs.queue_name}")
    private val queueName: String
  ) {

    @MockBean
    private lateinit var telemetryService: TelemetryService

    @Primary
    @Bean
    fun amazonSQSAsync(): AmazonSQSAsync {
      return AmazonSQSAsyncClientBuilder
        .standard()
        .withCredentials(
          AWSStaticCredentialsProvider(
            BasicAWSCredentials(
              accessKeyId,
              secretAccessKey
            )
          )
        )
        .withEndpointConfiguration(
          AwsClientBuilder.EndpointConfiguration(
            sqsEndpointUrl,
            regionName
          )
        )
        .build()
    }

    @Bean
    fun sqsMessageReceiver(): SqsMessageReceiver {
      return SqsMessageReceiver(queueName, telemetryService)
    }

    @Bean
    fun queueMessagingTemplate(@Autowired amazonSQSAsync: AmazonSQSAsync): QueueMessagingTemplate {
      return QueueMessagingTemplate(amazonSQSAsync)
    }
  }

  companion object {
    private const val QUEUE_NAME = "crime-portal-gateway-queue"
  }
}
