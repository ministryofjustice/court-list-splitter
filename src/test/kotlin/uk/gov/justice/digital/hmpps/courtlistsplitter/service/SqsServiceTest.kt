package uk.gov.justice.digital.hmpps.courtlistsplitter.service

import com.amazonaws.services.sqs.AmazonSQSAsync
import com.amazonaws.services.sqs.model.GetQueueUrlResult
import com.amazonaws.services.sqs.model.QueueDoesNotExistException
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever

@ExtendWith(MockitoExtension::class)
internal class SqsServiceTest {

  @Mock
  private lateinit var amazonSQSAsync: AmazonSQSAsync

  private lateinit var sqsService: SqsService

  @BeforeEach
  fun beforeEach() {
    sqsService = SqsService("queue-name", amazonSQSAsync)
  }

  @Test
  fun `should return true when queue available`() {

    whenever(amazonSQSAsync.getQueueUrl("queue-name")).thenReturn(GetQueueUrlResult().withQueueUrl("queue-url"))

    assertThat(sqsService.isQueueAvailable()).isTrue
  }

  @Test
  fun `given null queue URL result should return false`() {

    whenever(amazonSQSAsync.getQueueUrl("queue-name")).thenReturn(GetQueueUrlResult())

    assertThat(sqsService.isQueueAvailable()).isFalse
  }

  @Test
  fun `given null queue URL should return false`() {

    whenever(amazonSQSAsync.getQueueUrl("queue-name")).thenReturn(null)

    assertThat(sqsService.isQueueAvailable()).isFalse
  }

  @Test
  fun `given exception in AWS call should return false`() {

    whenever(amazonSQSAsync.getQueueUrl("queue-name")).thenThrow(QueueDoesNotExistException::class.java)

    assertThat(sqsService.isQueueAvailable()).isFalse
  }
}
