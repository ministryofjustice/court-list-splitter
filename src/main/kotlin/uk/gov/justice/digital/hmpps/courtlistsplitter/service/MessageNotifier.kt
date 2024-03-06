package uk.gov.justice.digital.hmpps.courtlistsplitter.service

import com.amazonaws.services.sns.model.MessageAttributeValue
import com.amazonaws.services.sns.model.PublishRequest
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.availability.AvailabilityChangeEvent.publish
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.courtlistsplitter.model.externaldocumentrequest.Case
import uk.gov.justice.hmpps.sqs.HmppsQueueService
import uk.gov.justice.hmpps.sqs.MissingTopicException

private const val MESSAGE_TYPE = "LIBRA_COURT_CASE"

@Component
class MessageNotifier(
  @Autowired
  private val objectMapper: ObjectMapper,
  @Autowired
  private val telemetryService: TelemetryService,
  @Autowired
  private val hmppsQueueService: HmppsQueueService,

) {

  private val topic = hmppsQueueService.findByTopicId("courtcaseeventstopic") ?: throw MissingTopicException("Could not find topic outboundtopic")

  fun send(case: Case, messageId: String) {
    telemetryService.trackCourtCaseSplitEvent(case, messageId)
    val message = objectMapper.writeValueAsString(case)
    val subject = "Details for case " + case.caseNo + " in court " + case.courtCode + " published with messageId " + messageId

    val messageValue = MessageAttributeValue()
      .withDataType("String")
      .withStringValue(MESSAGE_TYPE)

    val publishRequest = PublishRequest(topic.arn, message)
      .withMessageAttributes(mapOf("messageType" to messageValue))

    val publishResult = topic.snsClient.publish(publishRequest)
    log.info("Published message with subject {} with message Id {}", subject, publishResult.messageId)
  }

  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }
}
