package uk.gov.justice.digital.hmpps.courtlistsplitter.service

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.courtlistsplitter.model.externaldocumentrequest.Case

@Component
class MessageNotifier(
  @Autowired
  private val objectMapper: ObjectMapper,
  @Autowired
  private val telemetryService: TelemetryService
) {
  fun send(case: Case, messageId: String) {
    telemetryService.trackCourtCaseEvent(case, messageId)
    val message = objectMapper.writeValueAsString(case)
  }
}
