package uk.gov.justice.digital.hmpps.courtlistsplitter.messaging

import com.amazonaws.auth.AWSStaticCredentialsProvider
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.client.builder.AwsClientBuilder
import com.amazonaws.services.sns.AmazonSNS
import com.amazonaws.services.sns.AmazonSNSClientBuilder
import com.amazonaws.services.sqs.AmazonSQS
import com.amazonaws.services.sqs.AmazonSQSAsync
import com.amazonaws.services.sqs.AmazonSQSAsyncClientBuilder
import com.amazonaws.services.sqs.AmazonSQSClientBuilder
import com.amazonaws.services.sqs.model.PurgeQueueRequest
import org.awaitility.Awaitility.await
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.mockito.kotlin.any
import org.mockito.kotlin.argThat
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.Primary
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.digital.hmpps.courtlistsplitter.model.externaldocumentrequest.Info
import uk.gov.justice.digital.hmpps.courtlistsplitter.service.CourtCaseMatcher
import uk.gov.justice.digital.hmpps.courtlistsplitter.service.MessageProcessor
import uk.gov.justice.digital.hmpps.courtlistsplitter.service.TelemetryService
import java.nio.file.Files
import java.nio.file.Paths
import java.time.LocalDate
import java.time.Month
import java.util.concurrent.TimeUnit

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Import(SqsMessageReceiverIntTest.AwsTestConfig::class)
@Disabled
class SqsMessageReceiverIntTest {

  @Autowired
  private lateinit var amazonSQSAsync: AmazonSQSAsync

  @MockBean
  private lateinit var telemetryService: TelemetryService

  @Autowired
  private lateinit var sqsClient: AmazonSQS

  @BeforeEach
  fun beforeEach() {
    sqsClient.purgeQueue(PurgeQueueRequest("http://localhost:4566/000000000000/crime-portal-gateway-queue"))
  }

  @AfterAll
  internal fun afterAll() {
    amazonSQSAsync.shutdown()
  }

  @Test
  fun `when message is received then track with telemetry`() {
    // 2 documents, 1 session per document, 2 cases per session
    val content = Files.readString(Paths.get("src/test/resources/messages/external-document-request-multi-session.xml"))

    // send message here

    await()
      .atMost(10, TimeUnit.SECONDS)
    // at least 3 messages have been received .until { }

    val info1 = Info(7, "B01CY", LocalDate.of(2020, Month.FEBRUARY, 23))
    val info2 = Info(5, "B01CX", LocalDate.of(2020, Month.FEBRUARY, 20))
    verify(telemetryService).trackCourtListEvent(eq(info1), any())
    verify(telemetryService).trackCourtListEvent(eq(info2), any())
    verify(telemetryService).trackCourtCaseSplitEvent(argThat(CourtCaseMatcher("1600032953")), any())
    verify(telemetryService).trackCourtCaseSplitEvent(argThat(CourtCaseMatcher("1600032979")), any())
    verify(telemetryService).trackCourtCaseSplitEvent(argThat(CourtCaseMatcher("1600032953")), any())
    verify(telemetryService).trackCourtCaseSplitEvent(argThat(CourtCaseMatcher("1600032952")), any())
  }

  @TestConfiguration
  class AwsTestConfig() {

    @Primary
    @Bean
    fun amazonSQSAsync(
      @Value("\${aws.sqs_endpoint_url}") sqsEndpointUrl: String,
      @Value("\${aws.access_key_id}") accessKeyId: String,
      @Value("\${aws.secret_access_key}") secretAccessKey: String,
      @Value("\${aws.region_name}") regionName: String
    ): AmazonSQSAsync {
      return AmazonSQSAsyncClientBuilder
        .standard()
        .withCredentials(AWSStaticCredentialsProvider(BasicAWSCredentials(accessKeyId, secretAccessKey)))
        .withEndpointConfiguration(AwsClientBuilder.EndpointConfiguration(sqsEndpointUrl, regionName))
        .build()
    }

    @Bean
    fun amazonSNSClient(
      @Value("\${aws.sqs_endpoint_url}") sqsEndpointUrl: String,
      @Value("\${aws.region-name}") regionName: String,
      @Value("\${aws_sns_access_key_id}") awsAccessKeyId: String,
      @Value("\${aws_sns_secret_access_key}") awsSecretAccessKey: String
    ): AmazonSNS {
      return AmazonSNSClientBuilder
        .standard()
        .withEndpointConfiguration(AwsClientBuilder.EndpointConfiguration(sqsEndpointUrl, regionName))
        .withCredentials(AWSStaticCredentialsProvider(BasicAWSCredentials(awsAccessKeyId, awsSecretAccessKey)))
        .build()
    }
    @Bean
    fun amazonSQSClient(
      @Value("\${aws.sqs_endpoint_url}") sqsEndpointUrl: String,
      @Value("\${aws.region-name}") regionName: String,
      @Value("\${aws_sns_access_key_id}") awsAccessKeyId: String,
      @Value("\${aws_sns_secret_access_key}") awsSecretAccessKey: String
    ): AmazonSQS? {
      return AmazonSQSClientBuilder
        .standard()
        .withEndpointConfiguration(AwsClientBuilder.EndpointConfiguration(sqsEndpointUrl, regionName))
        .withCredentials(AWSStaticCredentialsProvider(BasicAWSCredentials(awsAccessKeyId, awsSecretAccessKey)))
        .build()
    }

    @Bean
    fun sqsMessageReceiver(@Value("\${aws.sqs.queue_name}") queueName: String, messageProcessor: MessageProcessor): SqsMessageReceiver {
      return SqsMessageReceiver(queueName, messageProcessor)
    }
  }

  companion object {
    private const val QUEUE_NAME = "crime-portal-gateway-queue"
  }
}
