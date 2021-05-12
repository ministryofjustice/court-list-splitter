package uk.gov.justice.digital.hmpps.courtlistsplitter.service

import com.fasterxml.jackson.core.JsonProcessingException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.courtlistsplitter.messaging.MessageParser
import uk.gov.justice.digital.hmpps.courtlistsplitter.model.externaldocumentrequest.Block
import uk.gov.justice.digital.hmpps.courtlistsplitter.model.externaldocumentrequest.Case
import uk.gov.justice.digital.hmpps.courtlistsplitter.model.externaldocumentrequest.Document
import uk.gov.justice.digital.hmpps.courtlistsplitter.model.externaldocumentrequest.ExternalDocumentRequest
import uk.gov.justice.digital.hmpps.courtlistsplitter.model.externaldocumentrequest.Info
import uk.gov.justice.digital.hmpps.courtlistsplitter.model.externaldocumentrequest.Session
import java.util.function.Function
import java.util.stream.Stream

@Service
class MessageProcessor(
  @Autowired
  private val messageParser: MessageParser<ExternalDocumentRequest>,
  @Autowired
  private val messageNotifier: MessageNotifier,
  @Autowired
  private val telemetryService: TelemetryService
) {

  @Throws(JsonProcessingException::class)
  fun process(message: String, messageId: String) {
    val externalDocumentRequest = messageParser.parseMessage(message, ExternalDocumentRequest::class.java)

    val documents = externalDocumentRequest.documentWrapper.document
    trackCourtListReceipt(documents, messageId)

    documents
      .stream()
      .flatMap<Session>(
        Function<Document, Stream<Session>> { document: Document ->
          document.data.job.sessions.stream()
        }
      )
      .flatMap<Block>(
        Function<Session, Stream<Block>> { session: Session ->
          session.blocks.stream()
        }
      )
      .flatMap<Case>(
        Function<Block, Stream<Case>> { block: Block ->
          block.cases.stream()
        }
      )
      .forEach { messageNotifier.send(it, messageId) }
  }

  private fun trackCourtListReceipt(documents: List<Document>, messageId: String) {
    documents.stream()
      .map { it.info }
      .distinct()
      .forEach { info: Info -> telemetryService.trackCourtListEvent(info, messageId) }
  }
}
