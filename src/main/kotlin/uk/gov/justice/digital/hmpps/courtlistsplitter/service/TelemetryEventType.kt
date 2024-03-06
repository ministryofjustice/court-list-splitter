package uk.gov.justice.digital.hmpps.courtlistsplitter.service

enum class TelemetryEventType(val eventName: String) {
  COURT_LIST_RECEIVED("PiCCourtListReceived"), // Records receipt of a list per court
  COURT_CASE_SPLIT("PiCCourtCaseSplit"),
}
