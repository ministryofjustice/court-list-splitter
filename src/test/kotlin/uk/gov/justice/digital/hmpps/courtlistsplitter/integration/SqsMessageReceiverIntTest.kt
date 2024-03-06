package uk.gov.justice.digital.hmpps.courtlistsplitter.integration

import com.amazonaws.services.sns.model.MessageAttributeValue
import com.amazonaws.services.sns.model.PublishRequest
import org.awaitility.kotlin.await
import org.awaitility.kotlin.matches
import org.awaitility.kotlin.untilCallTo
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.mockito.kotlin.any
import org.mockito.kotlin.argThat
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.digital.hmpps.courtlistsplitter.model.externaldocumentrequest.Info
import uk.gov.justice.digital.hmpps.courtlistsplitter.service.CourtCaseMatcher
import uk.gov.justice.digital.hmpps.courtlistsplitter.service.TelemetryService
import java.nio.file.Files
import java.nio.file.Paths
import java.time.LocalDate
import java.time.Month
import java.util.UUID

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test", "sqs-read")
class SqsMessageReceiverIntTest : IntegrationTestBase() {

  @MockBean
  private lateinit var telemetryService: TelemetryService

  @Test
  fun `when message is received then track with telemetry`() {
    // 2 documents, 1 session per document, 2 cases per session
    val content = Files.readString(Paths.get("src/test/resources/messages/external-document-request-multi-session.xml"))

    crimePortalGatewayTopic?.snsClient?.publish(
      PublishRequest(crimePortalGatewayTopic?.arn, content)
        .withMessageAttributes(
          mapOf("MessageId" to MessageAttributeValue().withDataType("String").withStringValue(UUID.randomUUID().toString())),
        ),
    )
    crimePortalGatewayQueue?.sqsClient?.countMessagesOnQueue(crimePortalGatewayQueue?.queueUrl!!)
    await untilCallTo { courtCaseEventsQueue?.sqsClient?.countMessagesOnQueue(courtCaseEventsQueue?.queueUrl!!) } matches { it == 4 }

    val info1 = Info(7, "B01CY", LocalDate.of(2020, Month.FEBRUARY, 23))
    val info2 = Info(5, "B01CX", LocalDate.of(2020, Month.FEBRUARY, 20))
    verify(telemetryService).trackCourtListEvent(eq(info1), any())
    verify(telemetryService).trackCourtListEvent(eq(info2), any())
    verify(telemetryService).trackCourtCaseSplitEvent(argThat(CourtCaseMatcher("1600032953")), any())
    verify(telemetryService).trackCourtCaseSplitEvent(argThat(CourtCaseMatcher("1600032979")), any())
    verify(telemetryService).trackCourtCaseSplitEvent(argThat(CourtCaseMatcher("1600032953")), any())
    verify(telemetryService).trackCourtCaseSplitEvent(argThat(CourtCaseMatcher("1600032952")), any())
  }
}
