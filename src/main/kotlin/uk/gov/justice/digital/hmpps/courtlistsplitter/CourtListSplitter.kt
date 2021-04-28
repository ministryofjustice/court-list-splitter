package uk.gov.justice.digital.hmpps.courtlistsplitter

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication()
class CourtListSplitter

fun main(args: Array<String>) {
  runApplication<CourtListSplitter>(*args)
}
