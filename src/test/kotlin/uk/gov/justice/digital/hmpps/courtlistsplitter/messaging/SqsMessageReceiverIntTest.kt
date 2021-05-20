package uk.gov.justice.digital.hmpps.courtlistsplitter.messaging

import com.amazonaws.auth.AWSStaticCredentialsProvider
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.client.builder.AwsClientBuilder
import com.amazonaws.services.sqs.AmazonSQSAsync
import com.amazonaws.services.sqs.AmazonSQSAsyncClientBuilder
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.argThat
import com.nhaarman.mockitokotlin2.timeout
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.mockito.Mockito
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
import uk.gov.justice.digital.hmpps.courtlistsplitter.service.CourtCaseMatcher
import uk.gov.justice.digital.hmpps.courtlistsplitter.service.MessageNotifier
import uk.gov.justice.digital.hmpps.courtlistsplitter.service.MessageProcessor
import java.nio.file.Files
import java.nio.file.Paths

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Import(SqsMessageReceiverIntTest.AwsTestConfig::class)
class SqsMessageReceiverIntTest {

  @Autowired
  private lateinit var queueMessagingTemplate: QueueMessagingTemplate

  @Autowired
  private lateinit var messageNotifier: MessageNotifier

  @Autowired
  private lateinit var amazonSQSAsync: AmazonSQSAsync

  @AfterAll
  internal fun beforeAll() {
    amazonSQSAsync.shutdown()
  }

  @Test
  fun `when message is received then track with telemetry`() {
    // 2 documents, 1 session per document, 2 cases per session
    val content = Files.readString(Paths.get("src/test/resources/messages/external-document-request-multi-session.xml"))

    queueMessagingTemplate.convertAndSend(QUEUE_NAME, content)

    Mockito.verify(messageNotifier, timeout(5000)).send(argThat(CourtCaseMatcher("1600032953")), any())
    Mockito.verify(messageNotifier).send(argThat(CourtCaseMatcher("1600032979")), any())
    Mockito.verify(messageNotifier).send(argThat(CourtCaseMatcher("1600032952")), any())
    Mockito.verify(messageNotifier).send(argThat(CourtCaseMatcher("1600011111")), any())
  }

  @Test
  fun `given invalid message then track but retry before place on DLQ`() {
    queueMessagingTemplate.convertAndSend(QUEUE_NAME, "<xml>message content</xml>")

    verifyNoMoreInteractions(messageNotifier)
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

    @SuppressWarnings("unused")
    @MockBean
    private lateinit var messageNotifier: MessageNotifier

    @Autowired
    private lateinit var messageProcessor: MessageProcessor

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
      return SqsMessageReceiver(queueName, messageProcessor)
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
