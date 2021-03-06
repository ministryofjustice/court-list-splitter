package uk.gov.justice.digital.hmpps.courtlistsplitter.integration.health

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.kotlin.whenever
import org.springframework.boot.actuate.health.Health
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.digital.hmpps.courtlistsplitter.health.SnsCheck
import uk.gov.justice.digital.hmpps.courtlistsplitter.health.SqsCheck
import uk.gov.justice.digital.hmpps.courtlistsplitter.integration.IntegrationTestBase
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.function.Consumer

@ActiveProfiles("test")
class HealthCheckTest : IntegrationTestBase() {

  @MockBean
  private lateinit var sqsCheck: SqsCheck

  @MockBean
  private lateinit var snsCheck: SnsCheck

  @Test
  fun `Health page reports ok`() {
    whenever(sqsCheck.health()).thenReturn(Health.up().build())
    whenever(snsCheck.health()).thenReturn(Health.up().build())

    webTestClient.get()
      .uri("/health")
      .exchange()
      .expectStatus()
      .isOk
      .expectBody()
      .jsonPath("status").isEqualTo("UP")
  }

  @Test
  fun `Health info reports version`() {
    whenever(sqsCheck.health()).thenReturn(Health.up().build())
    whenever(snsCheck.health()).thenReturn(Health.up().build())

    webTestClient.get().uri("/health")
      .exchange()
      .expectStatus().isOk
      .expectBody().jsonPath("components.healthInfo.details.version").value(
        Consumer<String> {
          assertThat(it).startsWith(LocalDateTime.now().format(DateTimeFormatter.ISO_DATE))
        }
      )
  }

  @Test
  fun `Health ping page is accessible`() {
    webTestClient.get()
      .uri("/health/ping")
      .exchange()
      .expectStatus()
      .isOk
      .expectBody()
      .jsonPath("status").isEqualTo("UP")
  }

  @Test
  fun `readiness reports ok`() {
    webTestClient.get()
      .uri("/health/readiness")
      .exchange()
      .expectStatus()
      .isOk
      .expectBody()
      .jsonPath("status").isEqualTo("UP")
  }

  @Test
  fun `liveness reports ok`() {
    webTestClient.get()
      .uri("/health/liveness")
      .exchange()
      .expectStatus()
      .isOk
      .expectBody()
      .jsonPath("status").isEqualTo("UP")
  }
}
