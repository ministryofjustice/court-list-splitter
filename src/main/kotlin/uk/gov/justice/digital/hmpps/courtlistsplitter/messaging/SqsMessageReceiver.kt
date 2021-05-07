package uk.gov.justice.digital.hmpps.courtlistsplitter.messaging

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.cloud.aws.messaging.listener.SqsMessageDeletionPolicy
import org.springframework.cloud.aws.messaging.listener.annotation.SqsListener
import org.springframework.context.annotation.Profile
import org.springframework.messaging.handler.annotation.Header
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.courtlistsplitter.service.TelemetryEventType
import uk.gov.justice.digital.hmpps.courtlistsplitter.service.TelemetryService

@Service
@Profile("sqs-read")
class SqsMessageReceiver(
  @Value("\${aws.sqs.queue_name}") private val queueName: String,
  @Autowired private val telemetryService: TelemetryService
) {

  @SqsListener(value = ["crime-portal-gateway-queue"], deletionPolicy = SqsMessageDeletionPolicy.ON_SUCCESS)
  fun receive(message: String, @Header("MessageId") messageId: String?) {
    log.info("Received message from SQS queue {} with messageId: {}", queueName, messageId)
    telemetryService.trackEvent(TelemetryEventType.COURT_LIST_RECEIVED)
  }

  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }
}
