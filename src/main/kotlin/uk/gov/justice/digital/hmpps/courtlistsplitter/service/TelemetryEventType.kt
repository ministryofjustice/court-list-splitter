package uk.gov.justice.digital.hmpps.courtlistsplitter.service

enum class TelemetryEventType(val eventName: String) {
  COURT_LIST_MESSAGE_RECEIVED("PiCCourtListMessageReceived"), // Records the receipt of an entire message, with message ID
  COURT_LIST_RECEIVED("PiCCourtListReceived"), // Records receipt of a list per court
  COURT_CASE_RECEIVED("PiCCourtCaseReceived")
}
