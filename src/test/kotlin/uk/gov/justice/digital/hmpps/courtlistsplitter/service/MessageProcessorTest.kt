package uk.gov.justice.digital.hmpps.courtlistsplitter.service

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.dataformat.xml.JacksonXmlModule
import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.argThat
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.test.context.junit.jupiter.SpringExtension
import uk.gov.justice.digital.hmpps.courtlistsplitter.messaging.MessageParser
import uk.gov.justice.digital.hmpps.courtlistsplitter.model.externaldocumentrequest.ExternalDocumentRequest
import uk.gov.justice.digital.hmpps.courtlistsplitter.model.externaldocumentrequest.Info
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths
import javax.validation.Validation

@ExtendWith(SpringExtension::class)
@Import(MessageProcessorTest.TestMessagingConfig::class)
internal class MessageProcessorTest {

  @Autowired
  private lateinit var telemetryService: TelemetryService

  @Autowired
  private lateinit var messageNotifier: MessageNotifier

  @Autowired
  private lateinit var messageProcessor: MessageProcessor

  @Test
  @Throws(IOException::class)
  fun `given message with 2 documents and 4 cases then track court list receipt and send cases`() {
    // 2 documents, 1 session per document, 2 cases per session
    val content = Files.readString(Paths.get("src/test/resources/messages/external-document-request-multi-session.xml"))

    messageProcessor.process(content, "message-id")

    verify(telemetryService, times(2)).trackCourtListEvent(any<Info>(), eq("message-id"))
    verify(messageNotifier).send(argThat(CourtCaseMatcher("1600032953")), eq("message-id"))
    verify(messageNotifier).send(argThat(CourtCaseMatcher("1600032979")), eq("message-id"))
    verify(messageNotifier).send(argThat(CourtCaseMatcher("1600032952")), eq("message-id"))
    verify(messageNotifier).send(argThat(CourtCaseMatcher("1600011111")), eq("message-id"))
  }

  @Test
  fun `given an invalid message then throw an exception`() {
    assertThrows<JsonProcessingException> { messageProcessor.process("<xml>asdad</xml>", "message-id") }
  }

  @TestConfiguration
  class TestMessagingConfig {

    @MockBean
    private lateinit var messageNotifier: MessageNotifier
    @MockBean
    private lateinit var telemetryService: TelemetryService

    @Bean
    fun messageProcessor(): MessageProcessor {
      return MessageProcessor(messageParser(), messageNotifier, telemetryService)
    }

    @Bean
    fun messageParser(): MessageParser<ExternalDocumentRequest> {
      val xmlModule = JacksonXmlModule()
      xmlModule.setDefaultUseWrapper(false)
      val mapper = XmlMapper(xmlModule)
      mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
      mapper.registerModule(JavaTimeModule())
      mapper.registerKotlinModule()
      val factory = Validation.buildDefaultValidatorFactory()
      return MessageParser(mapper, factory.validator)
    }
  }
}
