package uk.gov.justice.digital.hmpps.courtlistsplitter.service

import com.amazonaws.services.sns.AmazonSNSClient
import com.amazonaws.services.sns.model.GetTopicAttributesResult
import com.amazonaws.services.sns.model.NotFoundException
import com.nhaarman.mockitokotlin2.whenever
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension

@ExtendWith(MockitoExtension::class)
internal class SnsServiceTest {
  @Mock
  private lateinit var amazonSNS: AmazonSNSClient

  private lateinit var snsService: SnsService

  @BeforeEach
  fun beforeEach() {
    snsService = SnsService("topic-name", amazonSNS)
  }

  @Test
  fun `should return true when topic is available`() {

    whenever(amazonSNS.getTopicAttributes("topic-name"))
      .thenReturn(GetTopicAttributesResult().withAttributes(mapOf("name" to "topic-name")))

    assertThat(snsService.isTopicReachable()).isTrue
  }

  @Test
  fun `should return false when exception thrown`() {

    whenever(amazonSNS.getTopicAttributes("topic-name")).thenThrow(NotFoundException::class.java)

    assertThat(snsService.isTopicReachable()).isFalse
  }

  @Test
  fun `should return false when null response is returned`() {

    whenever(amazonSNS.getTopicAttributes("topic-name"))
      .thenReturn(null)

    assertThat(snsService.isTopicReachable()).isFalse
  }

  @Test
  fun `should return false when no attributes are returned in response`() {

    whenever(amazonSNS.getTopicAttributes("topic-name"))
      .thenReturn(GetTopicAttributesResult())

    assertThat(snsService.isTopicReachable()).isFalse
  }
}
