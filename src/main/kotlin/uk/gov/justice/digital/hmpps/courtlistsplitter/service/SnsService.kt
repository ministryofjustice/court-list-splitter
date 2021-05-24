package uk.gov.justice.digital.hmpps.courtlistsplitter.service

import com.amazonaws.services.sns.AmazonSNS
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class SnsService(
  @Value("\${aws.sns.topic_arn}")
  private val topicArn: String,
  @Autowired
  private val amazonSNSClient: AmazonSNS
) {

  fun isTopicReachable(): Boolean {
    return try {
      amazonSNSClient.getTopicAttributes(topicArn).attributes.entries.isNotEmpty()
    } catch (exception: Exception) {
      log.error("Unable to get topic ", topicArn, exception)
      false
    }
  }

  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }
}
