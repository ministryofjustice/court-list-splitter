package uk.gov.justice.digital.hmpps.courtlistsplitter.messaging

import com.nhaarman.mockitokotlin2.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import uk.gov.justice.digital.hmpps.courtlistsplitter.service.TelemetryEventType
import uk.gov.justice.digital.hmpps.courtlistsplitter.service.TelemetryService

@ExtendWith(MockitoExtension::class)
internal class SqsMessageReceiverTest {

  @Mock
  private lateinit var telemetryService: TelemetryService

  private lateinit var sqsMessageReceiver: SqsMessageReceiver

  @BeforeEach
  fun beforeEach() {
    sqsMessageReceiver = SqsMessageReceiver("queue-name", telemetryService)
  }

  @Test
  fun `should track event when message received`() {

    sqsMessageReceiver.receive("message-content", "messageId")

    verify(telemetryService).trackEvent(TelemetryEventType.COURT_LIST_RECEIVED)
  }
}
