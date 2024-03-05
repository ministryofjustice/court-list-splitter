package uk.gov.justice.digital.hmpps.courtlistsplitter.messaging

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.jms.annotation.JmsListener
import org.springframework.messaging.handler.annotation.Header
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.courtlistsplitter.service.MessageProcessor

@Service
@Profile("sqs-read")
class SqsMessageReceiver(
  @Autowired private val messageProcessor: MessageProcessor,
  @Value("\${features.test.send_all_messages_to_dlq:false}") private val sendAllMessagesToDlq: Boolean = false
) {

  @JmsListener(destination = "crimeportalgatewayqueue", containerFactory = "hmppsQueueContainerFactoryProxy")
  fun receive(message: String) {
    if (sendAllMessagesToDlq) throw RuntimeException("Simulating failure because features.test.send_all_messages_to_dlq flag is set. Message will go to DLQ")
    messageProcessor.process(message, "some-id")
  }

  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }
}
