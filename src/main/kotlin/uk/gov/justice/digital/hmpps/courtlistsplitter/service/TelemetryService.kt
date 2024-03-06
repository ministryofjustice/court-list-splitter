package uk.gov.justice.digital.hmpps.courtlistsplitter.service

import com.microsoft.applicationinsights.TelemetryClient
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.courtlistsplitter.model.externaldocumentrequest.Case
import uk.gov.justice.digital.hmpps.courtlistsplitter.model.externaldocumentrequest.Info

@Service
class TelemetryService(private val telemetryClient: TelemetryClient) {

  fun trackEvent(eventType: TelemetryEventType) {
    telemetryClient.trackEvent(eventType.eventName)
  }

  fun trackCourtListEvent(info: Info, messageId: String) {
    val properties = mapOf(COURT_CODE_KEY to info.ouCode, HEARING_DATE_KEY to info.dateOfHearing.toString(), SQS_MESSAGE_ID_KEY to messageId)

    telemetryClient.trackEvent(TelemetryEventType.COURT_LIST_RECEIVED.eventName, properties, emptyMap())
  }

  fun trackCourtCaseSplitEvent(case: Case, messageId: String) {
    val session = case.block.session
    val properties = mapOf(
      COURT_CODE_KEY to session.courtCode,
      COURT_ROOM_KEY to session.courtRoom,
      HEARING_DATE_KEY to session.dateOfHearing.toString(),
      CASE_NO_KEY to case.caseNo,
      SQS_MESSAGE_ID_KEY to messageId,
    )

    telemetryClient.trackEvent(TelemetryEventType.COURT_CASE_SPLIT.eventName, properties, emptyMap())
  }

  companion object {
    const val COURT_CODE_KEY = "courtCode"
    const val COURT_ROOM_KEY = "courtRoom"
    const val CASE_NO_KEY = "caseNo"
    const val HEARING_DATE_KEY = "hearingDate"
    const val SQS_MESSAGE_ID_KEY = "sqsMessageId"
  }
}
