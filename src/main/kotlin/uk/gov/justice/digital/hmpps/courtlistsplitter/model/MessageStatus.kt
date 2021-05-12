package uk.gov.justice.digital.hmpps.courtlistsplitter.model

data class MessageStatus(
  val status: String? = null,
  val code: String? = null,
  val reason: String? = null,
  val detail: String? = null,
)
