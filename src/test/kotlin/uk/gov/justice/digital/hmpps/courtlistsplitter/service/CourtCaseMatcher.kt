package uk.gov.justice.digital.hmpps.courtlistsplitter.service

import org.mockito.ArgumentMatcher
import uk.gov.justice.digital.hmpps.courtlistsplitter.model.externaldocumentrequest.Case

class CourtCaseMatcher(private val caseNo: String) : ArgumentMatcher<Case> {
  override fun matches(argument: Case?): Boolean {
    return argument?.caseNo.equals(caseNo)
  }
}
