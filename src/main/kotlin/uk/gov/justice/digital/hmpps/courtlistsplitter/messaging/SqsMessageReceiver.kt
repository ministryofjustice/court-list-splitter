package uk.gov.justice.digital.hmpps.courtlistsplitter.messaging

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.jms.annotation.JmsListener
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.courtlistsplitter.service.MessageProcessor

@Service
@Profile("sqs-read")
class SqsMessageReceiver(
  @Autowired private val messageProcessor: MessageProcessor,
  @Autowired private val objectMapper: ObjectMapper,
  @Value("\${features.test.send_all_messages_to_dlq:false}") private val sendAllMessagesToDlq: Boolean = false,
) {

  @JmsListener(destination = "crimeportalgatewayqueue", containerFactory = "hmppsQueueContainerFactoryProxy")
  fun receive(rawMessage: String) {
    if (sendAllMessagesToDlq) throw RuntimeException("Simulating failure because features.test.send_all_messages_to_dlq flag is set. Message will go to DLQ")
    val message = objectMapper.readValue(rawMessage, Message::class.java)
    log.info("Received message from SQS queue with messageId: {}", message.MessageId)
    messageProcessor.process(message.Message, message.MessageId)
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  data class Message(
    val Message: String,
    val MessageId: String,
  )

  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }
}
