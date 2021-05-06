package uk.gov.justice.digital.hmpps.courtlistsplitter.service

import com.microsoft.applicationinsights.TelemetryClient
import com.nhaarman.mockitokotlin2.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension

@ExtendWith(MockitoExtension::class)
internal class TelemetryServiceTest {

  @Mock
  private lateinit var telemetryClient: TelemetryClient

  @InjectMocks
  private lateinit var telemetryService: TelemetryService

  @Test
  fun `should track event`() {

    telemetryService.trackEvent(TelemetryEventType.COURT_LIST_RECEIVED)

    verify(telemetryClient).trackEvent("PiCCourtListReceived")
  }
}
