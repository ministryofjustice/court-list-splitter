package uk.gov.justice.digital.hmpps.courtlistsplitter.service

import com.amazonaws.services.sns.AmazonSNS
import com.amazonaws.services.sns.model.PublishRequest
import com.amazonaws.services.sns.model.PublishResult
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.test.context.junit.jupiter.SpringExtension
import uk.gov.justice.digital.hmpps.courtlistsplitter.model.externaldocumentrequest.buildCase

@ExtendWith(SpringExtension::class, MockitoExtension::class)
@Import(MessageNotifierTest.TestNotifierConfig::class)
internal class MessageNotifierTest {

  @Autowired
  private lateinit var telemetryService: TelemetryService

  @Autowired
  private lateinit var messageNotifier: MessageNotifier

  @Autowired
  private lateinit var amazonSNSClient: AmazonSNS

  @Captor
  private lateinit var publishRequest: ArgumentCaptor<PublishRequest>

  @Test
  fun `when get case then serialise and publish to SNS`() {

    val case = buildCase(null, null, null)
    val result = PublishResult().withMessageId("messageId")
    whenever(amazonSNSClient.publish(any()))
      .thenReturn(result)

    messageNotifier.send(case, "message-id")

    verify(telemetryService).trackCourtCaseSplitEvent(case, "message-id")
    verify(amazonSNSClient).publish(publishRequest.capture())
    assertThat(publishRequest.value.message).contains("caseNo")
    assertThat(publishRequest.value.messageAttributes["messageType"]?.dataType).isEqualTo("String")
    assertThat(publishRequest.value.messageAttributes["messageType"]?.stringValue).isEqualTo("LIBRA_COURT_CASE")
  }

  @TestConfiguration
  class TestNotifierConfig {

    @MockBean
    private lateinit var telemetryService: TelemetryService

    @MockBean
    private lateinit var amazonSNSClient: AmazonSNS

    @Bean
    fun objectMapper(): ObjectMapper {
      val objectMapper = ObjectMapper()
      objectMapper.registerModule(JavaTimeModule())
      objectMapper.registerKotlinModule()
      return objectMapper
    }

    @Bean
    fun messageNotifier(): MessageNotifier {
      return MessageNotifier(objectMapper(), telemetryService, amazonSNSClient, "topicArn")
    }
  }
}
