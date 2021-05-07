package uk.gov.justice.digital.hmpps.courtlistsplitter.service

import com.microsoft.applicationinsights.TelemetryClient
import org.springframework.stereotype.Service

@Service
class TelemetryService(private val telemetryClient: TelemetryClient) {

  fun trackEvent(eventType: TelemetryEventType) {
    telemetryClient.trackEvent(eventType.eventName)
  }
}
