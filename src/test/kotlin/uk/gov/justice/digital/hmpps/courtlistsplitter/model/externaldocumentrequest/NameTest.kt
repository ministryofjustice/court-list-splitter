package uk.gov.justice.digital.hmpps.courtlistsplitter.model.externaldocumentrequest

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class NameTest {
  private val name: Name = Name(title = "Mr.", forename1 = "David", forename2 = "Robert", surname = "BOWIE")

  @Test
  fun `given nulls and orenames then return names space separated`() {
    assertThat(name.getForenames()).isEqualTo("David Robert")
  }

  @Test
  fun `given no forenames return empty string for getForenames`() {
    val name1 = Name(title = "Mr.", surname = "BOWIE")

    assertThat(name1.getForenames()).isEqualTo("")
  }

  @Test
  fun `given one forename but in wrong position then return for getForenames and trim`() {
    val name1 = Name(title = "Mr.", forename2 = "  David ")

    assertThat(name1.getForenames()).isEqualTo("David")
  }

  @Test
  fun `given only a title when getForenames then return empty string`() {
    val name1 = Name(title = "Mr.", forename2 = "  ")
    assertThat(name1.getForenames()).isEmpty()
  }

  @Test
  fun `given fully populated name then return for getFullName`() {
    assertThat(name.getFullName()).isEqualTo("Mr. David Robert BOWIE")
  }
}
