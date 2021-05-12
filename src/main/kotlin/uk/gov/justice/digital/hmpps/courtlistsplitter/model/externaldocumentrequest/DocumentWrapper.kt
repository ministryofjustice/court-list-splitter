package uk.gov.justice.digital.hmpps.courtlistsplitter.model.externaldocumentrequest

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty
import javax.validation.Valid
import javax.validation.constraints.NotNull

data class DocumentWrapper(

  @field:Valid
  @field:NotNull
  @JacksonXmlProperty(localName = "document")
  val document: MutableList<Document> = ArrayList()
)
