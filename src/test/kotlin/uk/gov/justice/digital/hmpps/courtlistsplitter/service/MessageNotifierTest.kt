package uk.gov.justice.digital.hmpps.courtlistsplitter.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.nhaarman.mockitokotlin2.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.test.context.junit.jupiter.SpringExtension
import uk.gov.justice.digital.hmpps.courtlistsplitter.model.externaldocumentrequest.buildCase

@ExtendWith(SpringExtension::class)
@Import(MessageNotifierTest.TestNotifierConfig::class)
internal class MessageNotifierTest {

  @Autowired
  private lateinit var telemetryService: TelemetryService

  @Autowired
  private lateinit var messageNotifier: MessageNotifier

  @Test
  fun `when get case then serialise`() {

    val case = buildCase(null, null, null)

    messageNotifier.send(case, "message-id")

    verify(telemetryService).trackCourtCaseEvent(case, "message-id")
  }

  @TestConfiguration
  class TestNotifierConfig {

    @MockBean
    private lateinit var telemetryService: TelemetryService

    @Bean
    fun objectMapper(): ObjectMapper {
      val objectMapper = ObjectMapper()
      objectMapper.registerModule(JavaTimeModule())
      objectMapper.registerKotlinModule()
      return objectMapper
    }
    @Bean
    fun messageNotifier(): MessageNotifier {
      return MessageNotifier(objectMapper(), telemetryService)
    }
  }
}
