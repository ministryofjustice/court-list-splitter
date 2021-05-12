package uk.gov.justice.digital.hmpps.courtlistsplitter.model.externaldocumentrequest

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDate

internal class InfoTest {
  @Test
  fun `given different sequence values objects are equal`() {
    val now = LocalDate.now()
    val info1 = Info(ouCode = "B16BG", sequence = 1L, dateOfHearing = now)
    val info2 = Info(ouCode = "B16BG", sequence = 2L, dateOfHearing = now)
    assertThat(info1).isEqualTo(info2)
  }

  @Test
  fun `given different dates objects are not equal`() {
    val now = LocalDate.now()
    val info1 = Info(ouCode = "B16BG", sequence = 1L, dateOfHearing = now)
    val info2 = Info(ouCode = "B16BG", sequence = 2L, dateOfHearing = now.plusDays(1))
    assertThat(info1).isNotEqualTo(info2)
  }
}
