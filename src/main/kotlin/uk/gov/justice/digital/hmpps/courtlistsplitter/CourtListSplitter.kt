package uk.gov.justice.digital.hmpps.courtlistsplitter

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

@SpringBootApplication
@ConfigurationPropertiesScan
class CourtListSplitter

fun main(args: Array<String>) {
  runApplication<CourtListSplitter>(*args)
}
