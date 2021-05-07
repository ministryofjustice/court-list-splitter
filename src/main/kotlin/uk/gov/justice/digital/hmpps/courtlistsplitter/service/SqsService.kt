package uk.gov.justice.digital.hmpps.courtlistsplitter.service

import com.amazonaws.services.sqs.AmazonSQSAsync
import com.amazonaws.services.sqs.model.QueueDoesNotExistException
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class SqsService(
  @Value("\${aws_sqs_queue_name:crime-portal-gateway-queue}")
  private val queueName: String,
  @Autowired
  private val amazonSQSAsync: AmazonSQSAsync
) {

  fun isQueueAvailable(): Boolean {
    return try {
      amazonSQSAsync.getQueueUrl(queueName).let { url ->
        return url?.queueUrl?.isNotEmpty() ?: false
      }
    } catch (existException: QueueDoesNotExistException) {
      log.error("Queue URL not available for name {}", queueName)
      false
    }
  }

  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }
}
