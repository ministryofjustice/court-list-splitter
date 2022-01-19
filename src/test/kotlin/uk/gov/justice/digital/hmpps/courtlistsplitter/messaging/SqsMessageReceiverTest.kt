package uk.gov.justice.digital.hmpps.courtlistsplitter.messaging

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import uk.gov.justice.digital.hmpps.courtlistsplitter.service.MessageProcessor

@ExtendWith(MockitoExtension::class)
internal class SqsMessageReceiverTest {

  @Mock
  private lateinit var messageProcessor: MessageProcessor
  private lateinit var sqsMessageReceiver: SqsMessageReceiver

  @Test
  fun `should track event when message received`() {
    sqsMessageReceiver = SqsMessageReceiver("queue-name", messageProcessor, false)

    sqsMessageReceiver.receive("message-content", "messageId")

    verify(messageProcessor).process("message-content", "messageId")
  }

  @Test
  fun `should throw exception when message received and force-error flag is true`() {
    sqsMessageReceiver = SqsMessageReceiver("queue-name", messageProcessor, true)

    assertThrows<RuntimeException>("Simulating failure because features.send_all_messages_to_dlq flag is set") { sqsMessageReceiver.receive("message-content", "messageId") }

    verify(messageProcessor, never()).process("message-content", "messageId")
  }
}
