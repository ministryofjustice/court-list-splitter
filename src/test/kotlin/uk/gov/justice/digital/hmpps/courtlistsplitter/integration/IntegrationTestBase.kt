package uk.gov.justice.digital.hmpps.courtlistsplitter.integration

import com.amazonaws.services.sqs.AmazonSQS
import com.amazonaws.services.sqs.model.PurgeQueueRequest
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.web.reactive.server.WebTestClient
import uk.gov.justice.digital.hmpps.courtlistsplitter.integration.LocalStackContainer.setLocalStackProperties
import uk.gov.justice.hmpps.sqs.HmppsQueueService

@SpringBootTest(webEnvironment = RANDOM_PORT)
@ActiveProfiles("test")
abstract class IntegrationTestBase {

  @Suppress("SpringJavaInjectionPointsAutowiringInspection")
  @Autowired
  lateinit var webTestClient: WebTestClient

  @Autowired
  lateinit var hmppsQueueService: HmppsQueueService

  @Autowired
  lateinit var objectMapper: ObjectMapper

  val crimePortalGatewayTopic by lazy {
    hmppsQueueService.findByTopicId("crimeportalgatewaytopic")
  }

  val crimePortalGatewayQueue by lazy {
    hmppsQueueService.findByQueueId("crimeportalgatewayqueue")
  }
  val courtCaseEventsQueue by lazy {
    hmppsQueueService.findByQueueId("courtcaseeventsqueue")
  }

  @BeforeEach
  fun beforeEach() {
    crimePortalGatewayQueue?.sqsDlqClient!!.purgeQueue(PurgeQueueRequest(crimePortalGatewayQueue?.dlqUrl))
    crimePortalGatewayQueue?.sqsClient!!.purgeQueue(PurgeQueueRequest(crimePortalGatewayQueue?.queueUrl))
    courtCaseEventsQueue?.sqsDlqClient!!.purgeQueue(PurgeQueueRequest(courtCaseEventsQueue?.dlqUrl))
    courtCaseEventsQueue?.sqsClient!!.purgeQueue(PurgeQueueRequest(courtCaseEventsQueue?.queueUrl))
  }

  internal fun AmazonSQS.countMessagesOnQueue(queueUrl: String): Int =
    this.getQueueAttributes(queueUrl, listOf("ApproximateNumberOfMessages"))
      .let { it.attributes["ApproximateNumberOfMessages"]?.toInt() ?: 0 }

  companion object {
    private val localStackContainer = LocalStackContainer.instance

    @JvmStatic
    @DynamicPropertySource
    fun testcontainers(registry: DynamicPropertyRegistry) {
      localStackContainer?.also { setLocalStackProperties(it, registry) }
    }
  }
}
