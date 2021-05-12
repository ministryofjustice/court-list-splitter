package uk.gov.justice.digital.hmpps.courtlistsplitter.model.externaldocumentrequest

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.Month

internal class SessionTest {
  private val info: Info = Info(sequence = 1L, dateOfHearing = LocalDate.now(), ouCode = "B10JQ")
  private val document: Document = Document(info = info)
  private val dataJob: DataJob = DataJob(name = "dataJob")
  private val job: Job = Job(name = "dataJob")

  @Test
  fun `given null ouCode on session then use value from Info`() {
    dataJob.document = document
    job.dataJob = dataJob
    val session = Session(dateOfHearing = LocalDate.now(), start = LocalTime.NOON)
    session.job = job

    assertThat(session.courtCode).isEqualTo("B10JQ")
  }

  @Test
  fun `given non-null ouCode on session then use value from session and not Info`() {
    dataJob.document = document
    job.dataJob = dataJob
    val session = Session(ouCode = "B12JQ")
    session.job = job

    assertThat(session.courtCode).isEqualTo("B12JQ")
  }

  @Test
  fun `get session start time derived`() {
    val dateOfHearing = LocalDate.of(2020, Month.JULY, 19)
    val session = Session(dateOfHearing = dateOfHearing, start = LocalTime.NOON)
    assertThat(session.getSessionStartTime()).isEqualTo(LocalDateTime.of(2020, Month.JULY, 19, 12, 0, 0))
  }
}
