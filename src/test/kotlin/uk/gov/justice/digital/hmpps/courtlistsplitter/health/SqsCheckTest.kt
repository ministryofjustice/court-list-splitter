package uk.gov.justice.digital.hmpps.courtlistsplitter.health

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import org.springframework.boot.actuate.health.Status
import uk.gov.justice.digital.hmpps.courtlistsplitter.service.SqsService

@ExtendWith(MockitoExtension::class)
internal class SqsCheckTest {

  @Mock
  private lateinit var sqsService: SqsService

  @InjectMocks
  private lateinit var sqsCheck: SqsCheck

  @Test
  fun `when queue available then check is UP`() {
    whenever(sqsService.isQueueAvailable()).thenReturn(true)

    assertThat(sqsCheck.health().status).isEqualTo(Status.UP)
  }

  @Test
  fun `when queue not available then check is DOWN`() {
    whenever(sqsService.isQueueAvailable()).thenReturn(false)

    assertThat(sqsCheck.health().status).isEqualTo(Status.DOWN)
  }
}
