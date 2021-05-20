package uk.gov.justice.digital.hmpps.courtlistsplitter.messaging

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.cloud.aws.messaging.listener.SqsMessageDeletionPolicy
import org.springframework.cloud.aws.messaging.listener.annotation.SqsListener
import org.springframework.context.annotation.Profile
import org.springframework.messaging.handler.annotation.Header
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.courtlistsplitter.service.MessageProcessor

@Service
@Profile("sqs-read")
class SqsMessageReceiver(
  @Value("\${aws.sqs.queue_name}") private val queueName: String,
  @Autowired private val messageProcessor: MessageProcessor
) {

  @SqsListener(value = ["\${aws.sqs.queue_name}"], deletionPolicy = SqsMessageDeletionPolicy.ON_SUCCESS)
  fun receive(message: String, @Header("MessageId") messageId: String) {
    log.info("Received message from SQS queue {} with messageId: {}", queueName, messageId)
    messageProcessor.process(message, messageId)
  }

  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }
}
