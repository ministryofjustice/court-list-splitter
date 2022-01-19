package uk.gov.justice.digital.hmpps.courtlistsplitter.service

import com.microsoft.applicationinsights.TelemetryClient
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.data.MapEntry
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.eq
import org.mockito.Captor
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.verify
import uk.gov.justice.digital.hmpps.courtlistsplitter.model.externaldocumentrequest.Info
import uk.gov.justice.digital.hmpps.courtlistsplitter.model.externaldocumentrequest.buildCase
import java.time.LocalDate
import java.time.Month

@ExtendWith(MockitoExtension::class)
internal class TelemetryServiceTest {

  @Mock
  private lateinit var telemetryClient: TelemetryClient

  @Captor
  private lateinit var propertiesCaptor: ArgumentCaptor<Map<String, String>>

  @InjectMocks
  private lateinit var telemetryService: TelemetryService

  @Test
  fun `should track event`() {

    telemetryService.trackEvent(TelemetryEventType.COURT_LIST_RECEIVED)

    verify(telemetryClient).trackEvent("PiCCourtListReceived")
  }

  @Test
  fun `when message is received and split by courts and date then track the court list event with properties`() {

    val info = Info(ouCode = "B10JQ", dateOfHearing = LocalDate.of(2021, Month.DECEMBER, 25), sequence = 1L)

    telemetryService.trackCourtListEvent(info, "message-id")

    verify(telemetryClient).trackEvent(eq("PiCCourtListReceived"), propertiesCaptor.capture(), eq(emptyMap()))
    val properties: Map<String, String> = propertiesCaptor.value
    assertThat(properties).hasSize(3)
    assertThat(properties).contains(MapEntry.entry("sqsMessageId", "message-id"))
    assertThat(properties).contains(MapEntry.entry("hearingDate", "2021-12-25"))
    assertThat(properties).contains(MapEntry.entry("courtCode", "B10JQ"))
  }

  @Test
  fun `when case is split`() {

    val case = buildCase(caseNo = "2134", dateOfHearing = LocalDate.of(2021, Month.DECEMBER, 25), courtCode = "B10JQ")

    telemetryService.trackCourtCaseSplitEvent(case, "message-id")

    verify(telemetryClient).trackEvent(eq("PiCCourtCaseSplit"), propertiesCaptor.capture(), eq(emptyMap()))
    val properties: Map<String, String> = propertiesCaptor.value

    assertThat(properties).hasSize(5)
    assertThat(properties).contains(MapEntry.entry("sqsMessageId", "message-id"))
    assertThat(properties).contains(MapEntry.entry("hearingDate", "2021-12-25"))
    assertThat(properties).contains(MapEntry.entry("courtCode", "B10JQ"))
    assertThat(properties).contains(MapEntry.entry("courtRoom", "01"))
    assertThat(properties).contains(MapEntry.entry("caseNo", "2134"))
  }
}
