package uk.gov.justice.digital.hmpps.courtlistsplitter.messaging

import com.amazonaws.auth.AWSStaticCredentialsProvider
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.client.builder.AwsClientBuilder
import com.amazonaws.services.sns.AmazonSNS
import com.amazonaws.services.sns.AmazonSNSClientBuilder
import com.amazonaws.services.sqs.AmazonSQSAsync
import com.amazonaws.services.sqs.AmazonSQSAsyncClientBuilder
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.argThat
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.verify
import org.awaitility.Awaitility.await
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.cloud.aws.messaging.core.QueueMessagingTemplate
import org.springframework.cloud.aws.messaging.listener.SqsMessageDeletionPolicy
import org.springframework.cloud.aws.messaging.listener.annotation.SqsListener
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.Primary
import org.springframework.messaging.handler.annotation.Header
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.digital.hmpps.courtlistsplitter.model.externaldocumentrequest.Case
import uk.gov.justice.digital.hmpps.courtlistsplitter.model.externaldocumentrequest.Info
import uk.gov.justice.digital.hmpps.courtlistsplitter.service.CourtCaseMatcher
import uk.gov.justice.digital.hmpps.courtlistsplitter.service.MessageProcessor
import uk.gov.justice.digital.hmpps.courtlistsplitter.service.TelemetryService
import java.nio.file.Files
import java.nio.file.Paths
import java.time.LocalDate
import java.time.Month
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Import(SqsMessageReceiverIntTest.AwsTestConfig::class)
class SqsMessageReceiverIntTest {

  @Autowired
  private lateinit var queueMessagingTemplate: QueueMessagingTemplate

  @Autowired
  private lateinit var telemetryService: TelemetryService

  @Autowired
  private lateinit var amazonSQSAsync: AmazonSQSAsync

  @Autowired
  private lateinit var sqsMessageCounter: SqsMessageCounter

  @BeforeEach
  fun beforeEach() {
    sqsMessageCounter.reset()
  }

  @AfterAll
  internal fun beforeAll() {
    amazonSQSAsync.shutdown()
  }

  @Test
  fun `when message is received then track with telemetry`() {
    // 2 documents, 1 session per document, 2 cases per session
    val content = Files.readString(Paths.get("src/test/resources/messages/external-document-request-multi-session.xml"))

    queueMessagingTemplate.convertAndSend(QUEUE_NAME, content)

    await()
      .atMost(3, TimeUnit.SECONDS)
      .until { countMessagesReceived() >= 3 }

    val info1 = Info(7, "B01CY", LocalDate.of(2020, Month.FEBRUARY, 23))
    val info2 = Info(5, "B01CX", LocalDate.of(2020, Month.FEBRUARY, 20))
    verify(telemetryService).trackCourtListEvent(eq(info1), any())
    verify(telemetryService).trackCourtListEvent(eq(info2), any())
    verify(telemetryService).trackCourtCaseSplitEvent(argThat<Case>(CourtCaseMatcher("1600032953")), any())
    verify(telemetryService).trackCourtCaseSplitEvent(argThat<Case>(CourtCaseMatcher("1600032979")), any())
    verify(telemetryService).trackCourtCaseSplitEvent(argThat<Case>(CourtCaseMatcher("1600032953")), any())
    verify(telemetryService).trackCourtCaseSplitEvent(argThat<Case>(CourtCaseMatcher("1600032952")), any())
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

    @Autowired
    private lateinit var messageProcessor: MessageProcessor

    @MockBean
    private lateinit var telemetryService: TelemetryService

    @Primary
    @Bean
    fun amazonSQSAsync(): AmazonSQSAsync {
      return AmazonSQSAsyncClientBuilder
        .standard()
        .withCredentials(AWSStaticCredentialsProvider(BasicAWSCredentials(accessKeyId, secretAccessKey)))
        .withEndpointConfiguration(AwsClientBuilder.EndpointConfiguration(sqsEndpointUrl, regionName))
        .build()
    }

    @Bean
    fun amazonSNSClient(
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
    fun sqsMessageReceiver(): SqsMessageReceiver {
      return SqsMessageReceiver(queueName, messageProcessor)
    }

    @Bean
    fun sqsMessageCounter(): SqsMessageCounter {
      return SqsMessageCounter()
    }

    @Bean
    fun queueMessagingTemplate(@Autowired amazonSQSAsync: AmazonSQSAsync): QueueMessagingTemplate {
      return QueueMessagingTemplate(amazonSQSAsync)
    }
  }

  private fun countMessagesReceived(): Int {
    return sqsMessageCounter.count.toInt()
  }

  companion object {
    private const val QUEUE_NAME = "crime-portal-gateway-queue"
  }
}

class SqsMessageCounter {
  var count = AtomicInteger(0)
  @SqsListener(value = ["court-case-matcher-queue"], deletionPolicy = SqsMessageDeletionPolicy.ALWAYS)
  fun receive(message: String, @Header("MessageId") messageId: String) {
    log.info("IN counter - incremented to is " + count.addAndGet(1) + " id " + messageId + ":" + message)
  }

  fun reset() {
    count.set(0)
  }

  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }
}
