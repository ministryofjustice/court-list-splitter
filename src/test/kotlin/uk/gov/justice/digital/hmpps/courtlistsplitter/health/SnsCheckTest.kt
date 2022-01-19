package uk.gov.justice.digital.hmpps.courtlistsplitter.health

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import org.springframework.boot.actuate.health.Status
import uk.gov.justice.digital.hmpps.courtlistsplitter.service.SnsService

@ExtendWith(MockitoExtension::class)
internal class SnsCheckTest {

  @Mock
  private lateinit var snsService: SnsService

  @InjectMocks
  private lateinit var snsCheck: SnsCheck

  @Test
  fun `when queue available then check is UP`() {
    whenever(snsService.isTopicReachable()).thenReturn(true)

    assertThat(snsCheck.health().status).isEqualTo(Status.UP)
  }

  @Test
  fun `when queue not available then check is DOWN`() {
    whenever(snsService.isTopicReachable()).thenReturn(false)

    assertThat(snsCheck.health().status).isEqualTo(Status.DOWN)
  }
}
