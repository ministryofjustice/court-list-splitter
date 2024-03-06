package uk.gov.justice.digital.hmpps.courtlistsplitter.integration

import com.amazonaws.services.sqs.AmazonSQS
import com.amazonaws.services.sqs.model.PurgeQueueRequest
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.reactive.server.WebTestClient
import uk.gov.justice.hmpps.sqs.HmppsQueueService

@SpringBootTest(webEnvironment = RANDOM_PORT)
@ActiveProfiles("test")
abstract class IntegrationTestBase {

  @Suppress("SpringJavaInjectionPointsAutowiringInspection")
  @Autowired
  lateinit var webTestClient: WebTestClient

  @Autowired
  lateinit var hmppsQueueService: HmppsQueueService

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
}
