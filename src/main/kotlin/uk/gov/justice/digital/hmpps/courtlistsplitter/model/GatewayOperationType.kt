package uk.gov.justice.digital.hmpps.courtlistsplitter.model

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty
import uk.gov.justice.digital.hmpps.courtlistsplitter.messaging.MessageParser.Companion.EXT_DOC_NS
import uk.gov.justice.digital.hmpps.courtlistsplitter.model.externaldocumentrequest.ExternalDocumentRequest
import javax.validation.Valid
import javax.validation.constraints.NotNull

data class GatewayOperationType(
  @field:Valid
  @field:NotNull
  @JacksonXmlProperty(namespace = EXT_DOC_NS, localName = "ExternalDocumentRequest")
  val externalDocumentRequest: ExternalDocumentRequest
)
