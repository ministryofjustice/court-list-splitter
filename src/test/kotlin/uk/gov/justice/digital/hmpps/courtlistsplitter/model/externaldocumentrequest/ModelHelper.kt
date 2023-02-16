package uk.gov.justice.digital.hmpps.courtlistsplitter.model.externaldocumentrequest

import java.time.LocalDate
import java.time.LocalTime
import java.time.Month

fun buildCase(caseNo: String?, dateOfHearing: LocalDate?, courtCode: String?): Case {
  val name = Name(title = "Mr.", forename1 = "David", forename2 = "Robert", surname = "BOWIE")
  val address = Address(line1 = "10 Downing St", line5 = "London", pcode = "SW1A 2AA")

  val offence1 = Offence(1, "Summary 1", "Title 1", "AS 1", "RW89008")
  val offence2 = Offence(2, "Summary 2", "Title 2", "AS 2", "CJ88160")

  val session = Session(
    1L,
    dateOfHearing ?: LocalDate.now(),
    "court name",
    "01",
    LocalTime.of(9, 30),
    LocalTime.of(12, 30),
    courtCode ?: "B14LO"
  )

  val block = Block(1L)
  block.session = session

  val case = Case(
    caseId = 1L,
    caseNo = caseNo ?: "100",
    seq = 1,
    name = name,
    defendantName = name.getFullName(),
    defendantType = "P",
    defendantSex = "M",
    defendantDob = LocalDate.of(1969, Month.AUGUST, 26),
    defendantAddress = address,
    defendantAge = 51,
    cro = "CRO1234",
    pnc = "PNC1234",
    listNo = "1st",
    nationality1 = "British",
    nationality2 = "Polish",
    offences = listOf(offence1, offence2)
  )
  case.block = block

  return case
}
